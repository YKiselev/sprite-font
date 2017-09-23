package com.github.ykiselev.gfx.sprite.font.builder;

import com.github.ykiselev.gfx.sprite.font.SpriteFontAndImage;
import com.github.ykiselev.gfx.sprite.font.SpriteFontBuilder;
import com.github.ykiselev.gfx.sprite.font.events.BuildSprite;
import com.github.ykiselev.gfx.sprite.font.events.LoadConfig;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigRenderOptions;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.controlsfx.dialog.Dialogs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.nio.file.Paths;

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
public final class AppStage {

    private static final String APP_TITLE = "Sprite Font Builder";

    private static final String FONT_BUILDER_APP_CONF = "font-builder-app.conf";

    private final Logger logger = LoggerFactory.getLogger(AppStage.class);

    private final Stage appStage;

    private final FontSettingTab fontSettingTab;

    private final CharacterRangeTab characterRangeTab;

    private final BitmapTab bitmapTab;

    private SpriteFontAndImage spriteFontAndImage;

    private static final FileChooser.ExtensionFilter JAR_EXT_FILTER = new FileChooser.ExtensionFilter("Jar archives (*.jar)", "jar");

    private final TabPane tabPane = new TabPane();

    private Config config;

    private final EventBus eventBus = new EventBus();

    public AppStage(Stage stage, double leftPaneWidth) throws Exception {
        this.appStage = stage;

        eventBus.register(this);

        final BorderPane borderPane = new BorderPane();
        borderPane.setTop(createMenu());

        bitmapTab = new BitmapTab(eventBus);
        fontSettingTab = new FontSettingTab(leftPaneWidth, eventBus);
        characterRangeTab = new CharacterRangeTab(eventBus);
        tabPane.getTabs().addAll(
                fontSettingTab.tab(),
                characterRangeTab.tab(),
                bitmapTab.tab()
        );
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
        eventBus.post(
                new LoadConfig(
                        config.getConfig("sprite-font-builder.state")
                )
        );
    }

    private void saveConfig() {
        final Config state = config.getConfig("sprite-font-builder.state");
        try {
            final Config newConfig = ConfigFactory.empty()
                    .withValue(
                            "sprite-font-builder.state",
                            bitmapTab.save(
                                    characterRangeTab.save(
                                            fontSettingTab.save(state)
                                    )
                            ).root()
                    );
            final ConfigRenderOptions options = ConfigRenderOptions.concise()
                    .setComments(false)
                    .setFormatted(true)
                    .setJson(false);

            try (PrintWriter writer = new PrintWriter(getConfigFile())) {
                writer.write(newConfig.root().render(options));
            }
        } catch (Exception ex) {
            logger.error("Failed to save config!", ex);
        }
    }

    private File getConfigFile() {
        return Paths.get(System.getProperty("user.home"), FONT_BUILDER_APP_CONF).toFile();
    }

    @Subscribe
    private void buildSprite(BuildSprite event) {
        try {
            final SpriteFontBuilder builder = SpriteFontBuilder.create(
                    fontSettingTab.getSelectedFont(),
                    event.ranges(),
                    event.defaultCharacter(),
                    event.glyphXBorder(),
                    event.glyphYBorder()
            );
            spriteFontAndImage = builder.build();
            bitmapTab.show(spriteFontAndImage.getImage());
            tabPane.getSelectionModel().select(bitmapTab.tab());
            saveConfig();
        } catch (Exception ex) {
            showWarning(ex.getMessage());
        }
    }

    private MenuBar createMenu() {
        final MenuBar menuBar = new MenuBar();

        final Menu menuFile = new Menu("File");

        final MenuItem fileSaveAs = new MenuItem("Save As...");
        fileSaveAs.setOnAction(this::onSaveAs);

        final MenuItem fileSaveGlyphAs = new MenuItem("Save Glyph As...");
        fileSaveGlyphAs.setOnAction(this::onSaveGlyphAs);

        final MenuItem fileSaveConfig = new MenuItem("Save Config");
        fileSaveConfig.setOnAction(this::onSaveConfig);

        final MenuItem fileResetConfig = new MenuItem("Reset Config");
        fileResetConfig.setOnAction(this::onResetConfig);

        final MenuItem fileExit = new MenuItem("Exit");
        fileExit.setOnAction((e) -> Platform.exit());

        menuFile.getItems().addAll(
                fileSaveAs,
                //fileSaveGlyphAs,
                new SeparatorMenuItem(),
                fileSaveConfig,
                new SeparatorMenuItem(),
                fileResetConfig,
                new SeparatorMenuItem(),
                fileExit
        );

        menuBar.getMenus().addAll(menuFile);

        return menuBar;
    }

    private void onResetConfig(ActionEvent actionEvent) {
        try {
            loadConfig(true);
        } catch (Exception ex) {
            showError("Config resetting failed!", ex);
        }
    }

    private void onSaveConfig(ActionEvent actionEvent) {
        try {
            saveConfig();
        } catch (Exception ex) {
            showError("Config save failed!", ex);
        }
    }

    /**
     * Debug method
     */
    private void onSaveGlyphAs(ActionEvent actionEvent) {
        try {
            if (spriteFontAndImage != null) {
                final FileChooser dlg = new FileChooser();
                dlg.setTitle("Save Glyph Image");
                dlg.setInitialFileName("char_g.png");
                dlg.getExtensionFilters().clear();
                dlg.getExtensionFilters().add(new FileChooser.ExtensionFilter("Png files", "png"));

                final File file = dlg.showSaveDialog(appStage);
                if (file != null) {
                    spriteFontAndImage.saveGlyphImage('g', file);
                }
            } else {
                showWarning("There is nothing to save!");
            }
        } catch (Exception ex) {
            logger.error("Glyph save failed!", ex);
            showError("Glyph save failed!", ex);
        }
    }

    private void onSaveAs(ActionEvent e) {
        try {
            if (spriteFontAndImage != null) {
                final FileChooser dlg = new FileChooser();
                dlg.setTitle("Save Image");
                dlg.setInitialFileName(spriteFontAndImage.getName() + ".jar");
                dlg.getExtensionFilters().clear();
                dlg.getExtensionFilters().add(JAR_EXT_FILTER);
                final File file = dlg.showSaveDialog(appStage);
                if (file != null) {
                    try (FileOutputStream os = new FileOutputStream(file)) {
                        spriteFontAndImage.saveToStream(os);
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
        Dialogs.create()
                .title(APP_TITLE)
                .message(message)
                .showWarning();
    }

    public static void showError(String message, Throwable t) {
        Dialogs.create()
                .title(APP_TITLE)
                .message(message)
                .showException(t);
    }

}
