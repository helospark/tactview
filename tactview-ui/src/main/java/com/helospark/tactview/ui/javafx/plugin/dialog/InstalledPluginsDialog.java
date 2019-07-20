package com.helospark.tactview.ui.javafx.plugin.dialog;

import java.io.InputStream;

import com.helospark.tactview.core.plugin.PluginManager;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class InstalledPluginsDialog {
    private Stage stage;
    private PluginManager pluginManager;

    public InstalledPluginsDialog(PluginManager pluginManager) {
        this.pluginManager = pluginManager;
        BorderPane borderPane = new BorderPane();
        borderPane.getStyleClass().add("dialog-root");

        Scene dialog = new Scene(borderPane);
        dialog.getStylesheets().add("stylesheet.css");
        stage = new Stage();
        stage.setHeight(400);
        stage.setWidth(600);

        GridPane pluginsGridPane = createGridPane();

        ScrollPane centerScrollPane = new ScrollPane(pluginsGridPane);
        centerScrollPane.setHbarPolicy(ScrollBarPolicy.NEVER);
        centerScrollPane.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);

        borderPane.setCenter(centerScrollPane);

        HBox buttonBar = new HBox();
        buttonBar.getStyleClass().add("render-dialog-button-bar");

        Region emptyRegion = new Region();
        HBox.setHgrow(emptyRegion, Priority.ALWAYS);
        buttonBar.getChildren().add(emptyRegion);

        Button okButton = new Button("Close");
        okButton.setOnMouseClicked(e -> {
            stage.close();
        });

        buttonBar.getChildren().add(okButton);

        borderPane.setBottom(buttonBar);

        stage.setTitle("Plugins");
        stage.setScene(dialog);
    }

    private GridPane createGridPane() {
        GridPane pluginsGridPane = new GridPane();
        pluginsGridPane.setVgap(4.0);
        pluginsGridPane.setHgap(15.0);
        pluginsGridPane.getStyleClass().add("render-dialog-grid-pane");

        int index = 0;
        for (var pluginDescriptor : pluginManager.getInstalledPlugins()) {
            Image image = loadPluginLogo(pluginDescriptor.getLogo());
            ImageView imageView = new ImageView(image);
            imageView.setFitHeight(64);
            imageView.setFitWidth(64);

            HBox pluginHbox = new HBox();
            pluginHbox.getChildren().add(imageView);
            pluginHbox.getStyleClass().add("plugin-list-element");

            VBox pluginDescriptionVbox = new VBox();
            Text pluginListTitle = new Text(pluginDescriptor.getName());
            pluginListTitle.getStyleClass().add("plugin-list-title");

            Text pluginListDescription = new Text(pluginDescriptor.getDescription());
            pluginListDescription.getStyleClass().add("plugin-list-description");

            Text pluginListAuthor = new Text(pluginDescriptor.getAuthor());
            pluginListAuthor.getStyleClass().add("plugin-list-author");

            pluginDescriptionVbox.getChildren().add(pluginListTitle);
            pluginDescriptionVbox.getChildren().add(pluginListDescription);
            pluginDescriptionVbox.getChildren().add(pluginListAuthor);

            pluginHbox.getChildren().add(pluginDescriptionVbox);

            pluginsGridPane.add(pluginHbox, 0, index++);
        }

        return pluginsGridPane;
    }

    private Image loadPluginLogo(String path) {
        Image image = null;
        if (path != null) {
            InputStream stream = getClass().getResourceAsStream("/" + path);
            image = new Image(stream);
        }

        if (image == null) {
            InputStream stream = getClass().getResourceAsStream("/icons/effect/fallback.png");

            image = new Image(stream);
        }
        return image;
    }

    public void show() {
        stage.show();
        stage.toFront();
    }

}
