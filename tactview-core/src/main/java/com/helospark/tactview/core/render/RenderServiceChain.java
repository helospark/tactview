package com.helospark.tactview.core.render;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.util.logger.Slf4j;

@Component
public class RenderServiceChain {
    private List<RenderService> renderServiceChainItem;
    @Slf4j
    private Logger logger;

    public RenderServiceChain(List<RenderService> renderServiceChainItem) {
        this.renderServiceChainItem = renderServiceChainItem;
    }

    public CompletableFuture<Void> render(RenderRequest renderRequest) {
        RenderService chainItem = renderServiceChainItem.stream()
                .filter(a -> a.supports(renderRequest))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No renderer supports format " + renderRequest.getFileName()));

        return CompletableFuture
                .runAsync(() -> chainItem.render(renderRequest))
                .exceptionally(e -> {
                    logger.error("Unable to render", e);
                    return null;
                });
    }
}
