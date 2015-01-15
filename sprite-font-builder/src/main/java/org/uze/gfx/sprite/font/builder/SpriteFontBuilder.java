package org.uze.gfx.sprite.font.builder;

import com.google.common.base.Preconditions;
import com.google.protobuf.ByteString;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.scene.text.*;
import org.uze.gfx.font.proto.FontProtos.Glyph;
import org.uze.gfx.font.proto.FontProtos.SpriteFont;
import org.uze.gfx.sprite.font.SpriteFontHolder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Uze on 07.01.2015.
 */
public class SpriteFontBuilder {

    public static final int BUF_SIZE = 16 * 1024;
    public static final int MAX_GLYPH_BORDER = 10;
    private final Font font;
    private final char[] characters;
    private final int defaultCharacterIndex;
    private final int glyphXBorder;
    private final int glyphYBorder;
    private int characterWidth;
    private int fontHeight;

    private SpriteFontBuilder(Font font, char[] characters, int defaultCharacterIndex, int glyphXBorder, int glyphYBorder) {
        Preconditions.checkArgument(glyphXBorder >= 0 && glyphXBorder <= MAX_GLYPH_BORDER);
        Preconditions.checkArgument(glyphYBorder >= 0 && glyphYBorder <= MAX_GLYPH_BORDER);

        this.font = font;
        this.characters = characters;
        this.defaultCharacterIndex = defaultCharacterIndex;
        this.glyphXBorder = glyphXBorder;
        this.glyphYBorder = glyphYBorder;
    }

    public static SpriteFontBuilder create(Font font, List<CharRange> ranges, char defaultCharacter, int glyphXBorder, int glyphYBorder) {
        final Set<Character> uniqueCharacters = ranges.stream()
            .map(CharRange::get)
            .flatMap(Collection::stream)
            .collect(Collectors.toSet());

        uniqueCharacters.add(defaultCharacter);

        final char[] chars = new char[uniqueCharacters.size()];
        int i = 0;
        for (Character ch : uniqueCharacters) {
            chars[i] = ch;
            i++;
        }

        Arrays.sort(chars);

        final int defaultCharacterIndex = Arrays.binarySearch(chars, defaultCharacter);

        return new SpriteFontBuilder(font, chars, defaultCharacterIndex, glyphXBorder, glyphYBorder);
    }

    public SpriteFontHolder build() {
        final int[] widths = measureCharacters();
        final Canvas canvas = createCanvas(widths);
        final Glyph[] glyphs = renderCharacters(canvas.getGraphicsContext2D(), widths);
        final WritableImage image = canvas.snapshot(new SnapshotParameters(), null);
        final SpriteFont.Builder fontBuilder = SpriteFont.newBuilder();

        fontBuilder.setCharacters(String.valueOf(characters))
            .addAllGlyph(Arrays.asList(glyphs))
            .setFontHeight(fontHeight);

        if (defaultCharacterIndex >= 0) {
            fontBuilder.setDefaultCharacterIndex(defaultCharacterIndex);
        }

        if (characterWidth > 0) {
            fontBuilder.setCharacterWidth(characterWidth);
        }

        if (glyphXBorder > 0) {
            fontBuilder.setGlyphXBorder(glyphXBorder);
        }

        if (glyphYBorder > 0) {
            fontBuilder.setGlyphYBorder(glyphYBorder);
        }

        final BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
        try {
            try (ByteArrayOutputStream os = new ByteArrayOutputStream(BUF_SIZE)) {
                ImageIO.write(bufferedImage, "png", os);
                fontBuilder.setBitmap(ByteString.copyFrom(os.toByteArray()));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return new SpriteFontHolder(font.getName(), fontBuilder.build(), image);
    }

    private Glyph[] renderCharacters(GraphicsContext ctx, int[] widths) {
        ctx.setFont(font);

        final int lineHeight = fontHeight + glyphYBorder;
        int x = glyphXBorder;
        int y = lineHeight;
        final Glyph[] glyphs = new Glyph[characters.length];
        final Glyph.Builder builder = Glyph.newBuilder();
        final int maxWidth = (int) ctx.getCanvas().getWidth();

        for (int i = 0; i < characters.length; i++) {
            final int w = widths[i] + glyphXBorder;
            final String text = String.valueOf(characters[i]);

            if (x + w > maxWidth) {
                x = glyphXBorder;
                y += lineHeight;
            }

            ctx.fillText(text, x, y);

            builder.clear()
                .setX(x)
                .setY(y);

            if (characterWidth != w) {
                builder.setWidth(w);
            }

            glyphs[i] = builder.build();

            x += w;
        }

        return glyphs;
    }

    private int[] measureCharacters() {
        final Text text = new Text();

        text.setFont(font);
        text.setBoundsType(TextBoundsType.LOGICAL);
        text.setTextAlignment(TextAlignment.LEFT);
        text.setFontSmoothingType(FontSmoothingType.LCD);

        final Scene scene = new Scene(new Group(text));

        text.setText("WWW");

        Bounds bounds = text.getLayoutBounds();
        fontHeight = (int) Math.ceil(bounds.getHeight());
        final int w1 = (int) bounds.getWidth();

        text.setText("iii");

        final int w2 = (int) text.getLayoutBounds().getWidth();

        final boolean isFixedPitch = (w1 == w2);

        if (isFixedPitch) {
            text.setText("x");
            characterWidth = (int) text.getLayoutBounds().getWidth();
        } else {
            characterWidth = 0;
        }

        final int[] widths = new int[characters.length];

        for (int i = 0; i < characters.length; i++) {
            text.setText(String.valueOf(characters[i]));
            bounds = text.getLayoutBounds();
            widths[i] = (int) Math.ceil(bounds.getWidth());
        }

        return widths;
    }

    private static int nextPowerOfTwo(int value) {
        if (value == 0) {
            return 0;
        }

        Preconditions.checkArgument(value > 0);

        return (int) Math.pow(2.0, Math.ceil(32 - Integer.numberOfLeadingZeros(value - 1)));
    }

    private Canvas createCanvas(int[] widths) {
        int totalWidth = glyphXBorder;
        for (int w : widths) {
            totalWidth += w + glyphXBorder;
        }

        int rowWidth = nextPowerOfTwo((int) Math.sqrt(totalWidth));
        int rows;

        while (true) {
            rows = getRowCount(widths, rowWidth);
            if (rows == 0) {
                rowWidth *= 2;
            } else {
                final int height = glyphYBorder + rows * (fontHeight + glyphYBorder);
                if (height > rowWidth) {
                    rowWidth *= 2;
                } else {
                    break;
                }
            }
        }

        return new Canvas(rowWidth, nextPowerOfTwo(glyphYBorder + rows * (fontHeight + glyphYBorder)));
    }

    private int getRowCount(int[] widths, int maxRowWidth) {
        int result = 0;
        int currentWidth = glyphXBorder;

        for (int charWidth : widths) {
            if (charWidth > maxRowWidth) {
                return 0;
            }
            currentWidth += charWidth + glyphXBorder;
            if (currentWidth > maxRowWidth) {
                result++;
                currentWidth = glyphXBorder + charWidth;
            }
        }

        if (currentWidth > glyphXBorder) {
            result++;
        }

        return result;
    }
}
