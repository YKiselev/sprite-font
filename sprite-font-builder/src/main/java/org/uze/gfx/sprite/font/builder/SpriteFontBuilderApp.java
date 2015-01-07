package org.uze.gfx.sprite.font.builder;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.dialog.Dialogs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uze.gfx.sprite.font.SpriteFont;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Uze on 06.01.2015.
 */
public class SpriteFontBuilderApp extends Application {

    public static final double COMBO_BOX_WIDTH = 200.0;
    public static final String APP_TITLE = "Sprite Font Builder";
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
    private SpriteFont spriteFont;

    @Override
    public void start(Stage stage) throws Exception {
        leftPaneWidth = 250.0;

        final BorderPane borderPane = new BorderPane();

        borderPane.setTop(createMenu());

        final TabPane tabPane = createTabPane();
        borderPane.setCenter(tabPane);

        final Scene scene = new Scene(borderPane, 640, 400, Color.WHITE);

        appStage = stage;

        stage.setTitle(APP_TITLE);
        stage.setScene(scene);
        stage.setMinWidth(400);
        stage.setMinHeight(200);
        stage.show();
    }

    private TabPane createTabPane() {
        final TabPane tabPane = new TabPane();

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

        final Button buildBtn = new Button("Build font sprite");
        buildBtn.setOnAction((e) -> onBuildFontSprite());

        vbox.getChildren().addAll(new Label("Character ranges:"), charRanges,
            new HBox(4.0, new Label("Default character:"), defaultCharacterField),
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

        final MenuItem fileExit = new MenuItem("Exit");
        fileExit.setOnAction((e) -> Platform.exit());

        menuFile.getItems().addAll(fileSaveAs, new SeparatorMenuItem(), fileExit);

        menuBar.getMenus().addAll(menuFile);

        return menuBar;
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
    }

    private void onSaveAs(ActionEvent e) {
        try {
            final FileChooser fileChooser1 = new FileChooser();
            fileChooser1.setTitle("Save Image");
            fileChooser1.setInitialFileName(spriteFont.getName());
            fileChooser1.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Jar archives (*.jar)", "jar"));

            if (spriteFont != null) {
                final File file = fileChooser1.showSaveDialog(appStage);
                if (file != null) {
                    try (FileOutputStream os = new FileOutputStream(file)) {
                        spriteFont.saveToStream(os);
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

    private void onBuildFontSprite() {
        try {
            final List<CharRange> ranges = getCharRanges();
            final SpriteFontBuilder builder = SpriteFontBuilder.create(getSelectedFont(), ranges, getDefaultCharacter());

            spriteFont = builder.build();
            bitmapTab.setContent(new ImageView(spriteFont.getImage()));
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
