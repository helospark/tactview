package com.helospark.tactview.ui.javafx.render;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import com.helospark.tactview.core.optionprovider.OptionProvider;
import com.helospark.tactview.core.render.RenderRequest;
import com.helospark.tactview.core.render.RenderService;
import com.helospark.tactview.core.render.RenderServiceChain;
import com.helospark.tactview.core.repository.ProjectRepository;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.ui.javafx.UiMessagingService;
import com.helospark.tactview.ui.javafx.uicomponents.propertyvalue.ComboBoxElement;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
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
    private boolean isRenderCancelled = false;
    private RenderService previousRenderService = null;
    private Map<String, OptionProvider<?>> optionProviders = Map.of();

    public RenderDialog(RenderServiceChain renderService, ProjectRepository projectRepository, UiMessagingService messagingService, TimelineManagerAccessor timelineManager) {
        BorderPane borderPane = new BorderPane();

        Scene dialog = new Scene(borderPane);
        stage = new Stage();

        int linePosition = 1;

        GridPane gridPane = new GridPane();
        gridPane.getStyleClass().add("render-dialog-grid-pane");

        TextField startPositionTextField = new TextField("0");
        addGridElement("start position", linePosition++, gridPane, startPositionTextField);

        TextField endPositionTextField = new TextField(timelineManager.findEndPosition().getSeconds().toString());
        addGridElement("end position", linePosition++, gridPane, endPositionTextField);

        TextField widthTextField = new TextField(Integer.toString(projectRepository.getWidth()));
        addGridElement("width", linePosition++, gridPane, widthTextField);

        TextField heightTextField = new TextField(Integer.toString(projectRepository.getHeight()));
        addGridElement("height", linePosition++, gridPane, heightTextField);

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        TextField fileNameTextField = new TextField();
        Button button = new Button("Browse");
        button.setOnMouseClicked(e -> {
            File result = fileChooser.showOpenDialog(stage);
            if (result != null) {
                fileNameTextField.setText(result.getAbsolutePath());
            }
        });
        HBox hbox = new HBox();
        hbox.getChildren().addAll(fileNameTextField, button);

        addGridElement("File name", linePosition++, gridPane, hbox);

        TextField upscaleField = new TextField("4");
        addGridElement("upscale", linePosition++, gridPane, upscaleField);

        GridPane rendererOptions = new GridPane();
        gridPane.add(rendererOptions, 0, linePosition++, 2, 1);

        ProgressBar progressBar = new ProgressBar();
        progressBar.setProgress(0);
        progressBar.prefWidthProperty().bind(dialog.widthProperty());
        GridPane.setColumnSpan(progressBar, 2);
        gridPane.add(progressBar, 0, linePosition++);

        borderPane.setCenter(gridPane);

        HBox buttonBar = new HBox();
        buttonBar.getStyleClass().add("render-dialog-button-bar");

        Button cancelButton = new Button("Close");
        cancelButton.setOnMouseClicked(e -> {
            isRenderCancelled = true;
            stage.close();
        });
        buttonBar.getChildren().add(cancelButton);

        Region emptyRegion = new Region();
        HBox.setHgrow(emptyRegion, Priority.ALWAYS);
        buttonBar.getChildren().add(emptyRegion);

        Button okButton = new Button("Render");
        okButton.setOnMouseClicked(e -> {
            cancelButton.setText("Cancel render");
            okButton.setDisable(true);

            RenderRequest request = RenderRequest.builder()
                    .withWidth(Integer.parseInt(widthTextField.getText()))
                    .withHeight(Integer.parseInt(heightTextField.getText()))
                    .withStep(BigDecimal.ONE.divide(projectRepository.getFps(), 100, RoundingMode.HALF_UP))
                    .withFps((int) Math.round(projectRepository.getFps().doubleValue()))
                    .withStartPosition(new TimelinePosition(new BigDecimal(startPositionTextField.getText())))
                    .withEndPosition(new TimelinePosition(new BigDecimal(endPositionTextField.getText())))
                    .withFileName(fileNameTextField.getText())
                    .withOptions(optionProviders)
                    .withIsCancelledSupplier(() -> isRenderCancelled)
                    .withUpscale(new BigDecimal(upscaleField.getText()))
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

        fileNameTextField.textProperty().addListener((obj, oldValue, newValue) -> {
            RenderRequest request = RenderRequest.builder()
                    .withWidth(Integer.parseInt(widthTextField.getText()))
                    .withHeight(Integer.parseInt(heightTextField.getText()))
                    .withStep(BigDecimal.ONE.divide(projectRepository.getFps(), 100, RoundingMode.HALF_UP))
                    .withFps((int) Math.round(projectRepository.getFps().doubleValue()))
                    .withStartPosition(new TimelinePosition(new BigDecimal(startPositionTextField.getText())))
                    .withEndPosition(new TimelinePosition(new BigDecimal(endPositionTextField.getText())))
                    .withFileName(fileNameTextField.getText())
                    .withIsCancelledSupplier(() -> isRenderCancelled)
                    .build();

            RenderService currentRenderService = renderService.getRenderer(request);
            if (currentRenderService != previousRenderService) {
                optionProviders = currentRenderService.getOptionProviders();
                previousRenderService = currentRenderService;
                rendererOptions.getChildren().clear();

                Collection<OptionProvider<?>> values = optionProviders.values();

                // TODO: all the below out of here
                int i = 0;
                for (var value : values) {
                    OptionProvider<Object> optionProvider = (OptionProvider<Object>) value;
                    Label label = new Label(optionProvider.getTitle());
                    rendererOptions.add(label, 0, i);

                    if (optionProvider.getValidValues().isEmpty()) {
                        TextField textField = new TextField();
                        textField.setText(String.valueOf(optionProvider.getValue()));
                        textField.textProperty().addListener((obj2, oldValue2, newValue2) -> {
                            Object parsedValue = optionProvider.getValueConverter().apply(newValue2);
                            // TODO: isValid, etc.
                            optionProvider.setValue(parsedValue);
                        });
                        rendererOptions.add(textField, 1, i);
                    } else {
                        ComboBox<ComboBoxElement> comboBox = new ComboBox<>();
                        Map<String, ComboBoxElement> comboBoxElements = new LinkedHashMap<>();

                        optionProvider.getValidValues()
                                .stream()
                                .forEach(a -> {
                                    var entry = new ComboBoxElement(a.getId(), a.getText());
                                    comboBox.getItems().add(entry);
                                    comboBoxElements.put(a.getId(), entry);
                                });
                        comboBox.getSelectionModel().select(comboBoxElements.get(optionProvider.getValue().toString()));

                        comboBox.setOnAction(e -> {
                            optionProvider.setValue(comboBox.getValue().getId());
                        });
                        rendererOptions.add(comboBox, 1, i);
                    }

                    ++i;
                }

            }
        });

        fileNameTextField.setText("/tmp/test.mp4");

        buttonBar.getChildren().add(okButton);

        borderPane.setBottom(buttonBar);

        stage.setTitle("Render");
        stage.setScene(dialog);
    }

    private void addGridElement(String name, int linePosition, GridPane gridPane, Node node) {
        gridPane.add(new Label(name), 0, linePosition);
        gridPane.add(node, 1, linePosition);
    }

    public void show() {
        stage.show();
        stage.toFront();
    }

}
