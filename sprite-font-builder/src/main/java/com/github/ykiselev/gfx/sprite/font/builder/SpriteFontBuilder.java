package com.github.ykiselev.gfx.sprite.font.builder;

import com.github.ykiselev.gfx.sprite.font.SpriteFontHolder;
import com.google.common.base.Preconditions;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Bounds;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextBoundsType;
import com.github.ykiselev.gfx.font.Glyph;
import com.github.ykiselev.gfx.font.GlyphBuilder;

import javax.imageio.ImageIO;
import java.awt.*;
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

        final SnapshotParameters snapshotParameters = new SnapshotParameters();
        snapshotParameters.setFill(Color.color(0, 0, 0, 0));

        final WritableImage image = canvas.snapshot(snapshotParameters, null);

        final com.github.ykiselev.gfx.font.SpriteFontBuilder fontBuilder = new com.github.ykiselev.gfx.font.SpriteFontBuilder();

        fontBuilder.withGlyphs(glyphs)
                .withFontHeight(fontHeight);

        if (defaultCharacterIndex >= 0) {
            fontBuilder.withDefaultCharacterIndex(defaultCharacterIndex);
        }

        if (characterWidth > 0) {
            fontBuilder.withCharacterWidth(characterWidth);
        }

        if (glyphXBorder > 0) {
            fontBuilder.withGlyphXBorder(glyphXBorder);
        }

        if (glyphYBorder > 0) {
            fontBuilder.withGlyphYBorder(glyphYBorder);
        }

        final BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
        try {
            final BufferedImage grayImage = new BufferedImage(
                    bufferedImage.getWidth(null),
                    bufferedImage.getHeight(null),
                    BufferedImage.TYPE_BYTE_GRAY
            );
            final Graphics2D pic = grayImage.createGraphics();
            pic.drawImage(bufferedImage, 0, 0, null);
            pic.dispose();
            try (ByteArrayOutputStream os = new ByteArrayOutputStream(BUF_SIZE)) {
                ImageIO.write(grayImage, "png", os);
                fontBuilder.withBitmap(os.toByteArray());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return new SpriteFontHolder(font.getName(), fontBuilder.createSpriteFont(), image);
    }

    private Glyph[] renderCharacters(GraphicsContext ctx, int[] widths) {
        final int width = (int) ctx.getCanvas().getWidth();

        ctx.setFont(font);
        ctx.setFill(Color.color(1, 1, 1, 1));
        ctx.setTextBaseline(VPos.BOTTOM);

        final int lineHeight = fontHeight + glyphYBorder;
        int x = glyphXBorder;
        int y = lineHeight;
        final Glyph[] glyphs = new Glyph[characters.length];
        final GlyphBuilder builder = new GlyphBuilder();

        for (int i = 0; i < characters.length; i++) {
            final int w = widths[i] + glyphXBorder;
            final char character = characters[i];

            if (x + w > width) {
                x = glyphXBorder;
                y += lineHeight;
            }

            ctx.fillText(String.valueOf(character), x, y);

            builder.clear()
                    .withCharacter(character)
                    .withX(x)
                    .withY(y - fontHeight);

            if (characterWidth != widths[i]) {
                builder.withWidth(widths[i]);
            }

            glyphs[i] = builder.createGlyph();

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
        final int w1 = (int) Math.ceil(bounds.getWidth());

        text.setText("iii");

        final int w2 = (int) Math.ceil(text.getLayoutBounds().getWidth());

        final boolean isFixedPitch = (w1 == w2);

        if (isFixedPitch) {
            text.setText("x");
            characterWidth = (int) Math.ceil(text.getLayoutBounds().getWidth());
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
