package com.helospark.tactview.core.render;

import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.annotation.Order;
import com.helospark.tactview.core.optionprovider.OptionProvider;
import com.helospark.tactview.core.repository.ProjectRepository;
import com.helospark.tactview.core.timeline.AudioVideoFragment;
import com.helospark.tactview.core.timeline.TimelineManagerRenderService;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ValueListElement;
import com.helospark.tactview.core.timeline.effect.scale.service.ScaleService;
import com.helospark.tactview.core.timeline.message.progress.ProgressAdvancedMessage;
import com.helospark.tactview.core.util.ByteBufferToImageConverter;
import com.helospark.tactview.core.util.logger.Slf4j;
import com.helospark.tactview.core.util.messaging.MessagingService;

import io.korhner.asciimg.image.AsciiImgCache;
import io.korhner.asciimg.image.character_fit_strategy.StructuralSimilarityFitStrategy;
import io.korhner.asciimg.image.converter.AsciiToStringConverter;

@Component
@Order(value = -2)
public class AsciiArtRenderService extends AbstractRenderService {
    @Slf4j
    private Logger logger;
    private StructuralSimilarityFitStrategy characterFirStrategy = new StructuralSimilarityFitStrategy();
    private ByteBufferToImageConverter byteBufferToImageConverter;

    public AsciiArtRenderService(TimelineManagerRenderService timelineManager, ByteBufferToImageConverter byteBufferToImageConverter, MessagingService messagingService,
            ScaleService scaleService, ProjectRepository projectRepository) {
        super(timelineManager, messagingService, scaleService, projectRepository);
        this.byteBufferToImageConverter = byteBufferToImageConverter;
    }

    @Override
    public void renderInternal(RenderRequest renderRequest) {
        File file = new File(renderRequest.getFileName());
        try (OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(file))) {

            outputStreamWriter.write("#!/bin/bash\n\n");

            BigDecimal startSeconds = renderRequest.getStartPosition().getSeconds();
            BigDecimal endSeconds = renderRequest.getEndPosition().getSeconds();

            BigDecimal position = startSeconds;
            while (position.compareTo(endSeconds) < 0 && !renderRequest.getIsCancelledSupplier().get()) {

                RenderRequestFrameRequest superRequest = RenderRequestFrameRequest.builder()
                        .withRenderRequest(renderRequest)
                        .withCurrentPosition(new TimelinePosition(position))
                        .withNeedsSound(false)
                        .withNeedsVideo(true)
                        .withExpectedWidth(renderRequest.getWidth())
                        .withExpectedHeight(renderRequest.getHeight())
                        .build();

                AudioVideoFragment videoResult = queryFrameAt(superRequest);

                BufferedImage image = byteBufferToImageConverter.byteBufferToBufferedImage(videoResult.getVideoResult().getBuffer(), renderRequest.getWidth(), renderRequest.getHeight());

                AsciiImgCache smallFontCache = AsciiImgCache.create(new Font("Courier", Font.PLAIN, 12));

                AsciiToStringConverter imageConverter = new AsciiToStringConverter(smallFontCache, characterFirStrategy);
                imageConverter.setCharacterCache(smallFontCache);
                imageConverter.setCharacterFitStrategy(characterFirStrategy);

                String frame = imageConverter.convertImage(image).toString()
                        .replaceAll("\n", "\\\\n")
                        .replaceAll("'", "\\\\'");

                outputStreamWriter.write("echo -e $'" + frame + "'\n");
                outputStreamWriter.write("sleep " + renderRequest.getStep().setScale(3, RoundingMode.HALF_UP) + "\n");
                outputStreamWriter.write("clear\n\n");

                renderRequest.getEncodedImageCallback().accept(videoResult.getVideoResult());

                videoResult.free();
                position = position.add(renderRequest.getStep());
                messagingService.sendAsyncMessage(new ProgressAdvancedMessage(renderRequest.getRenderId(), 1));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getId() {
        return "asciiart";
    }

    @Override
    public List<String> getSupportedFormats() {
        return List.of("sh");
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
    public List<ValueListElement> handledExtensions() {
        return List.of(new ValueListElement("sh", "sh (ascii art)"));
    }

}
