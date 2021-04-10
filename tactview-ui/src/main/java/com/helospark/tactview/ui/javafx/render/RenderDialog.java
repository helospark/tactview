package com.helospark.tactview.ui.javafx.render;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import org.apache.commons.io.FilenameUtils;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormat;

import com.helospark.tactview.core.decoder.framecache.GlobalMemoryManagerAccessor;
import com.helospark.tactview.core.optionprovider.OptionProvider;
import com.helospark.tactview.core.render.CreateValueProvidersRequest;
import com.helospark.tactview.core.render.RenderRequest;
import com.helospark.tactview.core.render.RenderService;
import com.helospark.tactview.core.render.RenderServiceChain;
import com.helospark.tactview.core.render.UpdateValueProvidersRequest;
import com.helospark.tactview.core.render.helper.HandledExtensionValueElement;
import com.helospark.tactview.core.repository.ProjectRepository;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.scale.service.ScaleRequest;
import com.helospark.tactview.core.timeline.effect.scale.service.ScaleService;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.ui.javafx.UiMessagingService;
import com.helospark.tactview.ui.javafx.control.ResolutionComponent;
import com.helospark.tactview.ui.javafx.stylesheet.AlertDialogFactory;
import com.helospark.tactview.ui.javafx.stylesheet.StylesheetAdderService;
import com.helospark.tactview.ui.javafx.uicomponents.propertyvalue.ComboBoxElement;
import com.helospark.tactview.ui.javafx.util.ByteBufferToJavaFxImageConverter;
import com.helospark.tactview.ui.javafx.util.DurationFormatter;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class RenderDialog {
    private static final int COLUMNS = 2;
    private static final int ADDITIONAL_OPTIONS_COLUMNS = 3;

    private ProjectRepository projectRepository;
    private AlertDialogFactory alertDialogFactory;

    private Stage stage;
    private boolean isRenderCancelled = false;
    private RenderService previousRenderService = null;
    private Map<String, OptionProvider<?>> optionProviders = Map.of();
    private ByteBufferToJavaFxImageConverter byteBufferToJavaFxImageConverter;
    private ScaleService scaleService;
    private RenderServiceChain renderService;

    TextField fileNameTextField;
    GridPane rendererOptions;
    ResolutionComponent resolutionField;

    TextField startPositionTextField;
    TextField endPositionTextField;
    ComboBox<ComboBoxElement> upscaleField;
    VBox metadataVBox;
    ComboBox<ComboBoxElement> extensionComboBox;

    int previewWidth;
    int previewHeight;
    ImageView previewImageView;
    private long previewLastUpdated = 0;
    CheckBox updatePreviewCheckbox;

    boolean shouldUpdateVisibility = false;
    boolean hasDropdownBeenUpdated = false;

    public RenderDialog(RenderServiceChain renderService, ProjectRepository projectRepository, UiMessagingService messagingService, TimelineManagerAccessor timelineManager,
            StylesheetAdderService stylesheetAdderService, AlertDialogFactory alertDialogFactory, ByteBufferToJavaFxImageConverter byteBufferToJavaFxImageConverter,
            ScaleService scaleService) {
        this.projectRepository = projectRepository;
        this.alertDialogFactory = alertDialogFactory;
        this.byteBufferToJavaFxImageConverter = byteBufferToJavaFxImageConverter;
        this.scaleService = scaleService;
        this.renderService = renderService;

        BorderPane borderPane = new BorderPane();
        borderPane.getStyleClass().add("dialog-root");

        Scene dialog = new Scene(borderPane);
        stylesheetAdderService.addStyleSheets(borderPane, "stylesheet.css");
        stage = new Stage();

        int linePosition = 0;

        VBox fullVerticalBox = new VBox(4.0);

        HBox upperHBox = new HBox(4.0);

        GridPane upperGridPane = new GridPane();
        upperGridPane.setVgap(4.0);
        upperGridPane.setHgap(15.0);
        upperGridPane.getStyleClass().add("render-dialog-grid-pane");

        startPositionTextField = new TextField(DurationFormatter.fromSeconds(BigDecimal.ZERO));
        startPositionTextField.getStyleClass().add("render-dialog-time-selector");
        addGridElement("start position", linePosition++, upperGridPane, startPositionTextField);

        endPositionTextField = new TextField(DurationFormatter.fromSeconds(timelineManager.findEndPosition().getSeconds()));
        endPositionTextField.getStyleClass().add("render-dialog-time-selector");
        addGridElement("end position", linePosition++, upperGridPane, endPositionTextField);

        resolutionField = new ResolutionComponent(projectRepository.getWidth(), projectRepository.getHeight());
        addGridElement("resolution", linePosition++, upperGridPane, resolutionField);

        upscaleField = createComboBox(List.of("1", "2", "4"), 0);
        addGridElement("upscale", linePosition++, upperGridPane, upscaleField);

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
        extensionComboBox = createComboBoxFromValueList(renderService.getCommonHandledExtensions(), "mp4");
        extensionComboBox.setPrefWidth(140.0);
        extensionComboBox.valueProperty().addListener((e, oldValue, newValue) -> {
            String currentValue = fileNameTextField.getText();
            int lastDot = currentValue.lastIndexOf('.');
            HandledExtensionValueElement selectedElement = findSelectedExtensionElementById(newValue.getId());
            if (selectedElement != null) {
                hasDropdownBeenUpdated = true;
                if (lastDot == -1) {
                    fileNameTextField.setText(currentValue + "." + selectedElement.getExtension());
                } else {
                    fileNameTextField.setText(currentValue.substring(0, lastDot) + "." + selectedElement.getExtension());
                }
            }
        });

        GridPane fileNameGrid = new GridPane();

        HBox fileNameHbox = new HBox();
        fileNameHbox.getChildren().add(extensionComboBox);
        fileNameHbox.getChildren().add(button);

        fileNameGrid.add(fileNameTextField, 0, 0);
        fileNameGrid.add(fileNameHbox, 1, 0);

        linePosition += (linePosition % COLUMNS);

        addGridElementForFullLine("File name", linePosition += 2, upperGridPane, fileNameGrid);

        metadataVBox = new VBox();
        TitledPane metadataPane = new TitledPane();
        metadataPane.setExpanded(false);
        metadataPane.setText("metadata table");
        metadataPane.setCollapsible(true);
        metadataPane.setContent(metadataVBox);

        Button addMetadataLine = new Button("Add new metadata");
        metadataVBox.getChildren().add(addMetadataLine);

        addMetadataLine.setOnAction(e -> {
            metadataVBox.getChildren().add(createMetadataBox("", "", a -> metadataVBox.getChildren().remove(a)));
        });
        metadataVBox.getChildren().add(createMetadataBox("Description", "Edited with Tactview", a -> metadataVBox.getChildren().remove(a)));

        addGridElementForFullLine("Metadata", linePosition += 2, upperGridPane, metadataPane);

        linePosition += (linePosition % COLUMNS);

        previewWidth = 250;
        previewHeight = (int) (previewWidth * ((double) projectRepository.getHeight() / projectRepository.getWidth()));

        previewImageView = new ImageView(byteBufferToJavaFxImageConverter.convertToJavafxImage(ByteBuffer.allocate(previewWidth * previewHeight * 4), previewWidth, previewHeight));

        previewImageView.minWidth(previewWidth);
        previewImageView.minHeight(previewHeight);
        previewImageView.maxWidth(previewWidth);
        previewImageView.maxHeight(previewHeight);
        updatePreviewCheckbox = new CheckBox("Update preview");
        updatePreviewCheckbox.setSelected(true);

        VBox previewVBox = new VBox(1.0);
        previewVBox.getChildren().addAll(previewImageView, updatePreviewCheckbox);

        upperHBox.getChildren().addAll(upperGridPane, previewVBox);
        fullVerticalBox.getChildren().add(upperHBox);

        rendererOptions = new GridPane();
        rendererOptions.getStyleClass().add("additional-render-options-panel");
        rendererOptions.setVgap(4.0);
        rendererOptions.setHgap(15.0);

        ProgressBar progressBar = new ProgressBar();
        progressBar.setProgress(0);
        progressBar.prefWidthProperty().bind(dialog.widthProperty());

        Text progressText = new Text();
        progressText.getStyleClass().add("progress-text");

        StackPane stackPane = new StackPane();
        stackPane.getChildren().addAll(progressBar, progressText);

        fullVerticalBox.getChildren().add(rendererOptions);

        ScrollPane centerScrollPane = new ScrollPane(fullVerticalBox);
        centerScrollPane.setHbarPolicy(ScrollBarPolicy.NEVER);
        centerScrollPane.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);

        borderPane.setCenter(centerScrollPane);

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

                RenderRequest request = getRenderRequest();

                String id = request.getRenderId();

                ProgressAdvancer progressAdvancer = new ProgressAdvancer(messagingService, id);
                progressText.setText("Preparing render...");
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
                            alertDialogFactory.showExceptionDialog("Error rendering", ex);
                            cancelButton.setDisable(false);
                            okButton.setDisable(false);
                            return null;
                        });
            }

        });

        fileNameTextField.textProperty().addListener((obj, oldValue, newValue) -> {
            List<HandledExtensionValueElement> commonExtensions = renderService.getCommonHandledExtensions();

            int selectedComboIndex = -1;
            for (int i = 0; i < commonExtensions.size(); ++i) {
                if (commonExtensions.get(i).getExtension().equals(FilenameUtils.getExtension(newValue))) {
                    selectedComboIndex = i;
                    break;
                }
            }

            if (selectedComboIndex != -1 && !hasDropdownBeenUpdated) {
                extensionComboBox.getSelectionModel().select(selectedComboIndex);
            }
            if (!newValue.contains(".")) {
                hasDropdownBeenUpdated = false;
            }

            RenderRequest request = RenderRequest.builder()
                    .withWidth(resolutionField.getResolutionWidth())
                    .withHeight(resolutionField.getResolutionHeight())
                    .withStep(BigDecimal.ONE.divide(projectRepository.getFps(), 100, RoundingMode.HALF_UP))
                    .withFps(projectRepository.getFps())
                    .withStartPosition(new TimelinePosition(DurationFormatter.toSeconds(startPositionTextField.getText())))
                    .withEndPosition(new TimelinePosition(DurationFormatter.toSeconds(endPositionTextField.getText())))
                    .withFileName(fileNameTextField.getText())
                    .withSelectedExtensionType(findSelectedExtensionElement())
                    .withIsCancelledSupplier(() -> isRenderCancelled)
                    .build();

            RenderService currentRenderService = renderService.getRenderer(request);
            if (currentRenderService != previousRenderService) {
                optionProviders = currentRenderService.getOptionProviders(CreateValueProvidersRequest.builder().withFileName(fileNameTextField.getText()).build());
                previousRenderService = currentRenderService;
                updateRenderOptions(rendererOptions);
                updateProvidersAfterUpdate();
            } else {
                updateProvidersAfterUpdate();
            }
        });

        fileNameTextField.setText(System.getProperty("user.home") + File.separator + "video.mp4");

        buttonBar.getChildren().add(okButton);

        VBox bottomVBox = new VBox();
        bottomVBox.getChildren().addAll(stackPane, buttonBar);

        borderPane.setBottom(bottomVBox);

        stage.setTitle("Render");
        stage.setScene(dialog);
        stage.setResizable(false);
    }

    protected RenderRequest getRenderRequest() {
        String fileName = fileNameTextField.getText();
        String extension = FilenameUtils.getExtension(fileName);

        HandledExtensionValueElement selectedExtensionElement = findSelectedExtensionElement();
        if (extension.isEmpty() && selectedExtensionElement != null) {
            if (!fileName.endsWith(".")) {
                fileName += ".";
            }
            fileName += selectedExtensionElement.extension;
        }

        return RenderRequest.builder()
                .withWidth(resolutionField.getResolutionWidth())
                .withHeight(resolutionField.getResolutionHeight())
                .withStep(BigDecimal.ONE.divide(this.projectRepository.getFps(), 100, RoundingMode.HALF_UP))
                .withFps(this.projectRepository.getFps())
                .withStartPosition(new TimelinePosition(DurationFormatter.toSeconds(startPositionTextField.getText())))
                .withEndPosition(new TimelinePosition(DurationFormatter.toSeconds(endPositionTextField.getText())))
                .withFileName(fileName)
                .withOptions(optionProviders)
                .withIsCancelledSupplier(() -> isRenderCancelled)
                .withUpscale(new BigDecimal(upscaleField.getSelectionModel().getSelectedItem().getId()))
                .withMetadata(collectMetadata(metadataVBox))
                .withSelectedExtensionType(selectedExtensionElement)
                .withEncodedImageCallback(image -> updatePreviewConsumer(image))
                .build();
    }

    private HandledExtensionValueElement findSelectedExtensionElement() {
        String selectedId = extensionComboBox.getSelectionModel().getSelectedItem().getId();
        return findSelectedExtensionElementById(selectedId);
    }

    private HandledExtensionValueElement findSelectedExtensionElementById(String selectedId) {
        List<HandledExtensionValueElement> commonExtensions = renderService.getCommonHandledExtensions();
        for (int i = 0; i < commonExtensions.size(); ++i) {
            if (commonExtensions.get(i).getId().equals(selectedId)) {
                return commonExtensions.get(i);
            }
        }
        return null;
    }

    private Map<String, String> collectMetadata(VBox metadataVBox) {
        Map<String, String> result = new LinkedHashMap<>();
        for (int i = 1; i < metadataVBox.getChildren().size(); ++i) {
            // TODO: bit too much dependence on UI layout
            String key = ((TextField) (((HBox) metadataVBox.getChildren().get(i)).getChildren().get(0))).getText();
            String value = ((TextField) (((HBox) metadataVBox.getChildren().get(i)).getChildren().get(1))).getText();
            result.put(key, value);
        }
        return result;
    }

    private void updatePreviewConsumer(ReadOnlyClipImage image) {
        long now = System.currentTimeMillis();
        if ((now - previewLastUpdated) > 1000 && updatePreviewCheckbox.isSelected()) { // small chance of doule update, but that's ok
            previewLastUpdated = now;
            ScaleRequest request = ScaleRequest.builder()
                    .withImage(image)
                    .withNewWidth(previewWidth)
                    .withNewHeight(previewHeight)
                    .build();
            ClipImage scaledImage = scaleService.createScaledImage(request);
            Image javafxImage = byteBufferToJavaFxImageConverter.convertToJavafxImage(scaledImage.getBuffer(), scaledImage.getWidth(), scaledImage.getHeight());
            Platform.runLater(() -> {
                previewImageView.setImage(javafxImage);
                GlobalMemoryManagerAccessor.memoryManager.returnBuffer(scaledImage.getBuffer());
            });
        }
    }

    private HBox createMetadataBox(String key, String value, Consumer<HBox> removeConsumer) {
        HBox metadataHBox = new HBox();
        TextField metadataKey = new TextField(key);
        TextField metadataValue = new TextField(value);
        Button removeButton = new Button("x");
        metadataHBox.getChildren().addAll(metadataKey, metadataValue, removeButton);
        removeButton.setOnAction(e -> {
            removeConsumer.accept(metadataHBox);
        });
        return metadataHBox;
    }

    private boolean showConfirmationDialogIfNeeded(String filePath) {
        boolean continueRender = true;
        if (new File(filePath).exists()) {
            Alert alert = alertDialogFactory.createSimpleAlertWithTitleAndContent(AlertType.CONFIRMATION, "File exists, override?", "File \"" + filePath + "\" already exists.\nOverride?");

            Optional<ButtonType> result = alert.showAndWait();
            continueRender = result.map(buttonType -> buttonType.equals(ButtonType.OK)).orElse(true);
        }
        return continueRender;
    }

    private void updateRenderOptions(GridPane rendererOptions) {
        rendererOptions.getChildren().clear();

        Collection<OptionProvider<?>> values = optionProviders.values();

        RenderRequest renderRequest = getRenderRequest();

        // TODO: all the below out of here
        int i = 0;
        for (var value : values) {
            int row = i / ADDITIONAL_OPTIONS_COLUMNS;
            int column = i % ADDITIONAL_OPTIONS_COLUMNS;

            OptionProvider<Object> optionProvider = (OptionProvider<Object>) value;

            if (!isOptionProviderVisible(optionProvider, renderRequest)) {
                continue;
            }

            Label label = new Label(optionProvider.getTitle());
            rendererOptions.add(label, column * ADDITIONAL_OPTIONS_COLUMNS, row);

            if (optionProvider.getValidValues().isEmpty()) {
                TextField textField = new TextField();
                textField.getStyleClass().add("render-dialog-option");
                textField.setText(String.valueOf(optionProvider.getValue()));
                textField.disableProperty().set(isOptionProviderDisabled(optionProvider, renderRequest));
                textField.textProperty().addListener((obj2, oldValue2, newValue2) -> {
                    Object parsedValue = optionProvider.getValueConverter().apply(newValue2);
                    // TODO: isValid, etc.
                    optionProvider.setValue(parsedValue);
                    updateProvidersAfterUpdate();
                });

                rendererOptions.add(textField, column * ADDITIONAL_OPTIONS_COLUMNS + 1, row);
            } else {
                ComboBox<ComboBoxElement> comboBox = new ComboBox<>();
                comboBox.getStyleClass().add("render-dialog-option");
                comboBox.disableProperty().set(isOptionProviderDisabled(optionProvider, renderRequest));
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
                    updateProvidersAfterUpdate();
                });

                rendererOptions.add(comboBox, column * ADDITIONAL_OPTIONS_COLUMNS + 1, row);
            }

            ++i;
        }
    }

    private boolean isOptionProviderVisible(OptionProvider<Object> optionProvider, RenderRequest renderRequest) {
        return optionProvider.getShouldShow().apply(renderRequest);
    }

    protected boolean isOptionProviderDisabled(OptionProvider<Object> optionProvider, RenderRequest renderRequest) {
        return !optionProvider.getIsEnabled().apply(renderRequest);
    }

    private void updateProvidersAfterUpdate() {
        UpdateValueProvidersRequest request = UpdateValueProvidersRequest.builder()
                .withFileName(fileNameTextField.getText())
                .withOptions(cloneProviders(optionProviders))
                .build();
        Map<String, OptionProvider<?>> updatedValues = previousRenderService.updateValueProviders(request);

        if (!updatedValues.equals(optionProviders)) {
            this.optionProviders = updatedValues;
            updateRenderOptions(rendererOptions);
        }
    }

    private Map<String, OptionProvider<?>> cloneProviders(Map<String, OptionProvider<?>> optionProviders2) {
        Map<String, OptionProvider<?>> result = new LinkedHashMap<>();
        for (var entry : optionProviders2.entrySet()) {
            result.put(entry.getKey(), entry.getValue().deepClone());
        }
        return result;
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

    public ComboBox<ComboBoxElement> createComboBoxFromValueList(List<HandledExtensionValueElement> values, String string) {
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

    private void addGridElementForFullLine(String name, int linePosition, GridPane gridPane, Node node) {
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
