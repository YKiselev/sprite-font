package org.uze.gfx.sprite.font.builder;

import com.google.common.base.Preconditions;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.scene.text.*;
import org.uze.gfx.sprite.font.Glyph;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Uze on 07.01.2015.
 */
public class SpriteFontBuilder {

    private final Font font;
    private final char[] characters;
    private boolean isFixedPitch;
    private int charWidth;
    private int fontHeight;

    private SpriteFontBuilder(Font font, char[] characters) {
        this.font = font;
        this.characters = characters;
    }

    public static SpriteFontBuilder create(Font font, List<CharRange> ranges, char defaultCharacter) {
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

        return new SpriteFontBuilder(font, chars);
    }

    public WritableImage build() {
        final int[] widths = measureCharacters();
        final Canvas canvas = createCanvas(widths);
        final Glyph[] glyphs = renderCharacters(canvas.getGraphicsContext2D(), widths);

        return canvas.snapshot(new SnapshotParameters(), null);
    }

    private Glyph[] renderCharacters(GraphicsContext ctx, int[] widths) {
        ctx.setFont(font);

        final int maxWidth = (int) ctx.getCanvas().getWidth();

        int x = 0;
        int y = fontHeight;
        final Glyph[] glyphs = new Glyph[characters.length];

        for (int i = 0; i < characters.length; i++) {
            final int w = widths[i];
            final String text = String.valueOf(characters[i]);

            if (x + w > maxWidth) {
                x = 0;
                y += fontHeight;
            }

            ctx.fillText(text, x, y);

            final Glyph glyph = new Glyph(x, y, w, fontHeight);
            glyphs[i] = glyph;

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

        isFixedPitch = (w1 == w2);

        if (isFixedPitch) {
            text.setText("x");
            charWidth = (int) text.getLayoutBounds().getWidth();
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

        final double power = Math.ceil(32 - Integer.numberOfLeadingZeros(value - 1));
        return (int) Math.pow(2.0, power);
    }

    private Canvas createCanvas(int[] widths) {
        int totalWidth = 0;
        for (int w : widths) {
            totalWidth += w;
        }

        int rowWidth = nextPowerOfTwo((int) Math.sqrt(totalWidth));
        int rows;

        while (true) {
            rows = getRowCount(widths, rowWidth);
            if (rows == 0) {
                rowWidth *= 2;
            } else {
                final int height = rows * fontHeight;
                if (height > rowWidth) {
                    rowWidth *= 2;
                } else {
                    break;
                }
            }
        }

        final int height = nextPowerOfTwo(rows * fontHeight);

        return new Canvas(rowWidth, height);
    }

    private int getRowCount(int[] widths, int maxRowWidth) {
        int result = 0;
        int currentWidth = 0;

        for (int charWidth : widths) {
            if (charWidth > maxRowWidth) {
                return 0;
            }
            currentWidth += charWidth;
            if (currentWidth > maxRowWidth) {
                result++;
                currentWidth = charWidth;
            }
        }

        if (currentWidth > 0) {
            result++;
        }

        return result;
    }
}
