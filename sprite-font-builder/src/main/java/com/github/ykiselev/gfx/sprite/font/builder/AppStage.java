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

import com.github.ykiselev.gfx.sprite.font.SpriteFontAndImage;
import com.github.ykiselev.gfx.sprite.font.SpriteFontReceipt;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigRenderOptions;
import com.typesafe.config.ConfigValueFactory;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
public final class AppStage {

    private static final String APP_TITLE = "Sprite Font Builder";

    private static final String FONT_BUILDER_APP_CONF = "font-builder-app.conf";

    private static final Logger logger = LoggerFactory.getLogger(AppStage.class);

    private final Stage appStage;

    private final FontSettingTab fontSettingTab;

    private final CharacterRangeTab characterRangeTab;

    private final BitmapTab bitmapTab;

    private SpriteFontReceipt receipt;

    private SpriteFontAndImage spriteFontAndImage;

    private static final FileChooser.ExtensionFilter BIN_EXT_FILTER = new FileChooser.ExtensionFilter("Sprite Font (*.sf)", "*.sf");

    private static final FileChooser.ExtensionFilter PNG_EXT_FILTER = new FileChooser.ExtensionFilter("Font Sprite As Png Image (*.png)", "*.png");

    private static final FileChooser.ExtensionFilter JSON_EXT_FILTER = new FileChooser.ExtensionFilter("Sprite Font Description As Json (*.json)", "*.json");

    private static final FileChooser.ExtensionFilter RECEIPT_EXT_FILTER = new FileChooser.ExtensionFilter("Sprite Font Receipt (*.sfr)", "*.sfr");

    private final TabPane tabPane = new TabPane();

    private Config config;

    private File directory;

