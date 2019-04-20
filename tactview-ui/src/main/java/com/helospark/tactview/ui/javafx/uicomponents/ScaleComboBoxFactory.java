package com.helospark.tactview.ui.javafx.uicomponents;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.repository.ProjectRepository;
import com.helospark.tactview.ui.javafx.UiTimelineManager;
import com.helospark.tactview.ui.javafx.repository.UiProjectRepository;

import javafx.scene.control.ComboBox;

@Component
public class ScaleComboBoxFactory {
    private UiTimelineManager uiTimelineManager;
    private ProjectRepository projectRepository;
    private UiProjectRepository uiProjectRepository;

    public ScaleComboBoxFactory(UiTimelineManager uiTimelineManager, ProjectRepository projectRepository, UiProjectRepository uiProjectRepository) {
        this.uiTimelineManager = uiTimelineManager;
        this.projectRepository = projectRepository;
        this.uiProjectRepository = uiProjectRepository;
    }

    public ComboBox<String> create() {
        ComboBox<String> sizeDropDown = new ComboBox<String>();
        sizeDropDown.getStyleClass().add("size-drop-down");
        sizeDropDown.getItems().add("10%");
        sizeDropDown.getItems().add("25%");
        sizeDropDown.getItems().add("50%");
        sizeDropDown.getItems().add("75%");
        sizeDropDown.getItems().add("100%");
        sizeDropDown.getItems().add("fit");
        sizeDropDown.getSelectionModel().select("fit");
        sizeDropDown.valueProperty()
                .addListener((o, oldValue, newValue) -> {
                    int width = projectRepository.getWidth();
                    int height = projectRepository.getHeight();

                    int previewWidth;
                    int previewHeight;
                    double scale;

                    if (newValue.equals("fit")) {
                        double horizontalScaleFactor = 320.0 / projectRepository.getWidth();
                        double verticalScaleFactor = 260.0 / projectRepository.getHeight();
                        scale = Math.min(horizontalScaleFactor, verticalScaleFactor);
                        previewWidth = (int) (scale * width);
                        previewHeight = (int) (scale * height);
                    } else {
                        int percent = Integer.parseInt(newValue.replace("%", ""));

                        scale = percent / 100.0;
                        previewWidth = (int) (width * scale);
                        previewHeight = (int) (height * scale);
                    }
                    uiProjectRepository.setPreviewWidth(previewWidth);
                    uiProjectRepository.setPreviewHeight(previewHeight);
                    uiProjectRepository.setScaleFactor(scale);

                    uiTimelineManager.refresh();
                });
        return sizeDropDown;
    }

}
