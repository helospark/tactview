package com.helospark.tactview.ui.javafx.render;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.joda.time.Period;
import org.joda.time.format.PeriodFormat;

import com.helospark.tactview.core.optionprovider.OptionProvider;
import com.helospark.tactview.core.render.CreateValueProvidersRequest;
import com.helospark.tactview.core.render.RenderRequest;
import com.helospark.tactview.core.render.RenderService;
import com.helospark.tactview.core.render.RenderServiceChain;
import com.helospark.tactview.core.render.UpdateValueProvidersRequest;
import com.helospark.tactview.core.repository.ProjectRepository;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ValueListElement;
import com.helospark.tactview.ui.javafx.UiMessagingService;
import com.helospark.tactview.ui.javafx.uicomponents.propertyvalue.ComboBoxElement;
import com.helospark.tactview.ui.javafx.util.DialogHelper;
import com.helospark.tactview.ui.javafx.util.DurationFormatter;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class RenderDialog {
    private static final int COLUMNS = 2;

    private Stage stage;
    private boolean isRenderCancelled = false;
    private RenderService previousRenderService = null;
    private Map<String, OptionProvider<?>> optionProviders = Map.of();

    TextField fileNameTextField;
    GridPane rendererOptions;

    public RenderDialog(RenderServiceChain renderService, ProjectRepository projectRepository, UiMessagingService messagingService, TimelineManagerAccessor timelineManager) {
        BorderPane borderPane = new BorderPane();
        borderPane.getStyleClass().add("dialog-root");

        Scene dialog = new Scene(borderPane);
        dialog.getStylesheets().add("stylesheet.css");
        stage = new Stage();

        int linePosition = 0;

        GridPane gridPane = new GridPane();
        gridPane.setVgap(4.0);
        gridPane.setHgap(15.0);
        gridPane.getStyleClass().add("render-dialog-grid-pane");

        TextField startPositionTextField = new TextField(DurationFormatter.fromSeconds(BigDecimal.ZERO));
        addGridElement("start position", linePosition++, gridPane, startPositionTextField);

        TextField endPositionTextField = new TextField(DurationFormatter.fromSeconds(timelineManager.findEndPosition().getSeconds()));
        addGridElement("end position", linePosition++, gridPane, endPositionTextField);

        TextField widthTextField = new TextField(Integer.toString(projectRepository.getWidth()));
        addGridElement("width", linePosition++, gridPane, widthTextField);

        TextField heightTextField = new TextField(Integer.toString(projectRepository.getHeight()));
        addGridElement("height", linePosition++, gridPane, heightTextField);

        ComboBox<ComboBoxElement> upscaleField = createComboBox(List.of("1", "2", "4"), 0);
        addGridElement("upscale", linePosition++, gridPane, upscaleField);

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        fileNameTextField = new TextField();
        GridPane.setHgrow(fileNameTextField, Priority.ALWAYS);

        Button button = new Button("Browse");

        button.setOnMouseClicked(e -> {
            File currentFile = new File(fileNameTextField.getText());
            fileChooser.setInitialDirectory(currentFile.getParentFile());
            fileChooser.setInitialFileName(currentFile.getName());
            File result = fileChooser.showSaveDialog(stage);
            if (result != null) {
                fileNameTextField.setText(result.getAbsolutePath());
            }
        });
        ComboBox<ComboBoxElement> extensionComboBox = createComboBoxFromValueList(renderService.getCommonHandledExtensions(), "mp4");
        extensionComboBox.setPrefWidth(80.0);
        extensionComboBox.valueProperty().addListener((e, oldValue, newValue) -> {
            String currentValue = fileNameTextField.getText();
            int lastDot = currentValue.lastIndexOf('.');
            if (lastDot == -1) {
                fileNameTextField.setText(currentValue + "." + newValue.getId());
            } else {
                fileNameTextField.setText(currentValue.substring(0, lastDot) + "." + newValue.getId());
            }
        });

        GridPane fileNameGrid = new GridPane();

        HBox fileNameHbox = new HBox();
        fileNameHbox.getChildren().add(extensionComboBox);
        fileNameHbox.getChildren().add(button);

        fileNameGrid.add(fileNameTextField, 0, 0);
        fileNameGrid.add(fileNameHbox, 1, 0);

        linePosition += (linePosition % COLUMNS);

        addGridElementForFullLine("File name", linePosition++, gridPane, fileNameGrid);

        linePosition += (linePosition % COLUMNS);

        rendererOptions = new GridPane();
        rendererOptions.setVgap(4.0);
        rendererOptions.setHgap(15.0);
        gridPane.add(rendererOptions, 0, linePosition++, COLUMNS * 2, 1);

        ProgressBar progressBar = new ProgressBar();
        progressBar.setProgress(0);
        progressBar.prefWidthProperty().bind(dialog.widthProperty());

        Text progressText = new Text();
        progressText.getStyleClass().add("progress-text");

        StackPane stackPane = new StackPane();
        stackPane.getChildren().addAll(progressBar, progressText);

        GridPane.setColumnSpan(stackPane, COLUMNS * 2);
        gridPane.add(stackPane, 0, linePosition++);

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
            String filePath = fileNameTextField.getText();
            boolean continueRender = showConfirmationDialogIfNeeded(filePath);

            if (continueRender) {
                cancelButton.setText("Cancel render");
                okButton.setDisable(true);

                RenderRequest request = RenderRequest.builder()
                        .withWidth(Integer.parseInt(widthTextField.getText()))
                        .withHeight(Integer.parseInt(heightTextField.getText()))
                        .withStep(BigDecimal.ONE.divide(projectRepository.getFps(), 100, RoundingMode.HALF_UP))
                        .withFps((int) Math.round(projectRepository.getFps().doubleValue()))
                        .withStartPosition(new TimelinePosition(DurationFormatter.toSeconds(startPositionTextField.getText())))
                        .withEndPosition(new TimelinePosition(DurationFormatter.toSeconds(endPositionTextField.getText())))
                        .withFileName(filePath)
                        .withOptions(optionProviders)
                        .withIsCancelledSupplier(() -> isRenderCancelled)
                        .withUpscale(new BigDecimal(upscaleField.getSelectionModel().getSelectedItem().getId()))
                        .build();

                String id = request.getRenderId();

                ProgressAdvancer progressAdvancer = new ProgressAdvancer(messagingService, id);
                stage.setTitle("Rendering inprogress...");
                progressAdvancer.updateProgress(info -> {
                    progressBar.setProgress(info.percent);
                    String formattedRemaining = PeriodFormat.getDefault().print(new Period(info.expectedMilliseconds).withMillis(0));

                    progressText.setText(String.format("%d%% (remaining: %s, fps: %.3f)", (int) (info.percent * 100.0), formattedRemaining, info.jobsPerSecond));
                }, () -> {
                    stage.close();
                });

                renderService.render(request)
                        .thenAccept(a -> {
                            cancelButton.setDisable(false);
                            okButton.setDisable(false);
                        })
                        .exceptionally(ex -> {
                            ex.printStackTrace();
                            DialogHelper.showExceptionDialog("Error rendering", "Unable to render to file, see stacktrace below, more details in logs", ex);
                            cancelButton.setDisable(false);
                            okButton.setDisable(false);
                            return null;
                        });
            }

        });

        fileNameTextField.textProperty().addListener((obj, oldValue, newValue) -> {
            RenderRequest request = RenderRequest.builder()
                    .withWidth(Integer.parseInt(widthTextField.getText()))
                    .withHeight(Integer.parseInt(heightTextField.getText()))
                    .withStep(BigDecimal.ONE.divide(projectRepository.getFps(), 100, RoundingMode.HALF_UP))
                    .withFps((int) Math.round(projectRepository.getFps().doubleValue()))
                    .withStartPosition(new TimelinePosition(DurationFormatter.toSeconds(startPositionTextField.getText())))
                    .withEndPosition(new TimelinePosition(DurationFormatter.toSeconds(endPositionTextField.getText())))
                    .withFileName(fileNameTextField.getText())
                    .withIsCancelledSupplier(() -> isRenderCancelled)
                    .build();

            RenderService currentRenderService = renderService.getRenderer(request);
            if (currentRenderService != previousRenderService) {
                optionProviders = currentRenderService.getOptionProviders(CreateValueProvidersRequest.builder().withFileName(fileNameTextField.getText()).build());
                previousRenderService = currentRenderService;
                updateRenderOptions(rendererOptions);
            } else {
                updateProvidersAfterUpdate();
            }
        });

        fileNameTextField.setText(System.getProperty("user.home") + File.separator + "video.mp4");

        buttonBar.getChildren().add(okButton);

        borderPane.setBottom(buttonBar);

        stage.setTitle("Render");
        stage.setScene(dialog);
    }

    private boolean showConfirmationDialogIfNeeded(String filePath) {
        boolean continueRender = true;
        if (new File(filePath).exists()) {
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("File exists, override?");
            alert.setHeaderText("File exists \"" + filePath + "\".\nOverride?");

            Optional<ButtonType> result = alert.showAndWait();
            continueRender = result.map(buttonType -> buttonType.equals(ButtonType.OK)).orElse(true);
        }
        return continueRender;
    }

    private void updateRenderOptions(GridPane rendererOptions) {
        rendererOptions.getChildren().clear();

        Collection<OptionProvider<?>> values = optionProviders.values();

        // TODO: all the below out of here
        int i = 0;
        for (var value : values) {
            int row = i / COLUMNS;
            int column = i % COLUMNS;

            OptionProvider<Object> optionProvider = (OptionProvider<Object>) value;
            Label label = new Label(optionProvider.getTitle());
            rendererOptions.add(label, column * COLUMNS, row);

            if (optionProvider.getValidValues().isEmpty()) {
                TextField textField = new TextField();
                textField.getStyleClass().add("render-dialog-option");
                textField.setText(String.valueOf(optionProvider.getValue()));
                textField.textProperty().addListener((obj2, oldValue2, newValue2) -> {
                    Object parsedValue = optionProvider.getValueConverter().apply(newValue2);
                    // TODO: isValid, etc.
                    optionProvider.setValue(parsedValue);
                    if (optionProvider.shouldTriggerUpdate()) {
                        updateProvidersAfterUpdate();
                    }
                });

                rendererOptions.add(textField, column * COLUMNS + 1, row);
            } else {
                ComboBox<ComboBoxElement> comboBox = new ComboBox<>();
                comboBox.getStyleClass().add("render-dialog-option");
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
                    if (optionProvider.shouldTriggerUpdate()) {
                        updateProvidersAfterUpdate();
                    }
                });

                rendererOptions.add(comboBox, column * COLUMNS + 1, row);
            }

            ++i;
        }
    }

    private void updateProvidersAfterUpdate() {
        UpdateValueProvidersRequest request = UpdateValueProvidersRequest.builder()
                .withFileName(fileNameTextField.getText())
                .withOptions(optionProviders)
                .build();
        Map<String, OptionProvider<?>> updatedValues = previousRenderService.updateValueProviders(request);

        if (!updatedValues.equals(optionProviders)) {
            this.optionProviders = updatedValues;
            updateRenderOptions(rendererOptions);
        }
    }

    public ComboBox<ComboBoxElement> createComboBox(List<String> values, int selectedIndex) {
        ComboBox<ComboBoxElement> comboBox = new ComboBox<>();
        values
                .stream()
                .forEach(a -> {
                    var entry = new ComboBoxElement(a, a);
                    comboBox.getItems().add(entry);
                });
        comboBox.getSelectionModel().select(selectedIndex);
        return comboBox;
    }

    public ComboBox<ComboBoxElement> createComboBoxFromValueList(List<ValueListElement> values, String string) {
        ComboBox<ComboBoxElement> comboBox = new ComboBox<>();
        values
                .stream()
                .forEach(a -> {
                    var entry = new ComboBoxElement(a.getId(), a.getText());
                    comboBox.getItems().add(entry);
                });
        for (int i = 0; i < values.size(); ++i) {
            if (values.get(i).getId().equals(string)) {
                comboBox.getSelectionModel().select(i);
                break;
            }
        }
        return comboBox;
    }

    private void addGridElement(String name, int linePosition, GridPane gridPane, Node node) {
        int row = linePosition / COLUMNS;
        int column = linePosition % COLUMNS;

        gridPane.add(new Label(name), column * COLUMNS, row);
        node.getStyleClass().add("render-dialog-option");
        gridPane.add(node, column * COLUMNS + 1, row);
    }

    private void addGridElementForFullLine(String name, int linePosition, GridPane gridPane, GridPane node) {
        int row = linePosition / COLUMNS;
        int column = linePosition % COLUMNS;

        gridPane.add(new Label(name), column * COLUMNS, row);
        gridPane.add(node, column * COLUMNS + 1, row, COLUMNS * 2, 1);
    }

    public void show() {
        stage.show();
        stage.toFront();
    }

}
