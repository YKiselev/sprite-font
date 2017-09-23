package com.github.ykiselev.gfx.sprite.font;

import com.github.ykiselev.gfx.font.Glyph;
import com.github.ykiselev.gfx.font.SpriteFont;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;

import javax.imageio.ImageIO;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import java.awt.image.BufferedImage;
import java.io.File;
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

    public SpriteFont getInfo() {
        return info;
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
        if (info.lineInterval() > 0) {
            builder.add("lineInterval", info.lineInterval());
        }
        final JsonArrayBuilder ab = Json.createArrayBuilder();
        for (Glyph glyph : info.glyphs()) {
            final JsonObjectBuilder b = Json.createObjectBuilder();
            b.add("character", glyph.character())
                    .add("x", glyph.x())
                    .add("y", glyph.y());
            if (glyph.width() > 0) {
                b.add("width", glyph.width());
            }
            ab.add(b);
        }
        builder.add("glyphs", ab.build());
        os.write(
                builder.build()
                        .toString()
                        .getBytes(StandardCharsets.UTF_8)
        );
    }

    public void saveGlyphImage(char value, File destFile) throws IOException {
        for (Glyph glyph : info.glyphs()) {
            if (glyph.character() == value) {
                final BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
                final BufferedImage glyphImage = bufferedImage.getSubimage(
                        glyph.x(),
                        glyph.y(),
                        glyph.width() != 0 ? glyph.width() : info.characterWidth(),
                        info.fontHeight()
                );
                ImageIO.write(glyphImage, "png", destFile);
                break;
            }
        }
    }
}
