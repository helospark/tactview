package com.helospark.tactview.ui.javafx.plugin.dialog;

import java.io.InputStream;
import java.util.List;

import com.helospark.tactview.core.plugin.PluginDescriptor;
import com.helospark.tactview.core.plugin.PluginManager;
import com.helospark.tactview.ui.javafx.plugin.RestartDialogOpener;
import com.helospark.tactview.ui.javafx.plugin.service.PluginInstallationService;

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
    private RestartDialogOpener restartDialogOpener;
    private PluginInstallationService pluginInstallationService;

    public InstalledPluginsDialog(PluginManager pluginManager, PluginInstallationService pluginInstallationService, RestartDialogOpener restartDialogOpener) {
        this.pluginManager = pluginManager;
        this.pluginInstallationService = pluginInstallationService;
        this.restartDialogOpener = restartDialogOpener;

        BorderPane borderPane = new BorderPane();
        borderPane.getStyleClass().add("dialog-root");

        Scene dialog = new Scene(borderPane);
        dialog.getStylesheets().add("stylesheet.css");
        stage = new Stage();
        stage.setHeight(400);
        stage.setWidth(400);

        GridPane pluginsGridPane = createGridPane();

        ScrollPane centerScrollPane = new ScrollPane(pluginsGridPane);
        centerScrollPane.setHbarPolicy(ScrollBarPolicy.NEVER);
        centerScrollPane.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);

        borderPane.setCenter(centerScrollPane);

        HBox buttonBar = new HBox();
        buttonBar.getStyleClass().add("render-dialog-button-bar");

        Button installButton = new Button("Install...");
        installButton.setOnMouseClicked(e -> {
            pluginInstallationService.installPlugin();
        });
        buttonBar.getChildren().add(installButton);

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

        List<PluginDescriptor> installedPlugins = pluginManager.getInstalledPlugins();

        if (installedPlugins.isEmpty()) {
            pluginsGridPane.add(new Text("You have no plugins installed yet"), 0, 0);
        } else {
            int index = 0;
            for (var pluginDescriptor : installedPlugins) {
                Image image = loadPluginLogo(pluginDescriptor.getLogo());
                ImageView imageView = new ImageView(image);
                imageView.setFitHeight(64);
                imageView.setFitWidth(64);

                HBox pluginHbox = new HBox(20);
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

                VBox operationsBox = new VBox();

                Button deletePluginButton = new Button("Delete");
                deletePluginButton.setOnMouseClicked(e -> {
                    pluginManager.deletePlugin(pluginDescriptor.getId());
                    restartDialogOpener.confirmRestart("Plugin successfully deleted.");
                });

                operationsBox.getChildren().add(deletePluginButton);
                pluginHbox.getChildren().add(operationsBox);

                pluginsGridPane.add(pluginHbox, 0, index++);
            }
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
