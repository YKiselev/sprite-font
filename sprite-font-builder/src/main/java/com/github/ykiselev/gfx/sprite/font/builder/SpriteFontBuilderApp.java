package com.github.ykiselev.gfx.sprite.font.builder;

import com.google.common.base.Joiner;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigRenderOptions;
import com.typesafe.config.ConfigValueFactory;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.dialog.Dialogs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.github.ykiselev.gfx.sprite.font.SpriteFontHolder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Uze on 06.01.2015.
 */
public class SpriteFontBuilderApp extends Application {

    public static final double COMBO_BOX_WIDTH = 200.0;
    public static final String APP_TITLE = "Sprite Font Builder";
    public static final String FONT_BUILDER_APP_CONF = "font-builder-app.conf";
    private final Logger logger = LoggerFactory.getLogger(SpriteFontBuilderApp.class);
    private Stage appStage;
    private final Tab bitmapTab = new Tab("Font Bitmap");
    private final ComboBox<FontWeight> fontWeightComboBox = new ComboBox<>();
    private final ComboBox<FontPosture> fontPostureComboBox = new ComboBox<>();
    private final ComboBox<Integer> fontSizeComboBox = new ComboBox<>();
    private final Text example = new Text();
    private final TextField exampleText = new TextField();
    private final ListView<String> fontListView = new ListView<>();
    private double leftPaneWidth = 200.0;
    private final TextArea charRanges = new TextArea("32-126\n1025\n1040-1105\n9650\n9660");
    private final TextField defaultCharacterField = new TextField("?");
    private SpriteFontHolder spriteFontHolder;
    public static final FileChooser.ExtensionFilter JAR_EXT_FILTER = new FileChooser.ExtensionFilter("Jar archives (*.jar)", "jar");
    private TextField borderWidthField = new TextField("0");
    private TextField borderHeightField = new TextField("0");
    private TabPane tabPane = new TabPane();
    private Config config;

    @Override
    public void start(Stage stage) throws Exception {
        leftPaneWidth = 250.0;

        final BorderPane borderPane = new BorderPane();

        borderPane.setTop(createMenu());

        final TabPane tabPane = createTabPane();
        borderPane.setCenter(tabPane);

        loadConfig(false);

        final Scene scene = new Scene(borderPane, 640, 400, Color.WHITE);

        appStage = stage;

        stage.setTitle(APP_TITLE);
        stage.setScene(scene);
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

        final Config state = config.getConfig("sprite-font-builder.state");
        try {
            final String fontName = state.getString("font.name");
            if (!StringUtils.isEmpty(fontName)) {
                final int index = fontListView.getItems().indexOf(fontName);
                if (index >= 0) {
                    fontListView.getSelectionModel().select(index);
                    fontListView.scrollTo(index);
                }
            }
        } catch (Exception ex) {
            logger.warn("Failed to load font name: {}", ex);
        }

        try {
            fontWeightComboBox.getSelectionModel().select(FontWeight.valueOf(state.getString("font.weight")));
        } catch (Exception ex) {
            logger.warn("Failed to load font weight: {}", ex);
        }

        try {
            fontPostureComboBox.getSelectionModel().select(FontPosture.valueOf(state.getString("font.posture")));
        } catch (Exception ex) {
            logger.warn("Failed to load font posture: {}", ex);
        }

        try {
            fontSizeComboBox.getSelectionModel().select((Integer) state.getInt("font.size"));
        } catch (Exception ex) {
            logger.warn("Failed to load font size: {}", ex);
        }

        try {
            charRanges.setText(Joiner.on('\n').join(state.getStringList("glyph.character-ranges")));
        } catch (Exception ex) {
            logger.warn("Failed to load character ranges: {}", ex);
        }

        try {
            final String value = state.getString("glyph.default-character");
            if (value != null && value.length() != 1) {
                logger.warn("Invalid default character: \"{}\"", value);
            } else {
                defaultCharacterField.setText(value);
            }
        } catch (Exception ex) {
            logger.warn("Failed to load default character: {}", ex);
        }

        try {
            borderWidthField.setText(state.getString("glyph.border.width"));
        } catch (Exception ex) {
            logger.warn("Failed to load glyph border width: {}", ex);
        }

        try {
            borderHeightField.setText(state.getString("glyph.border.height"));
        } catch (Exception ex) {
            logger.warn("Failed to load glyph border height: {}", ex);
        }
    }

