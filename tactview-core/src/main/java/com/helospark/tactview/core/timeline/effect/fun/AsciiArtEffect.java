package com.helospark.tactview.core.timeline.effect.fun;

import java.awt.Font;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.CloneRequestMetadata;
import com.helospark.tactview.core.LoadMetadata;
import com.helospark.tactview.core.ReflectionUtil;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.StatelessVideoEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.StepStringInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.IntegerProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ValueListElement;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ValueListProvider;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.util.BufferedImageToClipFrameResultConverter;
import com.helospark.tactview.core.util.ByteBufferToImageConverter;
import com.helospark.tactview.core.util.MathUtil;

import io.korhner.asciimg.image.AsciiImgCache;
import io.korhner.asciimg.image.character_fit_strategy.BestCharacterFitStrategy;
import io.korhner.asciimg.image.character_fit_strategy.ColorSquareErrorFitStrategy;
import io.korhner.asciimg.image.character_fit_strategy.StructuralSimilarityFitStrategy;
import io.korhner.asciimg.image.converter.AsciiToImageConverter;

public class AsciiArtEffect extends StatelessVideoEffect {
    private static final Map<String, BestCharacterFitStrategy> characterFitStrategies = Map.of(
            "square error", new ColorSquareErrorFitStrategy(),
            "ssim", new StructuralSimilarityFitStrategy());

    private IntegerProvider characterHeightProvider;
    private ValueListProvider<ValueListElement> characterFitStrategy;

    private ByteBufferToImageConverter byteBufferToImageConverter;
    private BufferedImageToClipFrameResultConverter bufferedImageToClipFrameResultConverter;

    public AsciiArtEffect(TimelineInterval interval, ByteBufferToImageConverter byteBufferToImageConverter, BufferedImageToClipFrameResultConverter bufferedImageToClipFrameResultConverter) {
        super(interval);
        this.byteBufferToImageConverter = byteBufferToImageConverter;
        this.bufferedImageToClipFrameResultConverter = bufferedImageToClipFrameResultConverter;
    }

    public AsciiArtEffect(AsciiArtEffect blurEffect, CloneRequestMetadata cloneRequestMetadata) {
        super(blurEffect, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(blurEffect, this);
    }

    public AsciiArtEffect(JsonNode node, LoadMetadata loadMetadata, ByteBufferToImageConverter byteBufferToImageConverter,
            BufferedImageToClipFrameResultConverter bufferedImageToClipFrameResultConverter) {
        super(node, loadMetadata);
        this.byteBufferToImageConverter = byteBufferToImageConverter;
        this.bufferedImageToClipFrameResultConverter = bufferedImageToClipFrameResultConverter;
    }

    @Override
    public ReadOnlyClipImage createFrame(StatelessEffectRequest request) {
        int characterSize = MathUtil.clampToInt(characterHeightProvider.getValueAt(request.getEffectPosition()) * request.getScale(), 2, 200);

        AsciiImgCache smallFontCache = AsciiImgCache.create(new Font("Courier", Font.BOLD, characterSize));
        BufferedImage input = byteBufferToImageConverter.frameToBufferedImage(request.getCurrentFrame());
        BestCharacterFitStrategy squareErrorStrategy = characterFitStrategies.get(characterFitStrategy.getValueAt(request.getEffectPosition()).getId());

        AsciiToImageConverter imageConverter = new AsciiToImageConverter(smallFontCache, squareErrorStrategy);
        imageConverter.setCharacterCache(smallFontCache);
        imageConverter.setCharacterFitStrategy(squareErrorStrategy);

        return bufferedImageToClipFrameResultConverter.convertFromIntArgb(imageConverter.convertImage(input));
    }

    @Override
    public void initializeValueProvider() {
        characterHeightProvider = new IntegerProvider(2, 100, new MultiKeyframeBasedDoubleInterpolator(20.0));

        characterFitStrategy = new ValueListProvider<>(List.of(
                new ValueListElement("square error", "square error"),
                new ValueListElement("ssim", "ssim")),
                new StepStringInterpolator("ssim"));
    }

    @Override
    public List<ValueProviderDescriptor> getValueProviders() {
        ValueProviderDescriptor characterHeightProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(characterHeightProvider)
                .withName("Character height")
                .build();

        ValueProviderDescriptor characterFitStrategyDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(characterFitStrategy)
                .withName("Strategy")
                .build();

        return Arrays.asList(characterFitStrategyDescriptor, characterHeightProviderDescriptor);
    }

    @Override
    public StatelessEffect cloneEffect(CloneRequestMetadata cloneRequestMetadata) {
        return new AsciiArtEffect(this, cloneRequestMetadata);
    }

}
