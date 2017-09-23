package com.github.ykiselev.gfx.sprite.font.builder;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Created by Uze on 06.01.2015.
 */
public class SpriteFontBuilderApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        new AppStage(stage, 250.0);
    }

    public static void main(String[] args) {
        Application.launch(SpriteFontBuilderApp.class, args);
    }
}
