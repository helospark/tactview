package com.helospark.tactview.ui.javafx.clip.chain;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.render.proxy.LowResolutionProxyCreatorService;
import com.helospark.tactview.core.timeline.VideoClip;
import com.helospark.tactview.ui.javafx.repository.UiProjectRepository;

import javafx.scene.control.MenuItem;

@Component
public class CreateLowResolutionProxyChainItem implements ClipContextMenuChainItem {
    private LowResolutionProxyCreatorService lowResolutionProxyCreatorService;
    private UiProjectRepository uiProjectRepository;

    public CreateLowResolutionProxyChainItem(LowResolutionProxyCreatorService lowResolutionProxyCreatorService, UiProjectRepository uiProjectRepository) {
        this.lowResolutionProxyCreatorService = lowResolutionProxyCreatorService;
        this.uiProjectRepository = uiProjectRepository;
    }

    @Override
    public MenuItem createMenu(ClipContextMenuChainItemRequest request) {
        VideoClip videoClip = (VideoClip) request.getPrimaryClip();

        MenuItem menuItem = new MenuItem("Create lowres proxy");

        menuItem.setOnAction(e -> {
            lowResolutionProxyCreatorService.createLowResolutionProxy(videoClip, uiProjectRepository.getPreviewWidth(), uiProjectRepository.getPreviewHeight());
        });

        return menuItem;
    }

    @Override
    public boolean supports(ClipContextMenuChainItemRequest request) {
        return request.getPrimaryClip() instanceof VideoClip &&
                !((VideoClip) request.getPrimaryClip()).containsLowResolutionProxy();
    }

}
