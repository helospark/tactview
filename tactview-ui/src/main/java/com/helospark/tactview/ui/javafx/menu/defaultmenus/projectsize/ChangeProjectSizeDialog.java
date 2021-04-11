package com.helospark.tactview.ui.javafx.menu.defaultmenus.projectsize;

import java.math.BigDecimal;

import com.helospark.tactview.core.repository.ProjectRepository;
import com.helospark.tactview.ui.javafx.control.ResolutionComponent;
import com.helospark.tactview.ui.javafx.stylesheet.AlertDialogFactory;
import com.helospark.tactview.ui.javafx.stylesheet.StylesheetAdderService;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

public class ChangeProjectSizeDialog {
    private Stage stage;

    public ChangeProjectSizeDialog(ProjectSizeInitializer projectSizeInitializer, ProjectRepository projectRepository, StylesheetAdderService stylesheetAdderService,
            AlertDialogFactory alertDialogFactory) {
        BorderPane borderPane = new BorderPane();
        borderPane.getStyleClass().add("dialog-root");

        Scene dialog = new Scene(borderPane);
        stage = new Stage();
        stage.setWidth(300);
        stage.setHeight(250);
        stylesheetAdderService.styleDialog(stage, borderPane, "stylesheet.css");

        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10));
        gridPane.getStyleClass().add("change-project-size-dialog-grid-pane");
        gridPane.prefWidthProperty().bind(stage.widthProperty());

        int initialWidth = projectRepository.isVideoInitialized() ? projectRepository.getWidth() : 1920;
        int initialHeight = projectRepository.isVideoInitialized() ? projectRepository.getHeight() : 1080;
        ResolutionComponent resolutionComponent = new ResolutionComponent(initialWidth, initialHeight);

        gridPane.add(new Label("Resolution"), 0, 0);
        gridPane.add(resolutionComponent, 1, 0);

        TextField fpsText = new TextField(getOrDefaultVideo(projectRepository, projectRepository.getFps(), BigDecimal.valueOf(30)));
        gridPane.add(new Label("FPS"), 0, 2);
        gridPane.add(fpsText, 1, 2);

        TextField sampleRateText = new TextField(getOrDefaultAudio(projectRepository, projectRepository.getSampleRate(), BigDecimal.valueOf(44100)));
        gridPane.add(new Label("Sample rate"), 0, 3);
        gridPane.add(sampleRateText, 1, 3);

        TextField bytesPerSampleText = new TextField(getOrDefaultAudio(projectRepository, projectRepository.getBytesPerSample(), BigDecimal.valueOf(4)));
        gridPane.add(new Label("Bytes/sample"), 0, 4);
        gridPane.add(bytesPerSampleText, 1, 4);

        TextField numberOfChannelsText = new TextField(getOrDefaultAudio(projectRepository, projectRepository.getNumberOfChannels(), BigDecimal.valueOf(4)));
        gridPane.add(new Label("Number of channels"), 0, 5);
        gridPane.add(numberOfChannelsText, 1, 5);

        borderPane.setCenter(gridPane);

        HBox buttonBar = new HBox();
        buttonBar.getStyleClass().add("render-dialog-button-bar");

        Region emptyRegion = new Region();
        HBox.setHgrow(emptyRegion, Priority.ALWAYS);

        Button okButton = new Button("Ok");

        Button cancelButton = new Button("Cancel");

        buttonBar.getChildren().add(cancelButton);
        buttonBar.getChildren().add(emptyRegion);
        buttonBar.getChildren().add(okButton);

        borderPane.setBottom(buttonBar);

        stage.setTitle("Change render size");
        stage.setScene(dialog);

        okButton.setOnMouseClicked(e -> {
            int width = resolutionComponent.getResolutionWidth();
            int height = resolutionComponent.getResolutionHeight();
            BigDecimal fps = new BigDecimal(fpsText.getText());
            int numberOfChannels = Integer.parseInt(numberOfChannelsText.getText());

            if (numberOfChannels < 1 || numberOfChannels > 7) {
                Alert alert = alertDialogFactory.createSimpleAlertWithTitleAndContent(AlertType.WARNING, "Invalid number of channels",
                        "Channel number must be between 1 and 7, but was " + numberOfChannels);
                alert.showAndWait();
                return;
            }

            projectSizeInitializer.initializeProjectSize(width, height, fps);

            projectRepository.initializeAudio(Integer.parseInt(sampleRateText.getText()), Integer.parseInt(bytesPerSampleText.getText()), numberOfChannels);
            stage.close();
        });
        cancelButton.setOnMouseClicked(e -> {
            stage.close();
        });
    }

    private String getOrDefaultVideo(ProjectRepository projectRepository, Object currentValue, Object defaultValue) {
        if (projectRepository.isVideoInitialized()) {
            return currentValue + "";
        } else {
            return defaultValue + "";
        }
    }

    private String getOrDefaultAudio(ProjectRepository projectRepository, Object currentValue, Object defaultValue) {
        if (projectRepository.isAudioInitialized()) {
            return currentValue + "";
        } else {
            return defaultValue + "";
        }
    }

    public void show() {
        stage.show();
        stage.toFront();
    }

}
