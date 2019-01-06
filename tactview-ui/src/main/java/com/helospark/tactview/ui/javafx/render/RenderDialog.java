package com.helospark.tactview.ui.javafx.render;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;

import com.helospark.tactview.core.render.RenderRequest;
import com.helospark.tactview.core.render.RenderServiceChain;
import com.helospark.tactview.core.repository.ProjectRepository;
import com.helospark.tactview.core.timeline.TimelineManager;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.ui.javafx.UiMessagingService;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class RenderDialog {
    private Stage stage;

    public RenderDialog(RenderServiceChain renderService, ProjectRepository projectRepository, UiMessagingService messagingService, TimelineManager timelineManager) {
        BorderPane borderPane = new BorderPane();

        Scene dialog = new Scene(borderPane);
        stage = new Stage();

        GridPane gridPane = new GridPane();
        gridPane.getStyleClass().add("render-dialog-grid-pane");

        gridPane.add(new Label("start position"), 0, 1);
        TextField startPositionTextField = new TextField("0");
        gridPane.add(startPositionTextField, 1, 1);

        gridPane.add(new Label("end position"), 0, 2);
        TextField endPositionTextField = new TextField(timelineManager.findEndPosition().getSeconds().toString());
        gridPane.add(endPositionTextField, 1, 2);

        gridPane.add(new Label("Width"), 0, 3);
        TextField widthTextField = new TextField(Integer.toString(projectRepository.getWidth()));
        gridPane.add(widthTextField, 1, 3);

        gridPane.add(new Label("Height"), 0, 4);
        TextField heightTextField = new TextField(Integer.toString(projectRepository.getHeight()));
        gridPane.add(heightTextField, 1, 4);

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        TextField textField = new TextField("/tmp/test.mp4");
        Button button = new Button("Browse");
        button.setOnMouseClicked(e -> {
            File result = fileChooser.showOpenDialog(stage);
            if (result != null) {
                textField.setText(result.getAbsolutePath());
            }
        });
        HBox hbox = new HBox();
        hbox.getChildren().addAll(textField, button);
        gridPane.add(new Label("File name"), 0, 5);
        gridPane.add(hbox, 1, 5);

        ProgressBar progressBar = new ProgressBar();
        progressBar.setProgress(0);
        progressBar.prefWidthProperty().bind(dialog.widthProperty());
        GridPane.setColumnSpan(progressBar, 2);
        gridPane.add(progressBar, 0, 6);

        borderPane.setCenter(gridPane);

        HBox buttonBar = new HBox();
        button.getStyleClass().add("render-dialog-button-bar");

        Button cancelButton = new Button("Close");
        cancelButton.setOnMouseClicked(e -> stage.close());
        buttonBar.getChildren().add(cancelButton);

        Region emptyRegion = new Region();
        HBox.setHgrow(emptyRegion, Priority.ALWAYS);
        buttonBar.getChildren().add(emptyRegion);

        Button okButton = new Button("Render");
        okButton.setOnMouseClicked(e -> {
            cancelButton.setDisable(true);
            okButton.setDisable(true);

            RenderRequest request = RenderRequest.builder()
                    .withWidth(Integer.parseInt(widthTextField.getText()))
                    .withHeight(Integer.parseInt(heightTextField.getText()))
                    .withStep(BigDecimal.ONE.divide(projectRepository.getFps(), 100, RoundingMode.HALF_UP))
                    .withFps((int) Math.round(projectRepository.getFps().doubleValue()))
                    .withStartPosition(new TimelinePosition(new BigDecimal(startPositionTextField.getText())))
                    .withEndPosition(new TimelinePosition(new BigDecimal(endPositionTextField.getText())))
                    .withFileName(textField.getText())
                    .build();

            String id = request.getRenderId();

            ProgressAdvancer progressAdvancer = new ProgressAdvancer(messagingService, id);
            stage.setTitle("Rendering inprogress...");
            progressAdvancer.updateProgress(progress -> progressBar.setProgress(progress), () -> {
                stage.close();
            });

            renderService.render(request)
                    .thenAccept(a -> {
                        cancelButton.setDisable(false);
                        okButton.setDisable(false);
                    });

        });

        buttonBar.getChildren().add(okButton);

        borderPane.setBottom(buttonBar);

        stage.setTitle("Render");
        stage.setScene(dialog);
    }

    public void show() {
        stage.show();
        stage.toFront();
    }

}
