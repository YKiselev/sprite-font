package org.uze.gfx.sprite.font;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;
import org.uze.gfx.font.Glyph;
import org.uze.gfx.font.SpriteFont;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

/**
 * Created by Uze on 07.01.2015.
 */
public final class SpriteFontHolder {

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

    public SpriteFontHolder(String name, SpriteFont info, WritableImage image) {
        this.name = name;
        this.info = info;
        this.image = image;
    }

    public void saveToStream(OutputStream outputStream) throws IOException {
        final Manifest manifest = new Manifest();

        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");

        final String basePath = "fonts/sprite/";

        try (JarOutputStream os = new JarOutputStream(outputStream, manifest); ObjectOutputStream oos = new ObjectOutputStream(os)) {
            os.putNextEntry(
                    new JarEntry(basePath + name + ".bin")
            );
            oos.writeObject(info);
            os.closeEntry();
        }
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
