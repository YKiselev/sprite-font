package org.uze.gfx.sprite.font;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

/**
 * Created by Uze on 07.01.2015.
 */
public class SpriteFont {

    private final String name;

    private final SpriteFontInfo info;

    private final WritableImage image;

    public String getName() {
        return name;
    }

    public SpriteFontInfo getInfo() {
        return info;
    }

    public WritableImage getImage() {
        return image;
    }

    public SpriteFont(String name, SpriteFontInfo info, WritableImage image) {
        this.name = name;
        this.info = info;
        this.image = image;
    }

    public void saveToStream(OutputStream outputStream) throws IOException {
        final Manifest manifest = new Manifest();

        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");

        final String basePath = "fonts/sprite/" + name;

        final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

        try (JarOutputStream os = new JarOutputStream(outputStream, manifest)) {
            JarEntry entry = new JarEntry(basePath + "/info.json");
            os.putNextEntry(entry);
            os.write(gson.toJson(info).getBytes(StandardCharsets.UTF_8));
            os.closeEntry();

            entry = new JarEntry(basePath + "/image.png");
            os.putNextEntry(entry);
            final BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
            ImageIO.write(bufferedImage, "png", os);
            os.closeEntry();
        }
    }
}
