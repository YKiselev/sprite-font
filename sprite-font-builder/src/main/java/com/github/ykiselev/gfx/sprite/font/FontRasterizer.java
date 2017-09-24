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
import com.github.ykiselev.gfx.font.SpriteFont;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Bounds;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextBoundsType;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
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
public final class FontRasterizer {

    private static final int MAX_GLYPH_BORDER = 10;

    private final Font font;

    private final char[] characters;

    private final int defaultCharacterIndex;

    private final int glyphXBorder;

    private final int glyphYBorder;

    private int characterWidth;

    private int fontHeight;

    public FontRasterizer(Font font, char[] characters, int defaultCharacterIndex, int glyphXBorder, int glyphYBorder) {
        if (glyphXBorder < 0 || glyphXBorder > MAX_GLYPH_BORDER) {
            throw new IllegalArgumentException("Border width should be in range 0-" + MAX_GLYPH_BORDER);
        }
        if (glyphYBorder < 0 || glyphYBorder > MAX_GLYPH_BORDER) {
            throw new IllegalArgumentException("Border height should be in range 0-" + MAX_GLYPH_BORDER);
        }
        this.font = font;
        this.characters = characters.clone();
        this.defaultCharacterIndex = defaultCharacterIndex;
        this.glyphXBorder = glyphXBorder;
        this.glyphYBorder = glyphYBorder;
    }

    public static SpriteFontAndImage create(Font font, List<CharRange> ranges, char defaultCharacter, int glyphXBorder, int glyphYBorder) {
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
        return new FontRasterizer(font, chars, defaultCharacterIndex, glyphXBorder, glyphYBorder).build();
    }

    public SpriteFontAndImage build() {
        final int[] widths = measureCharacters();
        final Canvas canvas = createCanvas(widths);
        final SnapshotParameters snapshotParameters = new SnapshotParameters();
        snapshotParameters.setFill(Color.color(0, 0, 0, 0));
        // Note: render glyphs before taking image snapshot
        final Glyph[] glyphs = renderCharacters(canvas.getGraphicsContext2D(), widths);
        final WritableImage image = canvas.snapshot(snapshotParameters, null);
        final SpriteFont spriteFont = new SpriteFont(
                fontHeight,
                defaultCharacterIndex,
                characterWidth,
                glyphs,
                toPngBytes(image),
                0,
                glyphXBorder,
                glyphYBorder
        );
        return new SpriteFontAndImage(font.getName(), spriteFont, image);
    }

    private byte[] toPngBytes(Image image) {
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
            try (ByteArrayOutputStream os = new ByteArrayOutputStream(16 * 1024)) {
                ImageIO.write(grayImage, "png", os);
                return os.toByteArray();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
