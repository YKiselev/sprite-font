package com.github.ykiselev.gfx.sprite.font.builder;

import com.typesafe.config.Config;
import javafx.scene.control.Tab;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import static java.util.Objects.requireNonNull;

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
public final class BitmapTab implements BuilderTab {

    private final Tab tab = new Tab("Font Bitmap");

    private final Stage stage;

    @Override
    public Tab tab() {
        return tab;
    }

    public BitmapTab(Stage stage) {
        this.stage = requireNonNull(stage);
        tab.setClosable(false);
    }

    public void show(Image image) {
        final ImageView imageView = new ImageView(image);
        imageView.setPreserveRatio(true);
        imageView.setBlendMode(BlendMode.EXCLUSION);
        imageView.setCache(true);
        final BorderPane pane = new BorderPane(imageView);
        pane.setBackground(
                new Background(new BackgroundFill(Color.WHITE, null, null))
        );
        stage.widthProperty().addListener((observable, oldValue, newValue) -> {
            imageView.setFitWidth(
                    Math.min(image.getWidth(), newValue.doubleValue())
            );
        });
        stage.heightProperty().addListener((observable, oldValue, newValue) -> {
            imageView.setFitHeight(
                    Math.min(image.getHeight(), newValue.doubleValue())
            );
        });
        tab.setContent(pane);
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
