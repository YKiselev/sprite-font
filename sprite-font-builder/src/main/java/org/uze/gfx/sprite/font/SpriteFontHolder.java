package org.uze.gfx.sprite.font;

import javafx.scene.image.WritableImage;
import org.uze.gfx.font.proto.FontProtos;

import java.io.IOException;
import java.io.OutputStream;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

/**
 * Created by Uze on 07.01.2015.
 */
public class SpriteFontHolder {

    private final String name;

    private final FontProtos.SpriteFont info;

    private final WritableImage image;

    public String getName() {
        return name;
    }

    public FontProtos.SpriteFont getInfo() {
        return info;
    }

    public WritableImage getImage() {
        return image;
    }

    public SpriteFontHolder(String name, FontProtos.SpriteFont info, WritableImage image) {
        this.name = name;
        this.info = info;
        this.image = image;
    }

    public void saveToStream(OutputStream outputStream) throws IOException {
        final Manifest manifest = new Manifest();

        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");

        final String basePath = "fonts/sprite/";

        try (JarOutputStream os = new JarOutputStream(outputStream, manifest)) {
            JarEntry entry = new JarEntry(basePath + name + ".pbuf");
            os.putNextEntry(entry);
            info.writeTo(os);
            os.closeEntry();
        }
    }
}
