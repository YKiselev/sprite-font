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

package com.github.ykiselev.gfx.sprite.font.builder;

import com.typesafe.config.Config;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
public final class BitmapTab implements BuilderTab {

    private final Tab tab = new Tab("Font Bitmap");

    @Override
    public Tab tab() {
        return tab;
    }

    public BitmapTab() {
        tab.setClosable(false);
    }

    public void show(Image image) {
        final ImageView imageView = new ImageView(image);
        imageView.setPreserveRatio(true);
        imageView.setBlendMode(BlendMode.EXCLUSION);
        imageView.setCache(true);
        final ScrollPane scrollPane = new ScrollPane(imageView);
        tab.setContent(scrollPane);
        tab.setTooltip(new Tooltip("Bitmap size: " + image.getWidth() + ":" + image.getHeight()));
    }

    @Override
    public void load(Config state) {
    }

    @Override
    public Config save(Config config) {
        return config;
    }
}
