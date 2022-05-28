package com.helospark.tactview.ui.javafx.menu.defaultmenus.projectsize;

import java.math.BigDecimal;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.message.DropCachesMessage;
import com.helospark.tactview.core.repository.ProjectRepository;
import com.helospark.tactview.ui.javafx.CanvasStateHolder;
import com.helospark.tactview.ui.javafx.CanvasStates;
import com.helospark.tactview.ui.javafx.UiMessagingService;
import com.helospark.tactview.ui.javafx.repository.UiProjectRepository;
import com.helospark.tactview.ui.javafx.tabs.dockabletab.impl.PreviewDockableTab;
import com.helospark.tactview.ui.javafx.uicomponents.DefaultCanvasTranslateSetter;

@Component
public class ProjectSizeInitializer {
    private ProjectRepository projectRepository;
    private UiProjectRepository uiProjectRepository;
    private UiMessagingService messagingService;
    private DefaultCanvasTranslateSetter defaultCanvasTranslateSetter;
    private CanvasStates canvasStates;

    public ProjectSizeInitializer(ProjectRepository projectRepository, UiProjectRepository uiProjectRepository, UiMessagingService messagingService,
            DefaultCanvasTranslateSetter defaultCanvasTranslateSetter, CanvasStates canvasStates) {
        this.projectRepository = projectRepository;
        this.uiProjectRepository = uiProjectRepository;
        this.messagingService = messagingService;
        this.defaultCanvasTranslateSetter = defaultCanvasTranslateSetter;
        this.canvasStates = canvasStates;
    }

    public void initializeProjectSize(int width, int height, BigDecimal fps) {
        CanvasStateHolder canvasStateHolder = canvasStates.getCanvas(PreviewDockableTab.ID);

        projectRepository.initializeVideo(width, height, fps);
        double horizontalScaleFactor = (canvasStateHolder.getAvailableWidth()) / projectRepository.getWidth();
        double verticalScaleFactor = (canvasStateHolder.getAvailableHeight()) / projectRepository.getHeight();
        double scale = Math.min(horizontalScaleFactor, verticalScaleFactor);
        double aspectRatio = ((double) width) / ((double) height);
        int previewWidth = (int) (scale * width);
        int previewHeight = (int) (scale * height);
        uiProjectRepository.setScaleFactor(scale);
        uiProjectRepository.setAlignedPreviewSize(previewWidth, previewHeight, projectRepository.getWidth(), projectRepository.getHeight());
        defaultCanvasTranslateSetter.setDefaultCanvasTranslate(uiProjectRepository.getPreviewWidth(), uiProjectRepository.getPreviewHeight(), canvasStateHolder);
        uiProjectRepository.setAspectRatio(aspectRatio);
        messagingService.sendAsyncMessage(new RegenerateAllImagePatternsMessage());
        messagingService.sendAsyncMessage(new DropCachesMessage());
    }

}
