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
import java.nio.ByteBuffer;
import java.util.Arrays;

import static java.util.Objects.requireNonNull;

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
public final class GlyphRange implements Serializable {

    private final transient Glyph[] glyphs;

    public Glyph[] glyphs() {
        return glyphs;
    }

    public GlyphRange(Glyph[] glyphs) {
        this.glyphs = requireNonNull(glyphs);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GlyphRange range = (GlyphRange) o;
        return Arrays.equals(glyphs, range.glyphs);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(glyphs);
    }

    @Override
    public String toString() {
        return "GlyphRange{" +
                ", glyphs=" + Arrays.toString(glyphs) +
                '}';
    }

    private Object writeReplace() throws ObjectStreamException {
        return new GlyphRangeReplacement(this);
    }
}

/**
 * Each glyph is stored as 3 integers, first is packed (hi 16 bits - char, lo 16 bits - width) then x and y.
 */
final class GlyphRangeReplacement implements Serializable {

    private static final long serialVersionUID = -4007521747869723305L;

    private final byte[] data;

    GlyphRangeReplacement(GlyphRange range) {
        data = toArray(range.glyphs());
    }

    Object readResolve() throws ObjectStreamException {
        return new GlyphRange(
                toGlyphs(data)
        );
    }

    private Glyph[] toGlyphs(byte[] data) {
        final Glyph[] result = new Glyph[data.length >> 3];
        final ByteBuffer buf = ByteBuffer.wrap(data);
        for (int i = 0; i < result.length; i++) {
            result[i] = new Glyph(
                    buf.getChar(),
                    buf.getShort() & 0xffff,
                    buf.getShort() & 0xffff,
                    buf.getShort()
            );
        }
        return result;
    }

    private byte[] toArray(Glyph[] glyphs) {
        final ByteBuffer buf = ByteBuffer.allocate(8 * glyphs.length);
        for (final Glyph glyph : glyphs) {
            buf.putChar(glyph.character());
            buf.putShort((short) (glyph.x() & 0xffff));
            buf.putShort((short) (glyph.y() & 0xffff));
            buf.putShort(glyph.width());
        }
        return buf.array();
    }
}