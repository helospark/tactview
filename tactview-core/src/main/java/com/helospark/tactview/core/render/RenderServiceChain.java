package com.helospark.tactview.core.render;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.slf4j.Logger;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.render.helper.HandledExtensionValueElement;
import com.helospark.tactview.core.timeline.longprocess.LongProcessRequestor;
import com.helospark.tactview.core.util.ThreadSleep;
import com.helospark.tactview.core.util.logger.Slf4j;

@Component
public class RenderServiceChain {
    private List<RenderService> renderServiceChainItem;
    private LongProcessRequestor longProcessRequestor;
    @Slf4j
    private Logger logger;

    public RenderServiceChain(List<RenderService> renderServiceChainItem, LongProcessRequestor longProcessRequestor) {
        this.renderServiceChainItem = renderServiceChainItem;
        this.longProcessRequestor = longProcessRequestor;
    }

    public CompletableFuture<Void> render(RenderRequest renderRequest) {
        RenderService chainItem = getRenderer(renderRequest);

        return CompletableFuture
                .runAsync(() -> {
                    int i = 0;
                    while (!longProcessRequestor.isFinished()) {
                        ThreadSleep.sleep(1000);
                        ++i;
                        if (i % 10 == 0) {
                            logger.info("Doing long processes, number of unscheduled processes " + longProcessRequestor.getRunningJobs().size());
                        }
                    }
                })
                .thenRunAsync(() -> chainItem.render(renderRequest));
    }

    public RenderService getRenderer(RenderRequest renderRequest) {
        return renderServiceChainItem.stream()
                .filter(a -> a.supports(renderRequest))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No renderer supports format " + renderRequest.getFileName()));
    }

    public List<HandledExtensionValueElement> getCommonHandledExtensions() {
        List<HandledExtensionValueElement> elements = renderServiceChainItem.stream()
                .flatMap(renderService -> renderService.handledExtensions().stream())
                .collect(Collectors.toList());
        List<HandledExtensionValueElement> reversedList = new ArrayList<>();

        for (int i = elements.size() - 1; i >= 0; --i) {
            reversedList.add(elements.get(i));
        }

        return reversedList;
    }

}
