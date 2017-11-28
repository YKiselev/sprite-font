/*
 * Copyright 2017 Yuriy Kiselev (uze@yandex.ru)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.ykiselev.gfx.sprite.font;

import com.github.ykiselev.gfx.font.Glyph;
import com.github.ykiselev.gfx.font.GlyphRange;
import com.github.ykiselev.gfx.font.SpriteFont;
import com.github.ykiselev.gfx.sprite.font.image.PngBytes;
import javafx.geometry.Bounds;
import javafx.geometry.VPos;
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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * Created by Uze on 07.01.2015.
 */
public final class FontRasterizer {

    private static final int MAX_GLYPH_BORDER = 10;

    private final Font font;

    private final Collection<char[]> characters;

    private final char defaultCharacter;

    private final int glyphXBorder;

    private final int glyphYBorder;

    public FontRasterizer(Font font, List<char[]> characters, char defaultCharacter, int glyphXBorder, int glyphYBorder) {
        if (glyphXBorder < 0 || glyphXBorder > MAX_GLYPH_BORDER) {
            throw new IllegalArgumentException("Border width should be in range 0-" + MAX_GLYPH_BORDER);
        }
        if (glyphYBorder < 0 || glyphYBorder > MAX_GLYPH_BORDER) {
            throw new IllegalArgumentException("Border height should be in range 0-" + MAX_GLYPH_BORDER);
        }
        this.font = font;
        this.characters = characters;
        this.defaultCharacter = defaultCharacter;
        this.glyphXBorder = glyphXBorder;
        this.glyphYBorder = glyphYBorder;
    }

    public SpriteFontAndImage build() {
        final Text text = createText();
        final List<Range> ranges = characters.stream()
                .map(Range::new)
                .map(r -> r.measure(text))
                .collect(Collectors.toList());
        final int fontHeight = ranges.stream()
                .map(Range::metrics)
                .mapToInt(RangeMetrics::height)
                .max()
                .orElse(0);
        final Canvas canvas = createCanvas(ranges, fontHeight);
        final int characterWidth = ranges.stream()
                .map(Range::metrics)
                .mapToInt(RangeMetrics::characterWidth)
                .reduce(0, (a, b) -> a == b ? a : 0);
        final SnapshotParameters snapshotParameters = new SnapshotParameters();
        snapshotParameters.setFill(Color.color(0, 0, 0, 0));
        // Note: render glyphs before taking image snapshot
        renderCharacters(canvas.getGraphicsContext2D(), ranges, fontHeight, characterWidth);
        final WritableImage image = canvas.snapshot(snapshotParameters, null);
        final SpriteFont spriteFont = new SpriteFont(
                fontHeight,
                defaultCharacter,
                characterWidth,
                ranges.stream()
                        .map(Range::toGlyphRange)
                        .toArray(GlyphRange[]::new),
                PngBytes.convert(image),
                glyphXBorder,
                glyphYBorder
        );
        return new SpriteFontAndImage(font.getName(), spriteFont, image);
    }

    private void renderCharacters(GraphicsContext ctx, Collection<Range> ranges, int fontHeight, int characterWidth) {
        final int width = (int) ctx.getCanvas().getWidth();

        ctx.setFont(font);
        ctx.setFill(Color.color(1, 1, 1, 1));
        ctx.setTextBaseline(VPos.BOTTOM);
        ctx.setTextAlign(TextAlignment.LEFT);
        ctx.setFontSmoothingType(FontSmoothingType.GRAY);

        final int lineHeight = fontHeight + glyphYBorder;
        int x = glyphXBorder;
        int y = lineHeight;
        for (Range range : ranges) {
            final RangeMetrics metrics = range.metrics();
            for (int i = 0; i < range.length(); i++) {
                final int w = metrics.width(i) + glyphXBorder;
                final char character = range.charAt(i);
                if (x + w > width) {
                    x = glyphXBorder;
                    y += lineHeight;
                }
                ctx.fillText(String.valueOf(character), x, y);
                range.glyph(
                        i,
                        new Glyph(
                                character,
                                x,
                                y - fontHeight,
                                (short) metrics.width(i)
                        )
                );
                x += w;
            }
        }
    }

    private Text createText() {
        final Text text = new Text();
        text.setFont(font);
        text.setBoundsType(TextBoundsType.VISUAL);
        text.setTextAlignment(TextAlignment.LEFT);
        text.setTextOrigin(VPos.BOTTOM);
        text.setFontSmoothingType(FontSmoothingType.GRAY);
        text.setSmooth(true);
        return text;
    }

    private static int nextPowerOfTwo(int value) {
        if (value == 0) {
            return 0;
        }
        if (value <= 0) {
            throw new IllegalArgumentException("Value should be positive!");
        }
        return (int) Math.pow(
                2.0,
                Math.ceil(
                        32 - Integer.numberOfLeadingZeros(value - 1)
                )
        );
    }

    private Canvas createCanvas(Collection<Range> ranges, int fontHeight) {
        final int[] widths = ranges.stream()
                .map(Range::metrics)
                .map(RangeMetrics::widths)
                .flatMapToInt(Arrays::stream)
                .toArray();
        final int totalWidth = glyphXBorder + Arrays.stream(widths)
                .map(v -> v + glyphXBorder)
                .sum();
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
        return new Canvas(
                rowWidth,
                nextPowerOfTwo(glyphYBorder + rows * (fontHeight + glyphYBorder))
        );
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

final class RangeMetrics {

    private final int[] widths;

    private final int characterWidth;

    private final int height;

    int[] widths() {
        return widths;
    }

    int width(int index) {
        return widths[index];
    }

    int characterWidth() {
        return characterWidth;
    }

    int height() {
        return height;
    }

    RangeMetrics(int[] widths, int characterWidth, int height) {
        this.widths = requireNonNull(widths);
        this.characterWidth = characterWidth;
        this.height = height;
    }
}

final class Range {

    private final char[] chars;

    private final RangeMetrics metrics;

    private final Glyph[] glyphs;

    char charAt(int index) {
        return chars[index];
    }

    int length() {
        return chars.length;
    }

    RangeMetrics metrics() {
        return metrics;
    }

    void glyph(int index, Glyph glyph) {
        glyphs[index] = glyph;
    }

    Range(char[] chars) {
        this(chars, null, new Glyph[chars.length]);
    }

    Range(char[] chars, RangeMetrics metrics, Glyph[] glyphs) {
        this.chars = chars;
        this.metrics = metrics;
        this.glyphs = glyphs;
    }

    Range measure(Text text) {
        int min = Integer.MAX_VALUE, max = 0, rangeHeight = 0, characterWidth = 0;
        final int[] widths = new int[chars.length];
        for (int i = 0; i < chars.length; i++) {
            final char ch = chars[i];
            text.setText(String.valueOf(ch));
            final Bounds bounds = text.getLayoutBounds();
            final int width = (int) Math.ceil(bounds.getWidth());
            final int height = (int) Math.ceil(bounds.getHeight());
            widths[i] = width;
            if (height > rangeHeight) {
                rangeHeight = height;
            }
            if (width < min) {
                min = width;
            }
            if (width > max) {
                max = width;
            }
        }
        if (min == max) {
            characterWidth = min;
        }
        return new Range(
                chars,
                new RangeMetrics(widths, characterWidth, rangeHeight),
                glyphs
        );
    }

    GlyphRange toGlyphRange() {
        return new GlyphRange(glyphs);
    }
}
