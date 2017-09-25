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
import javafx.scene.image.WritableImage;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Created by Uze on 07.01.2015.
 */
public final class SpriteFontAndImage {

    private final String name;

    private final SpriteFont info;

    private final WritableImage image;

    public String getName() {
        return name;
    }

    public WritableImage getImage() {
        return image;
    }

    public SpriteFontAndImage(String name, SpriteFont info, WritableImage image) {
        this.name = name;
        this.info = info;
        this.image = image;
    }

    public void saveSpriteFont(OutputStream os) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(os)) {
            oos.writeObject(info);
        }
    }

    public void savePng(OutputStream os) throws IOException {
        os.write(info.bitmap());
    }

    public void saveJson(OutputStream os) throws IOException {
        final JsonObjectBuilder builder = Json.createObjectBuilder()
                .add("fontHeight", info.fontHeight())
                .add("defaultCharacterIndex", info.defaultCharacterIndex())
                .add("glyphXBorder", info.glyphXBorder())
                .add("glyphYBorder", info.glyphYBorder());
        if (info.characterWidth() > 0) {
            builder.add("characterWidth", info.characterWidth());
        }
        final JsonArrayBuilder ab = Json.createArrayBuilder();
        for (GlyphRange range : info.glyphs()) {
            final JsonArrayBuilder rab = Json.createArrayBuilder();
            for (Glyph glyph : range.glyphs()) {
                final JsonObjectBuilder b = Json.createObjectBuilder();
                b.add("character", glyph.character())
                        .add("x", glyph.x())
                        .add("y", glyph.y());
                if (glyph.width() > 0) {
                    b.add("width", glyph.width());
                }
                rab.add(b);
            }
            ab.add(rab);
        }
        builder.add("glyphs", ab.build());
        os.write(
                builder.build()
                        .toString()
                        .getBytes(StandardCharsets.UTF_8)
        );
    }

//    public void saveGlyphImage(char value, File destFile) throws IOException {
//        for (Glyph glyph : info.glyphs()) {
//            if (glyph.character() == value) {
//                final BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
//                final BufferedImage glyphImage = bufferedImage.getSubimage(
//                        glyph.x(),
//                        glyph.y(),
//                        glyph.width() != 0 ? glyph.width() : info.characterWidth(),
//                        info.fontHeight()
//                );
//                ImageIO.write(glyphImage, "png", destFile);
//                break;
//            }
//        }
//    }
}