    private void saveConfig() {
        final Config state = config.getConfig("sprite-font-builder.state");
        try {
            Config newState = state.withValue("font.name", ConfigValueFactory.fromAnyRef(fontListView.getSelectionModel().getSelectedItem()))
                .withValue("font.weight", ConfigValueFactory.fromAnyRef(ObjectUtils.toString(fontWeightComboBox.getSelectionModel().getSelectedItem())))
                .withValue("font.posture", ConfigValueFactory.fromAnyRef(ObjectUtils.toString(fontPostureComboBox.getSelectionModel().getSelectedItem())))
                .withValue("font.size", ConfigValueFactory.fromAnyRef(fontSizeComboBox.getSelectionModel().getSelectedItem()))
                .withValue("glyph.character-ranges",
                    ConfigValueFactory.fromIterable(Arrays.asList(StringUtils.split(charRanges.getText(), "[\\r\\n]+")))
                )
                .withValue("glyph.default-character", ConfigValueFactory.fromAnyRef(defaultCharacterField.getText()))
                .withValue("glyph.border.width", ConfigValueFactory.fromAnyRef(borderWidthField.getText()))
                .withValue("glyph.border.height", ConfigValueFactory.fromAnyRef(borderHeightField.getText()));

            final Config newConfig = ConfigFactory.empty()
                .withValue("sprite-font-builder.state", newState.root());
            final ConfigRenderOptions options = ConfigRenderOptions.concise()
                .setComments(false)
                .setFormatted(true)
                .setJson(false);

            try (PrintWriter writer = new PrintWriter(getConfigFile())) {
                writer.write(newConfig.root().render(options));
            }
        } catch (Exception ex) {
            logger.warn("Failed to save config: {}", ex);
        }
    }

    private File getConfigFile() {
        return Paths.get(System.getProperty("user.home"), FONT_BUILDER_APP_CONF).toFile();
    }

    private TabPane createTabPane() {
        bitmapTab.setClosable(false);

        tabPane.getTabs().addAll(createFontSettingsTab(), createCharacterRangesTab(), bitmapTab);

        return tabPane;
    }

    private Tab createCharacterRangesTab() {
        final Tab result = new Tab("Character ranges");

        result.setClosable(false);

        VBox vbox = new VBox(4.0);
        vbox.setPadding(new Insets(4.0));
        result.setContent(vbox);

        charRanges.setEditable(true);
        charRanges.setMinHeight(120.0);

        defaultCharacterField.setMaxWidth(30.0);
        borderWidthField.setMaxWidth(30.0);
        borderHeightField.setMaxWidth(30.0);

        final Button buildBtn = new Button("Build font sprite");
        buildBtn.setOnAction((e) -> onBuildFontSprite());

        vbox.getChildren().addAll(new Label("Character ranges:"), charRanges,
            new HBox(4.0,
                new Label("Default character:"), defaultCharacterField,
                new Label("Border width:"), borderWidthField,
                new Label("Border height:"), borderHeightField),
            buildBtn);

        return result;
    }

