package com.helospark.tactview.ui.javafx.render;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import com.helospark.tactview.core.render.RenderRequest;
import com.helospark.tactview.core.render.RenderServiceChain;
import com.helospark.tactview.core.repository.ProjectRepository;
import com.helospark.tactview.core.timeline.TimelinePosition;

import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class RenderDialog {
    private Stage stage;

    public RenderDialog(List<String> supportedIds, RenderServiceChain renderService, ProjectRepository projectRepository) {
        BorderPane borderPane = new BorderPane();

        GridPane gridPane = new GridPane();
        gridPane.add(new Label("format"), 0, 0);
        Node comboBox = createComboBox(supportedIds);
        gridPane.add(comboBox, 1, 0);

        gridPane.add(new Label("start position"), 0, 1);
        TextField startPositionTextField = new TextField("0");
        gridPane.add(startPositionTextField, 1, 1);

        gridPane.add(new Label("end position"), 0, 2);
        TextField endPositionTextField = new TextField("10.0");
        gridPane.add(endPositionTextField, 1, 2);

        gridPane.add(new Label("Width"), 0, 3);
        TextField widthTextField = new TextField(Integer.toString(projectRepository.getWidth()));
        gridPane.add(widthTextField, 1, 3);

        gridPane.add(new Label("Height"), 0, 4);
        TextField heightTextField = new TextField(Integer.toString(projectRepository.getHeight()));
        gridPane.add(heightTextField, 1, 4);

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        TextField textField = new TextField("/tmp/test.mpeg");
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

        borderPane.setCenter(gridPane);

        HBox buttonBar = new HBox();

        Button cancelButton = new Button("cancel");
        cancelButton.setOnMouseClicked(e -> stage.close());
        hbox.getChildren().add(cancelButton);

        Button okButton = new Button("ok");
        okButton.setOnMouseClicked(e -> {
            RenderRequest request = RenderRequest.builder()
                    .withWidth(Integer.parseInt(widthTextField.getText()))
                    .withHeight(Integer.parseInt(heightTextField.getText()))
                    .withStep(BigDecimal.ONE.divide(projectRepository.getFps(), 100, RoundingMode.HALF_UP))
                    .withStartPosition(new TimelinePosition(new BigDecimal(startPositionTextField.getText())))
                    .withEndPosition(new TimelinePosition(new BigDecimal(endPositionTextField.getText())))
                    .withFileName(textField.getText())
                    .build();
            renderService.render(request);
        });

        hbox.getChildren().add(okButton);

        borderPane.setBottom(buttonBar);

        Scene dialog = new Scene(borderPane);
        stage = new Stage();
        stage.setTitle("Render");
        stage.setScene(dialog);
    }

    public void show() {
        stage.show();
        stage.toFront();
    }

    private Node createComboBox(List<String> supportedIds) {
        return new ComboBox<>(FXCollections.observableArrayList(supportedIds));
    }

}
