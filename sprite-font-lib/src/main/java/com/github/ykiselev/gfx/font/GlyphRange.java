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

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
public final class GlyphRange implements Serializable {

    private final transient char start;

    private final transient Glyph[] glyphs;

    public char start() {
        return start;
    }

    public Glyph[] glyphs() {
        return glyphs;
    }

    public GlyphRange(char start, Glyph[] glyphs) {
        this.start = start;
        this.glyphs = glyphs;
    }

    /**
     * Returns glyph for specified character
     *
     * @param character the character to return glyph for
     * @return the glyph if character is in range or {@code null}
     */
    public Glyph glyph(char character) {
        final int idx = character - start;
        if (idx < 0 || idx >= glyphs.length) {
            return null;
        }
        return glyphs[idx];
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GlyphRange range = (GlyphRange) o;
        return start == range.start &&
                Arrays.equals(glyphs, range.glyphs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, glyphs);
    }

    @Override
    public String toString() {
        return "GlyphRange{" +
                "start=" + start +
                ", glyphs=" + Arrays.toString(glyphs) +
                '}';
    }

    private Object writeReplace() throws ObjectStreamException {
        return new GlyphRangeReplacement(this);
    }
}

final class GlyphRangeReplacement implements Serializable {

    private static final long serialVersionUID = -4007521747869723305L;

    private char start;

    private int[] data;

    GlyphRangeReplacement(GlyphRange range) {
        start = range.start();
        data = toArray(range.glyphs());
    }

    Object readResolve() throws ObjectStreamException {
        return new GlyphRange(
                start,
                toGlyphs(data)
        );
    }

    private Glyph[] toGlyphs(int[] data) {
        final Glyph[] result = new Glyph[data.length >> 2];
        for (int i = 0; i < result.length; i++) {
            final int k = i * 4;
            result[i] = new Glyph(
                    (char) data[k],
                    data[k + 1],
                    data[k + 2],
                    data[k + 3]
            );
        }
        return result;
    }

    private int[] toArray(Glyph[] glyphs) {
        final int[] data = new int[4 * glyphs.length];
        for (int i = 0; i < glyphs.length; i++) {
            final Glyph glyph = glyphs[i];
            final int k = i * 4;
            data[k] = glyph.character();
            data[k + 1] = glyph.x();
            data[k + 2] = glyph.y();
            data[k + 3] = glyph.width();
        }
        return data;
    }
}