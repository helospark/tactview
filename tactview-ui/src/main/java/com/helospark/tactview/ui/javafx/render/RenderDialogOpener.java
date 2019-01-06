package com.helospark.tactview.ui.javafx.render;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.render.RenderServiceChain;
import com.helospark.tactview.core.repository.ProjectRepository;
import com.helospark.tactview.ui.javafx.UiMessagingService;

@Component
public class RenderDialogOpener {
    private RenderServiceChain renderService;
    private ProjectRepository projectRepository;
    private UiMessagingService messagingService;

    public RenderDialogOpener(RenderServiceChain renderService, ProjectRepository projectRepository, UiMessagingService messagingService) {
        this.renderService = renderService;
        this.projectRepository = projectRepository;
        this.messagingService = messagingService;
    }

    public void render() {
        RenderDialog renderDialog = new RenderDialog(renderService, projectRepository, messagingService);
        renderDialog.show();
    }

}
