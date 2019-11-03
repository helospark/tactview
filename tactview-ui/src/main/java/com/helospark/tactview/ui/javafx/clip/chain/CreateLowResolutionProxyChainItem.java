package com.helospark.tactview.ui.javafx.clip.chain;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.render.proxy.ProxyCreationService;
import com.helospark.tactview.core.timeline.VideoClip;
import com.helospark.tactview.ui.javafx.repository.UiProjectRepository;

import javafx.scene.control.MenuItem;

@Component
public class CreateLowResolutionProxyChainItem implements ClipContextMenuChainItem {
    //    private LowResolutionProxyCreatorService lowResolutionProxyCreatorService;
    private ProxyCreationService proxyCreationService;
    private UiProjectRepository uiProjectRepository;

    public CreateLowResolutionProxyChainItem(ProxyCreationService proxyCreationService, UiProjectRepository uiProjectRepository) {
        this.proxyCreationService = proxyCreationService;
        this.uiProjectRepository = uiProjectRepository;
    }

    @Override
    public MenuItem createMenu(ClipContextMenuChainItemRequest request) {
        VideoClip videoClip = (VideoClip) request.getPrimaryClip();

        MenuItem menuItem = new MenuItem("Create lowres proxy");

        menuItem.setOnAction(e -> {
            proxyCreationService.createProxy(videoClip, uiProjectRepository.getPreviewWidth(), uiProjectRepository.getPreviewHeight());
        });

        return menuItem;
    }

    @Override
    public boolean supports(ClipContextMenuChainItemRequest request) {
        return request.getPrimaryClip() instanceof VideoClip &&
                !((VideoClip) request.getPrimaryClip()).containsLowResolutionProxy();
    }

}
