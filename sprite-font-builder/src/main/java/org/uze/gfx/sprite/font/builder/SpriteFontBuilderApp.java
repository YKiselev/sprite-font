package org.uze.gfx.sprite.font.builder;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.dialog.Dialogs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

/**
 * Created by Uze on 06.01.2015.
 */
public class SpriteFontBuilderApp extends Application {

    public static final double COMBO_BOX_WIDTH = 200.0;
    public static final String APP_TITLE = "Sprite Font Builder";
    private final Logger logger = LoggerFactory.getLogger(SpriteFontBuilderApp.class);
    private Stage appStage;
    private final Tab bitmapTab = new Tab("Font Bitmap");
    private final Text example = new Text();
    private final ComboBox<FontWeight> fontWeightComboBox = new ComboBox<>();
    private final ComboBox<FontPosture> fontPostureComboBox = new ComboBox<>();
    private final ComboBox<Integer> fontSizeComboBox = new ComboBox<>();
    private final TextField exampleText = new TextField();
    private final ListView<String> fontListView = new ListView<>();

    @Override
    public void start(Stage stage) throws Exception {
        final BorderPane borderPane = new BorderPane();

        final MenuBar menuBar = createMenu();
        borderPane.setTop(menuBar);

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

        final TextArea charRanges = new TextArea("32-126\n1025\n1040-1105\n9650\n9660");
        charRanges.setEditable(true);
        charRanges.setMinHeight(120.0);

        vbox.getChildren().add(charRanges);

        return result;
    }

    private Tab createFontSettingsTab() {
        final Tab tab = new Tab();

        tab.setText("Font Settings");
        tab.setClosable(false);

        final AnchorPane pane = new AnchorPane();

        tab.setContent(pane);

        final TextField fontFilter = new TextField();
        fontFilter.textProperty().addListener((a, b, c) -> onFontFilterChanged());

        AnchorPane.setTopAnchor(fontFilter, 2.0);
        AnchorPane.setLeftAnchor(fontFilter, 2.0);

        fontListView.setPrefWidth(250.0);
        fontListView.setEditable(false);
        fontListView.setFixedCellSize(Region.USE_COMPUTED_SIZE);
        fontListView.setCellFactory((listView) -> new FontListCell());
        fontListView.setItems(FXCollections.observableList(Font.getFamilies()));
        fontListView.getSelectionModel().selectedItemProperty().addListener((a, b, c) -> onNewFontSelected());

        pane.getChildren().addAll(fontFilter, fontListView);

        AnchorPane.setTopAnchor(fontListView, 20.0);
        AnchorPane.setLeftAnchor(fontListView, 2.0);
        AnchorPane.setBottomAnchor(fontListView, 2.0);

        final VBox vbox = new VBox(4.0);
        vbox.setPadding(new Insets(4.0));
        pane.getChildren().add(vbox);

        AnchorPane.setTopAnchor(vbox, 2.0);
        AnchorPane.setLeftAnchor(vbox, fontListView.getPrefWidth() + 2.0);
        AnchorPane.setBottomAnchor(vbox, 2.0);
        AnchorPane.setRightAnchor(vbox, 2.0);

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

//        example.setAlignment(Pos.TOP_LEFT);
//        example.setPadding(new Insets(4.0));
//        example.setPrefHeight(100.0);
//        example.setMaxHeight(100.0);
//        example.setTextOverrun(OverrunStyle.CLIP);
        example.setText("Example text");

        vbox.getChildren().addAll(fontWeightLabel, fontWeightComboBox,
            fontPostureLabel, fontPostureComboBox,
            fontSizeLabel, fontSizeComboBox,
            exampleText, example);

        fontListView.getSelectionModel().selectFirst();

        return tab;
    }

    private MenuBar createMenu() {
        final MenuBar menuBar = new MenuBar();

        final Menu menuFile = new Menu("File");

        final Menu fileSaveAs = new Menu("Save As...");

        fileSaveAs.setOnAction(this::onSaveAs);

        menuFile.getItems().add(fileSaveAs);

        menuBar.getMenus().addAll(menuFile);

        return menuBar;
    }

    private void onFontFilterChanged() {

    }

    private void onSaveAs(ActionEvent e) {
        final FileChooser fileChooser1 = new FileChooser();
        fileChooser1.setTitle("Save Image");

        final Node node = bitmapTab.getContent();
        if (node != null) {
            final WritableImage image = node.snapshot(new SnapshotParameters(), null);
            // todo - need to wait?
            if (image != null) {
                final File file = fileChooser1.showSaveDialog(appStage);
                if (file != null) {
                    try {
                        ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
                    } catch (IOException ex) {
                        logger.error("Save failed!", ex);
                    }
                }
            }
        } else {
            Dialogs.create()
                .title(APP_TITLE)
                .message("There is no image to save!")
                .showWarning();
        }
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
            }
        }
    }
}