    public AppStage(Stage stage, double leftPaneWidth) throws Exception {
        this.appStage = stage;

        final BorderPane borderPane = new BorderPane();
        borderPane.setTop(createMenu());

        bitmapTab = new BitmapTab();
        fontSettingTab = new FontSettingTab(leftPaneWidth);
        characterRangeTab = new CharacterRangeTab();
        tabPane.getTabs().addAll(
                fontSettingTab.tab(),
                characterRangeTab.tab(),
                bitmapTab.tab()
        );
        tabPane.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    if (newValue == bitmapTab.tab()) {
                        ensureBitmap();
                        saveConfig();
                    }
                });
        borderPane.setCenter(tabPane);

        loadConfig(false);

        stage.setTitle(APP_TITLE);
        stage.setScene(
                new Scene(borderPane, 640, 400, Color.WHITE)
        );
        stage.setMinWidth(400);
        stage.setMinHeight(200);
        stage.show();
    }

    private SpriteFontReceipt createReceipt() {
        return new SpriteFontReceipt(
                fontSettingTab.getSelectedFont(),
                characterRangeTab.getCharRanges(),
                characterRangeTab.getDefaultCharacter(),
                characterRangeTab.getGlyphBorderWidth(),
                characterRangeTab.getGlyphBorderHeight()
        );
    }

    private void ensureBitmap() {
        try {
            final SpriteFontReceipt newReceipt = createReceipt();
            if (Objects.equals(receipt, newReceipt)) {
                return;
            }
            logger.info("Rebuilding bitmap...");
            receipt = newReceipt;
            spriteFontAndImage = newReceipt.build();
            logger.info("Showing bitmap...");
            bitmapTab.show(spriteFontAndImage.getImage());
            logger.info("Operation complete.");
        } catch (Exception ex) {
            showError("Operation failed!", ex);
        }
    }

    private void loadReceipt(File file) {
        loadReceipt(
                ConfigFactory.parseFile(file)
        );
    }

    private void loadReceipt(Config config) {
        final Config cfg = config.getConfig("sprite-font-builder.state");
        fontSettingTab.load(cfg);
        characterRangeTab.load(cfg);
        bitmapTab.load(cfg);
    }

    private void loadConfig(Config config) {
        loadReceipt(config);
        try {
            directory = new File(
                    config.getString("sprite-font-builder.state.working-directory")
            );
            if (!directory.isDirectory()) {
                directory = null;
            }
        } catch (Exception ex) {
            logger.error("Failed to load working dir!", ex);
        }
    }

    private void loadConfig(boolean reset) {
        config = ConfigFactory.load();
        if (!reset) {
            final File file = getConfigFile();
            if (file.exists()) {
                config = ConfigFactory.parseFile(file)
                        .withFallback(config);
            }
        } else {
            final File file = getConfigFile();
            if (file.exists()) {
                file.delete();
            }
        }
        loadConfig(config);
    }

    private void saveConfig() {
        try {
            saveFullConfig(getConfigFile());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private Config collectSpriteFontConfig() throws FileNotFoundException {
        return ConfigFactory.empty()
                .withValue(
                        "sprite-font-builder.state",
                        bitmapTab.save(
                                characterRangeTab.save(
                                        fontSettingTab.save(
                                                ConfigFactory.empty()
                                        )
                                )
                        ).root()
                );
    }

    private void saveFullConfig(File file) throws FileNotFoundException {
        saveToFile(
                collectSpriteFontConfig()
                        .withValue(
                                "sprite-font-builder.state.working-directory",
                                ConfigValueFactory.fromAnyRef(
                                        Objects.toString(directory)
                                )
                        ),
                file
        );
    }

    private void saveToFile(Config config, File file) throws FileNotFoundException {
        try (PrintWriter writer = new PrintWriter(file)) {
            writer.write(
                    config.root()
                            .render(
                                    ConfigRenderOptions.concise()
                                            .setComments(false)
                                            .setFormatted(true)
                                            .setJson(false)
                            )
            );
        }
    }

    private File getConfigFile() {
        return Paths.get(System.getProperty("user.home"), FONT_BUILDER_APP_CONF).toFile();
    }

    private MenuBar createMenu() {
        final MenuBar menuBar = new MenuBar();
        menuBar.getMenus().addAll(
                createFileMenu()
        );
        return menuBar;
    }

    private Menu createFileMenu() {
        final Menu menuFile = new Menu("File");
        final MenuItem fileSaveAs = new MenuItem("Save As...");
        fileSaveAs.setOnAction(this::onSaveAs);
        final MenuItem fileOpen = new MenuItem("Open...");
        fileOpen.setOnAction(this::onOpen);
        final MenuItem fileSaveConfig = new MenuItem("Save Config");
        fileSaveConfig.setOnAction(this::onSaveConfig);
        final MenuItem fileResetConfig = new MenuItem("Reset Config");
        fileResetConfig.setOnAction(this::onResetConfig);
        final MenuItem fileExit = new MenuItem("Exit");
        fileExit.setOnAction((e) -> Platform.exit());
        menuFile.getItems().addAll(
                fileSaveAs,
                fileOpen,
                new SeparatorMenuItem(),
                fileSaveConfig,
                fileResetConfig,
                new SeparatorMenuItem(),
                fileExit
        );
        return menuFile;
    }

    private void onOpen(ActionEvent actionEvent) {
        try {
            final FileChooser dlg = new FileChooser();
            dlg.setTitle("Open File");
            dlg.setInitialDirectory(directory);
            dlg.getExtensionFilters().clear();
            dlg.getExtensionFilters().addAll(RECEIPT_EXT_FILTER);
            final File file = dlg.showOpenDialog(appStage);
            if (file != null) {
                directory = file.getParentFile();
                final FileChooser.ExtensionFilter filter = dlg.getSelectedExtensionFilter();
                if (filter == RECEIPT_EXT_FILTER) {
                    loadReceipt(file);
                }
            }
        } catch (Exception ex) {
            showError("Failed to open file!", ex);
        }
    }

    private void onResetConfig(ActionEvent actionEvent) {
        try {
            loadConfig(true);
        } catch (Exception ex) {
            showError("Failed to reset config!", ex);
        }
    }

    private void onSaveConfig(ActionEvent actionEvent) {
        try {
            saveConfig();
        } catch (Exception ex) {
            showError("Failed to save config!", ex);
        }
    }

    private void onSaveAs(ActionEvent e) {
        try {
            ensureBitmap();
            if (spriteFontAndImage != null) {
                final FileChooser dlg = new FileChooser();
                dlg.setTitle("Save Sprite Font");
                dlg.setInitialDirectory(directory);
                dlg.setInitialFileName(spriteFontAndImage.getName());
                dlg.getExtensionFilters().clear();
                dlg.getExtensionFilters().addAll(
                        BIN_EXT_FILTER, PNG_EXT_FILTER, JSON_EXT_FILTER, RECEIPT_EXT_FILTER
                );
                final File file = dlg.showSaveDialog(appStage);
                if (file != null) {
                    directory = file.getParentFile();
                    saveConfig();
                    final FileChooser.ExtensionFilter filter = dlg.getSelectedExtensionFilter();
                    try (FileOutputStream os = new FileOutputStream(file)) {
                        if (filter == BIN_EXT_FILTER) {
                            spriteFontAndImage.saveSpriteFont(os);
                        } else if (filter == PNG_EXT_FILTER) {
                            spriteFontAndImage.savePng(os);
                        } else if (filter == JSON_EXT_FILTER) {
                            spriteFontAndImage.saveJson(os);
                        } else if (filter == RECEIPT_EXT_FILTER) {
                            saveToFile(
                                    collectSpriteFontConfig(),
                                    file
                            );
                        }
                    }
                }
            } else {
                showWarning("There is nothing to save!");
            }
        } catch (Exception ex) {
            logger.error("Save failed!", ex);
            showError("Save failed!", ex);
        }
    }

    public static void showWarning(String message) {
        final Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(APP_TITLE);
        alert.setHeaderText(message);
        alert.showAndWait();
    }

    public static void showError(String message, Throwable t) {
        logger.error("Operation failed!", t);
        final Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(APP_TITLE);
        alert.setHeaderText(message);
        alert.setContentText(t.toString());
        alert.showAndWait();
    }

}
