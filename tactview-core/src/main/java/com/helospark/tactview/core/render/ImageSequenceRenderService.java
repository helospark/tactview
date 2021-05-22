package com.helospark.tactview.core.render;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.slf4j.Logger;

import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.annotation.Order;
import com.helospark.tactview.core.decoder.framecache.GlobalMemoryManagerAccessor;
import com.helospark.tactview.core.optionprovider.OptionProvider;
import com.helospark.tactview.core.render.helper.ExtensionType;
import com.helospark.tactview.core.render.helper.HandledExtensionValueElement;
import com.helospark.tactview.core.repository.ProjectRepository;
import com.helospark.tactview.core.timeline.TimelineManagerRenderService;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.scale.service.ScaleService;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.timeline.message.progress.ProgressAdvancedMessage;
import com.helospark.tactview.core.util.ByteBufferToImageConverter;
import com.helospark.tactview.core.util.logger.Slf4j;
import com.helospark.tactview.core.util.messaging.MessagingService;

@Component
@Order(value = -1)
public class ImageSequenceRenderService extends AbstractRenderService {

    private static final int FRAME_PER_BATCH = 60;
    @Slf4j
    private Logger logger;

    private ByteBufferToImageConverter byteBufferToImageConverter;

    public ImageSequenceRenderService(TimelineManagerRenderService timelineManager, ByteBufferToImageConverter byteBufferToImageConverter, MessagingService messagingService,
            ScaleService scaleService, ProjectRepository projectRepository) {
        super(timelineManager, messagingService, scaleService, projectRepository);
        this.byteBufferToImageConverter = byteBufferToImageConverter;
    }

    @Override
    public void renderInternal(RenderRequest renderRequest) {
        ExecutorService renderExecutorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        File file = new File(renderRequest.getFileName());
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        BigDecimal startSeconds = renderRequest.getStartPosition().getSeconds();
        BigDecimal endSeconds = renderRequest.getEndPosition().getSeconds();

        BigDecimal renderDistance = renderRequest.getStep().multiply(BigDecimal.valueOf(FRAME_PER_BATCH));

        BigDecimal position = startSeconds;
        int startFrame = startSeconds.multiply(renderRequest.getStep()).intValue();
        while (position.compareTo(endSeconds) < 0 && !renderRequest.getIsCancelledSupplier().get()) {
            BigDecimal finalPosition = position;
            int finalStartFrame = startFrame;

            CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(() -> {
                renderFrames(finalPosition, finalStartFrame, renderRequest, renderRequest.getEndPosition());
            }, renderExecutorService);

            futures.add(completableFuture);
            startFrame += FRAME_PER_BATCH;
            position = position.add(renderDistance);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()])).join();
        renderExecutorService.shutdown();
    }

    private void renderFrames(BigDecimal position, int startFrame, RenderRequest renderRequest, TimelinePosition endPosition) {
        BigDecimal currentPosition = position;
        int currentFrame = startFrame;
        String fileName = renderRequest.getFileName();
        int extensionIndex = fileName.lastIndexOf('.');
        String extension = fileName.substring(extensionIndex + 1);
        String fileNameWithoutExtension = fileName.substring(0, extensionIndex);
        for (int i = 0; i < FRAME_PER_BATCH; ++i) {
            try {
                TimelinePosition framePosition = new TimelinePosition(position).add(BigDecimal.valueOf(i).multiply(projectRepository.getFrameTime()));
                if (framePosition.isGreaterThan(endPosition)) {
                    break;
                }

                RenderRequestFrameRequest superRequest = RenderRequestFrameRequest.builder()
                        .withRenderRequest(renderRequest)
                        .withCurrentPosition(framePosition)
                        .withNeedsSound(false)
                        .withNeedsVideo(true)
                        .withExpectedWidth(renderRequest.getWidth())
                        .withExpectedHeight(renderRequest.getHeight())
                        .build();

                ReadOnlyClipImage videoResult = queryFrameAt(superRequest).getVideoResult();
                ByteBuffer frame = videoResult.getBuffer();

                BufferedImage image = byteBufferToImageConverter.byteBufferToBufferedImage(frame, renderRequest.getWidth(), renderRequest.getHeight());

                File outputfile = new File(fileNameWithoutExtension + "_" + currentFrame + "." + extension);
                ImageIO.write(image, extension, outputfile);

                renderRequest.getEncodedImageCallback().accept(videoResult);

                GlobalMemoryManagerAccessor.memoryManager.returnBuffer(frame);
                currentPosition = currentPosition.add(renderRequest.getStep());
                ++currentFrame;
                messagingService.sendAsyncMessage(new ProgressAdvancedMessage(renderRequest.getRenderId(), 1));
            } catch (IOException e) {
                logger.error("Error rendering frame", e);
            }
        }
    }

    @Override
    public String getId() {
        return "image";
    }

    @Override
    public List<String> getSupportedFormats() {
        return Arrays.stream(ImageIO.getWriterFormatNames())
                .map(a -> a.toLowerCase())
                .distinct()
                .filter(a -> !a.equals("gif")) // only animated git supported
                .collect(Collectors.toList());
    }

    @Override
    public boolean supports(RenderRequest renderRequest) {
        return getSupportedFormats()
                .stream()
                .filter(a -> renderRequest.getFileName().endsWith(a))
                .findFirst()
                .isPresent();
    }

    @Override
    public Map<String, OptionProvider<?>> getOptionProviders(CreateValueProvidersRequest request) {
        return Map.of();
    }

    @Override
    public Map<String, OptionProvider<?>> updateValueProviders(UpdateValueProvidersRequest request) {
        return request.options;
    }

    @Override
    public List<HandledExtensionValueElement> handledExtensions() {
        return getSupportedFormats()
                .stream()
                .map(a -> new HandledExtensionValueElement(a, a + " (images seq.)", ExtensionType.IMAGE_SEQUENCE))
                .collect(Collectors.toList());
    }

}
