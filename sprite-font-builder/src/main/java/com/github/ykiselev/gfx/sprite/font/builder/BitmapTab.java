package com.github.ykiselev.gfx.sprite.font.builder;

import com.google.common.eventbus.EventBus;
import com.typesafe.config.Config;
import javafx.scene.control.Tab;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;

import static java.util.Objects.requireNonNull;

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
public final class BitmapTab implements BuilderTab {

    private final Tab tab = new Tab("Font Bitmap");

    private final EventBus eventBus;

    @Override
    public Tab tab() {
        return tab;
    }

    public BitmapTab(EventBus eventBus) {
        this.eventBus = requireNonNull(eventBus);
        tab.setClosable(false);
        eventBus.register(this);
    }

    public void show(Image image) {
        final ImageView imageView = new ImageView(image);
        imageView.setPreserveRatio(true);
        imageView.setBlendMode(BlendMode.EXCLUSION);
        final BorderPane pane = new BorderPane(imageView);
        pane.setBackground(
                new Background(new BackgroundFill(Color.WHITE, null, null))
        );
        tab.setContent(pane);
    }

    public void load(Config state) {
    }

    @Override
    public Config save(Config config) {
        return config;
    }
}
