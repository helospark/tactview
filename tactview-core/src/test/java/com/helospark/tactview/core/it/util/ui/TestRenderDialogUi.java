package com.helospark.tactview.core.it.util.ui;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import com.helospark.tactview.core.optionprovider.OptionProvider;
import com.helospark.tactview.core.render.CreateValueProvidersRequest;
import com.helospark.tactview.core.render.RenderRequest;
import com.helospark.tactview.core.render.RenderServiceChain;
import com.helospark.tactview.core.repository.ProjectRepository;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.util.messaging.MessagingService;

public class TestRenderDialogUi {
    RenderServiceChain renderService;
    ProjectRepository projectRepository;
    TimelineManagerAccessor timelineManager;
    MessagingService messagingService;

    String fileName = System.getProperty("java.io.tmpdir") + File.separator + "test.mp4";
    int width;
    int height;
    BigDecimal fps;
    BigDecimal upscale;
    TimelinePosition startPosition, endPosition;

    private Map<String, OptionProvider<?>> optionProviders = Map.of();

    public TestRenderDialogUi(RenderServiceChain renderService, ProjectRepository projectRepository, TimelineManagerAccessor timelineManager, MessagingService messagingService) {
        this.renderService = renderService;
        this.projectRepository = projectRepository;
        this.timelineManager = timelineManager;
        this.messagingService = messagingService;

        this.width = projectRepository.getWidth();
        this.height = projectRepository.getHeight();
        this.fps = projectRepository.getFps();
        this.upscale = BigDecimal.ONE;
        this.startPosition = TimelinePosition.ofSeconds(0);
        this.endPosition = TimelinePosition.ofSeconds(10);

        updateOptionProviders(renderService);
    }

    private void updateOptionProviders(RenderServiceChain renderService) {
        CreateValueProvidersRequest createValueProviderRequest = CreateValueProvidersRequest.builder()
                .withFileName(fileName)
                .build();

        optionProviders = renderService.getRenderer(createRequest()).getOptionProviders(createValueProviderRequest);
    }

    public TestRenderDialogUi setFileName(String fileName) {
        this.fileName = fileName;
        updateOptionProviders(renderService);
        return this;
    }

    public TestRenderDialogUi setWidth(int width) {
        this.width = width;
        return this;
    }

    public TestRenderDialogUi setHeight(int height) {
        this.height = height;
        return this;
    }

    public TestRenderDialogUi setFps(BigDecimal fps) {
        this.fps = fps;
        return this;
    }

    public TestRenderDialogUi setUpscale(BigDecimal upscale) {
        this.upscale = upscale;
        return this;
    }

    public TestRenderDialogUi setStartPosition(TimelinePosition startPosition) {
        this.startPosition = startPosition;
        return this;
    }

    public TestRenderDialogUi setEndPosition(TimelinePosition endPosition) {
        this.endPosition = endPosition;
        return this;
    }

    public TestRenderDialogUi setOptionProviderById(String id, Object value) {
        OptionProvider<Object> optionProvider = (OptionProvider<Object>) this.optionProviders.get(id);
        optionProvider.setValue(value);
        return this;
    }

    public static class RenderDialogProgressing {
        CompletableFuture<Void> future;

        public RenderDialogProgressing(CompletableFuture<Void> future) {
            this.future = future;
        }

        public void waitUntilRenderFinishes() {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }

    }

    public RenderDialogProgressing clickRender() {
        RenderRequest request = createRequest();

        return new RenderDialogProgressing(renderService.render(request));
    }

    private RenderRequest createRequest() {
        RenderRequest request = RenderRequest.builder()
                .withWidth(width)
                .withHeight(height)
                .withStep(BigDecimal.ONE.divide(projectRepository.getFps(), 100, RoundingMode.HALF_UP))
                .withFps(fps)
                .withStartPosition(startPosition)
                .withEndPosition(endPosition)
                .withFileName(fileName)
                .withOptions(optionProviders)
                .withIsCancelledSupplier(() -> false)
                .withUpscale(upscale)
                .build();
        return request;
    }

}
