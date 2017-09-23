package com.github.ykiselev.gfx.sprite.font.builder;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Created by Uze on 06.01.2015.
 */
public class SpriteFontBuilderApp extends Application {

    private AppStage appStage;

    @Override
    public void start(Stage stage) throws Exception {
        appStage = new AppStage(stage, 250.0);
    }

}