    private Tab createFontSettingsTab() {
        final Tab tab = new Tab();

        tab.setText("Font Settings");
        tab.setClosable(false);

        final AnchorPane pane = new AnchorPane();

        tab.setContent(pane);

        final VBox leftBox = new VBox(4.0);
        leftBox.setPadding(new Insets(4.0));

        leftBox.setPrefWidth(leftPaneWidth);
        pane.getChildren().add(leftBox);

        AnchorPane.setTopAnchor(leftBox, 2.0);
        AnchorPane.setLeftAnchor(leftBox, 2.0);
        AnchorPane.setBottomAnchor(leftBox, 2.0);

        final TextField fontFilter = new TextField();
        fontFilter.textProperty().addListener((a, b, c) -> onFontFilterChanged(b, c));
        fontFilter.setPromptText("Start typing font family to filter list");

        fontListView.setEditable(false);
        fontListView.setFixedCellSize(Region.USE_COMPUTED_SIZE);
        fontListView.setCellFactory((listView) -> new FontListCell());
        fontListView.setItems(FXCollections.observableList(Font.getFamilies()));
        fontListView.getSelectionModel().selectedItemProperty().addListener((a, b, c) -> onNewFontSelected());

        VBox.setVgrow(fontListView, Priority.ALWAYS);

        leftBox.getChildren().addAll(fontFilter, fontListView);

        final VBox rightBox = new VBox(4.0);
        rightBox.setPadding(new Insets(4.0));
        pane.getChildren().add(rightBox);

        AnchorPane.setTopAnchor(rightBox, 2.0);
        AnchorPane.setLeftAnchor(rightBox, leftPaneWidth + 2.0);
        AnchorPane.setBottomAnchor(rightBox, 2.0);
        AnchorPane.setRightAnchor(rightBox, 2.0);

        final Label fontWeightLabel = new Label("Font weight:");
        fontWeightLabel.setLabelFor(fontWeightComboBox);
        fontWeightComboBox.setItems(FXCollections.observableArrayList(FontWeight.values()));
        fontWeightComboBox.getSelectionModel().select(FontWeight.NORMAL);
        fontWeightComboBox.setPrefWidth(COMBO_BOX_WIDTH);
        fontWeightComboBox.getSelectionModel().selectedItemProperty().addListener((a, b, c) -> onNewFontSelected());

        final Label fontPostureLabel = new Label("Font posture:");
        fontPostureLabel.setLabelFor(fontPostureComboBox);
        fontPostureComboBox.setItems(FXCollections.observableArrayList(FontPosture.values()));
        fontPostureComboBox.getSelectionModel().select(FontPosture.REGULAR);
        fontPostureComboBox.setPrefWidth(COMBO_BOX_WIDTH);
        fontPostureComboBox.getSelectionModel().selectedItemProperty().addListener((a, b, c) -> onNewFontSelected());

        final Label fontSizeLabel = new Label("Font size:");
        fontSizeLabel.setLabelFor(fontSizeComboBox);
        fontSizeComboBox.setEditable(true);
        fontSizeComboBox.setItems(FXCollections.observableArrayList(8, 10, 11, 12, 14, 16, 18, 20, 22, 24, 36, 48));
        fontSizeComboBox.getSelectionModel().select(Integer.valueOf(10));
        fontSizeComboBox.setPrefWidth(COMBO_BOX_WIDTH);
        fontSizeComboBox.getSelectionModel().selectedItemProperty().addListener((a, b, c) -> onNewFontSelected());
        fontSizeComboBox.setConverter(new IntegerStringConverter());

        exampleText.setPromptText("Leave empty to use font family as example text");
        exampleText.textProperty().addListener((a, b, c) -> onNewFontSelected());

        example.setText("Example text");
        example.setFocusTraversable(false);

        rightBox.getChildren().addAll(fontWeightLabel, fontWeightComboBox,
            fontPostureLabel, fontPostureComboBox,
            fontSizeLabel, fontSizeComboBox,
            exampleText, example);

        fontListView.getSelectionModel().selectFirst();

        return tab;
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
            final FileChooser fileChooser1 = new FileChooser();
            fileChooser1.setTitle("Save Glyph Image");
            fileChooser1.setInitialFileName("char_g.png");
            fileChooser1.getExtensionFilters().clear();
            fileChooser1.getExtensionFilters().add(new FileChooser.ExtensionFilter("Png files", "png"));

            if (spriteFontHolder != null) {
                final File file = fileChooser1.showSaveDialog(appStage);
                if (file != null) {
                    spriteFontHolder.saveGlyphImage('g', file);
                }
            } else {
                showWarning("There is nothing to save!");
            }
        } catch (Exception ex) {
            logger.error("Glyph save failed!", ex);
            showError("Glyph save failed!", ex);
        }
    }

    private void onFontFilterChanged(String oldValue, String newValue) {
        final String prefix = newValue.toUpperCase();
        final String selectedItem = fontListView.getSelectionModel().getSelectedItem();
        final List<String> items;
        if (StringUtils.isEmpty(newValue)) {
            items = Font.getFamilies();
        } else {
            items = Font.getFamilies()
                .stream()
                .filter((s) -> s.toUpperCase().startsWith(prefix))
                .sorted()
                .collect(Collectors.toList());
        }
        fontListView.setItems(FXCollections.observableList(items));
        fontListView.getSelectionModel().select(selectedItem);
        if (fontListView.getSelectionModel().getSelectedIndex() == -1) {
            fontListView.getSelectionModel().selectFirst();
        }
    }

    private void onSaveAs(ActionEvent e) {
        try {
            final FileChooser fileChooser1 = new FileChooser();
            fileChooser1.setTitle("Save Image");
            fileChooser1.setInitialFileName(spriteFontHolder.getName() + ".jar");
            fileChooser1.getExtensionFilters().clear();
            fileChooser1.getExtensionFilters().add(JAR_EXT_FILTER);

            if (spriteFontHolder != null) {
                final File file = fileChooser1.showSaveDialog(appStage);
                if (file != null) {
                    try (FileOutputStream os = new FileOutputStream(file)) {
                        spriteFontHolder.saveToStream(os);
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

    private void showWarning(String message) {
        Dialogs.create()
            .title(APP_TITLE)
            .message(message)
            .showWarning();
    }

    private void showError(String message, Throwable t) {
        Dialogs.create()
            .title(APP_TITLE)
            .message(message)
            .showException(t);
    }

    private void onNewFontSelected() {
        if (!StringUtils.isEmpty(exampleText.getText())) {
            example.setText(exampleText.getText());
        } else {
            example.setText(getSelectedFontFamily());
        }
        example.setFont(getSelectedFont());
    }

    private Font getSelectedFont() {
        return Font.font(
            getSelectedFontFamily(),
            getSelectedFontWeight(),
            getSelectedFontPosture(),
            getSelectedFontSize()
        );
    }

    private double getSelectedFontSize() {
        return fontSizeComboBox.getSelectionModel().getSelectedItem();
    }

    private FontPosture getSelectedFontPosture() {
        return fontPostureComboBox.getSelectionModel().getSelectedItem();
    }

    private FontWeight getSelectedFontWeight() {
        return fontWeightComboBox.getSelectionModel().getSelectedItem();
    }

    private String getSelectedFontFamily() {
        return fontListView.getSelectionModel().getSelectedItem();
    }

    private char getDefaultCharacter() {
        final String value = defaultCharacterField.getText();
        if (StringUtils.isEmpty(value) || value.length() != 1) {
            throw new IllegalArgumentException("Bad default character: " + value);
        }
        return value.charAt(0);
    }

    private int getGlyphBorderWidth() {
        String value = borderWidthField.getText();
        if (StringUtils.isEmpty(value)) {
            value = "0";
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid glyph border width: " + value);
        }
    }

    private int getGlyphBorderHeight() {
        String value = borderHeightField.getText();
        if (StringUtils.isEmpty(value)) {
            value = "0";
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid glyph border height: " + value);
        }
    }

    private void onBuildFontSprite() {
        try {
            final List<CharRange> ranges = getCharRanges();
            final SpriteFontBuilder builder = SpriteFontBuilder.create(
                getSelectedFont(),
                ranges,
                getDefaultCharacter(),
                getGlyphBorderWidth(),
                getGlyphBorderHeight()
            );

            spriteFontHolder = builder.build();

            final ImageView imageView = new ImageView(spriteFontHolder.getImage());

            imageView.setPreserveRatio(true);
            imageView.setBlendMode(BlendMode.EXCLUSION);

            final BorderPane pane = new BorderPane(imageView);

            pane.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));

            bitmapTab.setContent(pane);
            tabPane.getSelectionModel().select(bitmapTab);

            saveConfig();
        } catch (Exception ex) {
            showWarning(ex.getMessage());
        }
    }

    private List<CharRange> getCharRanges() {
        final String text = charRanges.getText();
        if (StringUtils.isEmpty(text)) {
            return Collections.emptyList();
        }

        final List<CharRange> result = new ArrayList<>();
        final String[] lines = text.split("\\r?\\n|\\r|,");
        for (String line : lines) {
            if (StringUtils.isEmpty(line)) {
                continue;
            }
            final String[] parts = line.split("-");
            if (parts.length == 2) {
                result.add(new CharRange(toChar(Integer.parseInt(parts[0])), toChar(Integer.parseInt(parts[1]))));
            } else if (parts.length == 1) {
                final char start = toChar(Integer.parseInt(parts[0]));

                result.add(new CharRange(start, start));
            } else {
                throw new IllegalArgumentException("Bad range: " + line);
            }
        }

        return result;
    }

    private char toChar(int value) {
        if (value < Character.MIN_VALUE || value > Character.MAX_VALUE) {
            throw new IllegalArgumentException("Bad character: " + value);
        }
        return (char) value;
    }

    private static class FontListCell extends ListCell<String> {

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (item != null) {
                final Font font = Font.font(item);
                final Text text = new Text(item);

                text.setFont(font);
                text.setFontSmoothingType(FontSmoothingType.LCD);

                setGraphic(text);
            } else {
                setGraphic(null);
            }
        }
    }

}
