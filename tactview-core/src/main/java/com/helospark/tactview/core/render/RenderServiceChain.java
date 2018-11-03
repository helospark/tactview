package com.helospark.tactview.core.render;

import java.util.List;

import com.helospark.lightdi.annotation.Component;

@Component
public class RenderServiceChain {
    private List<RenderService> renderServiceChainItem;

    public RenderServiceChain(List<RenderService> renderServiceChainItem) {
        this.renderServiceChainItem = renderServiceChainItem;
    }

    public void render(RenderRequest renderRequest) {
        renderServiceChainItem.stream()
                .filter(a -> a.supports(renderRequest))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No renderer supports format " + renderRequest.getFileName()))
                .render(renderRequest);
    }
}
