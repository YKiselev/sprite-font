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
