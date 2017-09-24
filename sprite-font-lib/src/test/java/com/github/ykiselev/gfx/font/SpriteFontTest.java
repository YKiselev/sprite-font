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
public class SpriteFontTest {

    @Test
    public void shouldSerialize() throws Exception {
        final SpriteFont spriteFont = new SpriteFont(
                14,
                100,
                10,
                new Glyph[]{
                        new Glyph(1, 2, 3, 4),
                        new Glyph(2, 8, 3, 8)
                },
                new byte[]{1, 2, 3},
                2,
                3,
                4
        );
        final byte[] bytes;
        try (ByteArrayOutputStream os = new ByteArrayOutputStream(); ObjectOutputStream oos = new ObjectOutputStream(os)) {
            oos.writeObject(spriteFont);
            bytes = os.toByteArray();
        }
        final SpriteFont result;
        try (InputStream is = new ByteArrayInputStream(bytes); ObjectInputStream ois = new ObjectInputStream(is)) {
            result = (SpriteFont) ois.readObject();
        }
        assertEquals(spriteFont, result);
    }
}