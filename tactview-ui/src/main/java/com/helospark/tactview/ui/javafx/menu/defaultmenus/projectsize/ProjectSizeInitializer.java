package com.helospark.tactview.ui.javafx.menu.defaultmenus.projectsize;

import java.math.BigDecimal;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.message.DropCachesMessage;
import com.helospark.tactview.core.repository.ProjectRepository;
import com.helospark.tactview.ui.javafx.UiMessagingService;
import com.helospark.tactview.ui.javafx.repository.UiProjectRepository;

@Component
public class ProjectSizeInitializer {
    private ProjectRepository projectRepository;
    private UiProjectRepository uiProjectRepository;
    private UiMessagingService messagingService;

    public ProjectSizeInitializer(ProjectRepository projectRepository, UiProjectRepository uiProjectRepository, UiMessagingService messagingService) {
        this.projectRepository = projectRepository;
        this.uiProjectRepository = uiProjectRepository;
        this.messagingService = messagingService;
    }

    public void initializeProjectSize(int width, int height, BigDecimal fps) {
        projectRepository.initializeVideo(width, height, fps);
        double horizontalScaleFactor = 320.0 / projectRepository.getWidth();
        double verticalScaleFactor = 260.0 / projectRepository.getHeight();
        double scale = Math.min(horizontalScaleFactor, verticalScaleFactor);
        double aspectRatio = ((double) width) / ((double) height);
        int previewWidth = (int) (scale * width);
        int previewHeight = (int) (scale * height);
        uiProjectRepository.setScaleFactor(scale);
        uiProjectRepository.setPreviewWidth(previewWidth);
        uiProjectRepository.setPreviewHeight(previewHeight);
        uiProjectRepository.setAspectRatio(aspectRatio);
        messagingService.sendAsyncMessage(new RegenerateAllImagePatternsMessage());
        messagingService.sendAsyncMessage(new DropCachesMessage());
    }

}
