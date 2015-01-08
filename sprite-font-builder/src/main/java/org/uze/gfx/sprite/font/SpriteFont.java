package org.uze.gfx.sprite.font;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;
import org.uze.gfx.font.proto.FontProtos;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

/**
 * Created by Uze on 07.01.2015.
 */
public class SpriteFont {

    private final String name;

    private final FontProtos.SpriteFontInfo info;

    private final WritableImage image;

    public String getName() {
        return name;
    }

    public FontProtos.SpriteFontInfo getInfo() {
        return info;
    }

    public WritableImage getImage() {
        return image;
    }

    public SpriteFont(String name, FontProtos.SpriteFontInfo info, WritableImage image) {
        this.name = name;
        this.info = info;
        this.image = image;
    }

    public void saveToStream(OutputStream outputStream) throws IOException {
        final Manifest manifest = new Manifest();

        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");

        final String basePath = "fonts/sprite/" + name;

        try (JarOutputStream os = new JarOutputStream(outputStream, manifest)) {
            JarEntry entry = new JarEntry(basePath + "/info.pbuf");
            os.putNextEntry(entry);
            info.writeTo(os);
            os.closeEntry();

            entry = new JarEntry(basePath + "/image.png");
            os.putNextEntry(entry);
            final BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
            ImageIO.write(bufferedImage, "png", os);
            os.closeEntry();
        }
    }
}
