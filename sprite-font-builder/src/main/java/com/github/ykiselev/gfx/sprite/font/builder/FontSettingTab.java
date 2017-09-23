package com.github.ykiselev.gfx.sprite.font.builder;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigValueFactory;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.converter.IntegerStringConverter;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
public final class FontSettingTab implements BuilderTab {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final double COMBO_BOX_WIDTH = 200.0;

    private final Tab tab = new Tab();

    private final ComboBox<FontWeight> fontWeightComboBox = new ComboBox<>();

    private final ComboBox<FontPosture> fontPostureComboBox = new ComboBox<>();

    private final ComboBox<Integer> fontSizeComboBox = new ComboBox<>();

    private final Text example = new Text();

    private final TextField exampleText = new TextField();

    private final ListView<String> fontListView = new ListView<>();

    public Tab tab() {
        return tab;
    }

    public FontSettingTab(double leftPaneWidth) {
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

        rightBox.getChildren().addAll(
                fontWeightLabel,
                fontWeightComboBox,
                fontPostureLabel,
                fontPostureComboBox,
                fontSizeLabel,
                fontSizeComboBox,
                exampleText,
                example
        );
        fontListView.getSelectionModel().selectFirst();
    }

    @Override
    public void load(Config state) {
        final Config cfg;
        try {
            cfg = state.getConfig("font");
        } catch (Exception ex) {
            logger.error("Failed to load font section!", ex);
            return;
        }
        try {
            final String fontName = cfg.getString("name");
            if (!StringUtils.isEmpty(fontName)) {
                final int index = fontListView.getItems().indexOf(fontName);
                if (index >= 0) {
                    fontListView.getSelectionModel().select(index);
                    fontListView.scrollTo(index);
                }
            }
        } catch (Exception ex) {
            logger.error("Failed to load font name!", ex);
        }
        try {
            fontWeightComboBox.getSelectionModel().select(
                    FontWeight.valueOf(cfg.getString("weight"))
            );
        } catch (Exception ex) {
            logger.error("Failed to load font weight!", ex);
        }
        try {
            fontPostureComboBox.getSelectionModel().select(
                    FontPosture.valueOf(cfg.getString("posture"))
            );
        } catch (Exception ex) {
            logger.error("Failed to load font posture!", ex);
        }
        try {
            fontSizeComboBox.getSelectionModel().select((Integer) cfg.getInt("size"));
        } catch (Exception ex) {
            logger.error("Failed to load font size!", ex);
        }
    }

    @Override
    public Config save(Config config) {
        try {
            return config.withValue("font.name", ConfigValueFactory.fromAnyRef(fontListView.getSelectionModel().getSelectedItem()))
                    .withValue("font.weight", ConfigValueFactory.fromAnyRef(ObjectUtils.toString(fontWeightComboBox.getSelectionModel().getSelectedItem())))
                    .withValue("font.posture", ConfigValueFactory.fromAnyRef(ObjectUtils.toString(fontPostureComboBox.getSelectionModel().getSelectedItem())))
                    .withValue("font.size", ConfigValueFactory.fromAnyRef(fontSizeComboBox.getSelectionModel().getSelectedItem()));
        } catch (Exception ex) {
            logger.error("Failed to save config!", ex);
        }
        return config;
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

    private void onNewFontSelected() {
        if (!StringUtils.isEmpty(exampleText.getText())) {
            example.setText(exampleText.getText());
        } else {
            example.setText(getSelectedFontFamily());
        }
        example.setFont(getSelectedFont());
    }

    public Font getSelectedFont() {
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

    private static final class FontListCell extends ListCell<String> {

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
