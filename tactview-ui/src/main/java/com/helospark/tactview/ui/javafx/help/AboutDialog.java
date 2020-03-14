package com.helospark.tactview.ui.javafx.help;

import com.helospark.tactview.ui.javafx.stylesheet.StylesheetAdderService;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class AboutDialog {
    private Stage stage;

    public AboutDialog(VBox aboutDialogText, StylesheetAdderService stylesheetAdderService) {
        BorderPane borderPane = new BorderPane();
        borderPane.getStyleClass().add("dialog-root");

        Scene dialog = new Scene(borderPane);
        stage = new Stage();
        stage.setWidth(500);
        stage.setHeight(400);
        stylesheetAdderService.addStyleSheets(borderPane, "stylesheet.css");

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setHbarPolicy(ScrollBarPolicy.NEVER);
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10));
        gridPane.getStyleClass().add("about-dialog-grid-pane");

        gridPane.add(aboutDialogText, 0, 0);
        gridPane.prefWidthProperty().bind(stage.widthProperty());

        scrollPane.setContent(gridPane);

        borderPane.setCenter(scrollPane);

        HBox buttonBar = new HBox();
        buttonBar.getStyleClass().add("render-dialog-button-bar");

        Region emptyRegion = new Region();
        HBox.setHgrow(emptyRegion, Priority.ALWAYS);
        buttonBar.getChildren().add(emptyRegion);

        Button okButton = new Button("Ok");

        buttonBar.getChildren().add(okButton);

        borderPane.setBottom(buttonBar);

        stage.setTitle("About");
        stage.setScene(dialog);

        okButton.setOnMouseClicked(e -> stage.close());
    }

    public void show() {
        stage.show();
        stage.toFront();
    }
}
