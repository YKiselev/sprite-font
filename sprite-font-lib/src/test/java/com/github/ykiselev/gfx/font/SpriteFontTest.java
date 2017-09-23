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