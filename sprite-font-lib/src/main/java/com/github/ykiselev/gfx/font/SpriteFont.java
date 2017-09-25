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

package com.github.ykiselev.gfx.font;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
public final class SpriteFont implements Serializable {

    private static final long serialVersionUID = -6255551253816672629L;

    private final int fontHeight;

    private final int defaultCharacterIndex;

    // If greater than 0, font is fixed pitch font
    private final int characterWidth;

    private final GlyphRange[] glyphs;

    private final byte[] bitmap;

    // Left and right glyph border size
    private final int glyphXBorder;

    // Top and bottom border size
    private final int glyphYBorder;

    public int fontHeight() {
        return fontHeight;
    }

    public int defaultCharacterIndex() {
        return defaultCharacterIndex;
    }

    public int characterWidth() {
        return characterWidth;
    }

    public GlyphRange[] glyphs() {
        return glyphs;
    }

    public byte[] bitmap() {
        return bitmap;
    }

    public int glyphXBorder() {
        return glyphXBorder;
    }

    public int glyphYBorder() {
        return glyphYBorder;
    }

    public SpriteFont(int fontHeight, int defaultCharacterIndex, int characterWidth, GlyphRange[] glyphs, byte[] bitmap, int glyphXBorder, int glyphYBorder) {
        this.fontHeight = fontHeight;
        this.defaultCharacterIndex = defaultCharacterIndex;
        this.characterWidth = characterWidth;
        this.glyphs = glyphs;
        this.bitmap = bitmap;
        this.glyphXBorder = glyphXBorder;
        this.glyphYBorder = glyphYBorder;
    }

    @Override
    public String toString() {
        return "SpriteFont{" +
                "fontHeight=" + fontHeight +
                ", defaultCharacterIndex=" + defaultCharacterIndex +
                ", characterWidth=" + characterWidth +
                ", glyphXBorder=" + glyphXBorder +
                ", glyphYBorder=" + glyphYBorder +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SpriteFont that = (SpriteFont) o;
        return fontHeight == that.fontHeight &&
                defaultCharacterIndex == that.defaultCharacterIndex &&
                characterWidth == that.characterWidth &&
                glyphXBorder == that.glyphXBorder &&
                glyphYBorder == that.glyphYBorder &&
                Arrays.equals(glyphs, that.glyphs) &&
                Arrays.equals(bitmap, that.bitmap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fontHeight, defaultCharacterIndex, characterWidth, glyphs, bitmap, glyphXBorder, glyphYBorder);
    }
}