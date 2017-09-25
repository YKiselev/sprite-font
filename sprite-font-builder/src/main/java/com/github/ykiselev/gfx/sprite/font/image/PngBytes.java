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

package com.github.ykiselev.gfx.sprite.font.image;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
public final class PngBytes {

    public static byte[] convert(Image image) {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream(16 * 1024)) {
            ImageIO.write(
                    toGrayScale(
                            SwingFXUtils.fromFXImage(image, null)
                    ),
                    "png",
                    os
            );
            return os.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static BufferedImage toGrayScale(BufferedImage src) {
        if (src.getType() == BufferedImage.TYPE_BYTE_GRAY) {
            return src;
        }
        final BufferedImage grayImage = new BufferedImage(
                src.getWidth(null),
                src.getHeight(null),
                BufferedImage.TYPE_BYTE_GRAY
        );
        final Graphics2D pic = grayImage.createGraphics();
        pic.drawImage(src, 0, 0, null);
        pic.dispose();
        return grayImage;
    }
}
