package com.helospark.tactview.ui.javafx.uicomponents;

import com.helospark.tactview.core.markers.ResettableBean;
import com.helospark.tactview.core.repository.ProjectRepository;
import com.helospark.tactview.ui.javafx.CanvasStateHolder;
import com.helospark.tactview.ui.javafx.DisplayUpdateRequestMessage;
import com.helospark.tactview.ui.javafx.UiMessagingService;
import com.helospark.tactview.ui.javafx.repository.UiProjectRepository;

import javafx.application.Platform;
import javafx.scene.control.ComboBox;

public class ScaleComboBoxFactory implements ResettableBean {
    private final ProjectRepository projectRepository;
    private final UiProjectRepository uiProjectRepository;
    private final DefaultCanvasTranslateSetter defaultCanvasTranslateSetter;
    private final CanvasStateHolder canvasStateHolder;
    private UiMessagingService messagingService;

    private ComboBox<String> sizeDropDown;

    public ScaleComboBoxFactory(ProjectRepository projectRepository, UiProjectRepository uiProjectRepository, UiMessagingService messagingService,
            DefaultCanvasTranslateSetter defaultCanvasTranslateSetter, CanvasStateHolder canvasStateHolder) {
        this.messagingService = messagingService;
        this.projectRepository = projectRepository;
        this.uiProjectRepository = uiProjectRepository;
        this.defaultCanvasTranslateSetter = defaultCanvasTranslateSetter;
        this.canvasStateHolder = canvasStateHolder;
    }

    public ComboBox<String> create() {
        sizeDropDown = new ComboBox<>();
        sizeDropDown.getStyleClass().add("size-drop-down");
        sizeDropDown.getItems().add("10%");
        sizeDropDown.getItems().add("25%");
        sizeDropDown.getItems().add("50%");
        sizeDropDown.getItems().add("75%");
        sizeDropDown.getItems().add("100%");
        sizeDropDown.getItems().add("fit");
        sizeDropDown.getSelectionModel().select("fit");
        sizeDropDown.valueProperty()
                .addListener((o, oldValue, newValue2) -> {
                    refreshDisplay(sizeDropDown);
                });
        canvasStateHolder.getCanvas().widthProperty().addListener((e, oldV, newV) -> {
            refreshDisplay(sizeDropDown);
        });
        canvasStateHolder.getCanvas().heightProperty().addListener((e, oldV, newV) -> {
            refreshDisplay(sizeDropDown);
        });
        return sizeDropDown;
    }

    protected void refreshDisplay(ComboBox<String> sizeDropDown) {
        String newValue = sizeDropDown.getValue();
        int width = projectRepository.getWidth();
        int height = projectRepository.getHeight();

        int previewWidth;
        int previewHeight;
        double scale;

        if (newValue.equals("fit")) {

            double widthToUse = canvasStateHolder.getAvailableWidth();
            double heightToUse = canvasStateHolder.getAvailableHeight();

            double horizontalScaleFactor = widthToUse / projectRepository.getWidth();
            double verticalScaleFactor = heightToUse / projectRepository.getHeight();
            scale = Math.min(horizontalScaleFactor, verticalScaleFactor);
            previewWidth = (int) (scale * width);
            previewHeight = (int) (scale * height);
        } else {
            int percent = Integer.parseInt(newValue.replace("%", ""));

            scale = percent / 100.0;
            previewWidth = (int) (width * scale);
            previewHeight = (int) (height * scale);
        }

        uiProjectRepository.setAlignedPreviewSize(previewWidth, previewHeight, projectRepository.getWidth(), projectRepository.getHeight());
        defaultCanvasTranslateSetter.setDefaultCanvasTranslate(uiProjectRepository.getPreviewWidth(), uiProjectRepository.getPreviewHeight());

        Platform.runLater(() -> {
            messagingService.sendMessage(new DisplayUpdateRequestMessage(true));
        });
    }

    @Override
    public void resetDefaults() {
        sizeDropDown.getSelectionModel().select("fit");
        refreshDisplay(sizeDropDown);
    }

}
