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

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.junit.Assert.assertEquals;

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
public class GlyphRangeTest {

    @Test
    public void shouldSerialize() throws Exception {
        final GlyphRange range = new GlyphRange(
                new Glyph[]{
                        new Glyph((char) 1, 0xffff, 3, (short) 4),
                        new Glyph((char) 2, 8, 0xffff, (short) 8),
                        new Glyph(Character.MAX_VALUE, 8, 5, Short.MAX_VALUE),
                        new Glyph(Character.MIN_VALUE, 32, 6, Short.MAX_VALUE),
                        new Glyph(Character.MAX_VALUE, 55, 7, Short.MIN_VALUE),
                        new Glyph(Character.MIN_VALUE, 72, 8, Short.MIN_VALUE)
                }
        );
        final byte[] bytes;
        try (ByteArrayOutputStream os = new ByteArrayOutputStream(); ObjectOutputStream oos = new ObjectOutputStream(os)) {
            oos.writeObject(range);
            bytes = os.toByteArray();
        }
        final GlyphRange result;
        try (InputStream is = new ByteArrayInputStream(bytes); ObjectInputStream ois = new ObjectInputStream(is)) {
            result = (GlyphRange) ois.readObject();
        }
        assertEquals(range, result);
    }

}