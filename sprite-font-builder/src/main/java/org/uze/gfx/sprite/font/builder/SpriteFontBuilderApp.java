package org.uze.gfx.sprite.font.builder;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

/**
 * Created by Uze on 06.01.2015.
 */
public class SpriteFontBuilderApp extends Application {

    private final Logger logger = LoggerFactory.getLogger(SpriteFontBuilderApp.class);
    private Stage appStage;
    private final Tab bitmapTab = new Tab("Font Bitmap");

    @Override
    public void start(Stage stage) throws Exception {
        final BorderPane borderPane = new BorderPane();

        final MenuBar menuBar = createMenu();
        borderPane.setTop(menuBar);

        final TabPane tabPane = createTabPane();
        borderPane.setCenter(tabPane);

//        TextFlow textFlow = new TextFlow();
//        Font font = new Font("Tahoma", 48);
//
//        Text text1 = new Text("He said \u0627\u0644\u0633\u0644\u0627\u0645");
//        text1.setFill(Color.RED);
//        text1.setFont(font);
//        Text text2 = new Text(" \u0639\u0644\u064a\u0643\u0645 to me.");
//        text2.setFill(Color.BLUE);
//        text2.setFont(font);
//        textFlow.getChildren().addAll(text1, text2);

        //Group group = new Group(textFlow);

        Scene scene = new Scene(borderPane, 640, 400, Color.WHITE);

        appStage = stage;

        stage.setTitle("Sprite Font Builder");
        stage.setScene(scene);
        stage.setMinWidth(400);
        stage.setMinHeight(200);
        stage.show();
    }

    private TabPane createTabPane() {
        final TabPane tabPane = new TabPane();

        Tab tab = new Tab();
        tab.setText("Font Settings");
        tab.setContent(new Rectangle(200, 200, Color.LIGHTSTEELBLUE));
        tab.setClosable(false);
        tabPane.getTabs().add(tab);

        bitmapTab.setClosable(false);
        tabPane.getTabs().add(bitmapTab);

        return tabPane;
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
        }
    }
}
