package com.helospark.tactview.core.timeline.effect.blend;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.decoder.framecache.GlobalMemoryManagerAccessor;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.StatelessVideoEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.blendmode.BlendModeStrategy;
import com.helospark.tactview.core.timeline.blendmode.BlendModeValueListElement;
import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.StepStringInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.BooleanProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DependentClipProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ValueListProvider;
import com.helospark.tactview.core.timeline.effect.scale.service.ScaleRequest;
import com.helospark.tactview.core.timeline.effect.scale.service.ScaleService;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.util.IndependentPixelOperation;
import com.helospark.tactview.core.util.ReflectionUtil;
import com.helospark.tactview.core.util.ThreadLocalProvider;

public class BlendEffect extends StatelessVideoEffect {
    private List<BlendModeStrategy> strategies;
    private ScaleService scaleService;
    private IndependentPixelOperation independentPixelOperation;

    private DependentClipProvider dependentClipProvider;
    private ValueListProvider<BlendModeValueListElement> blendModeProvider;
    private BooleanProvider scaleOnDifferentSizePrivder;

    public BlendEffect(TimelineInterval interval, List<BlendModeStrategy> strategies, ScaleService scaleService, IndependentPixelOperation independentPixelOperation) {
        super(interval);
        this.strategies = strategies;
        this.scaleService = scaleService;
        this.independentPixelOperation = independentPixelOperation;
    }

    public BlendEffect(BlendEffect blendEffect, CloneRequestMetadata cloneRequestMetadata) {
        super(blendEffect, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(blendEffect, this, cloneRequestMetadata);
    }

    public BlendEffect(JsonNode node, LoadMetadata loadMetadata, List<BlendModeStrategy> strategies2, ScaleService scaleService2, IndependentPixelOperation independentPixelOperation2) {
        super(node, loadMetadata);
        this.strategies = strategies2;
        this.scaleService = scaleService2;
        this.independentPixelOperation = independentPixelOperation2;
    }

    @Override
    public ReadOnlyClipImage createFrame(StatelessEffectRequest request) {
        Optional<ReadOnlyClipImage> otherClip = dependentClipProvider.getValueAt(request.getEffectPosition(), request.getRequestedClips());
        if (otherClip.isPresent()) {
            ReadOnlyClipImage clip = otherClip.get();

            boolean scaleOnDifferentSize = scaleOnDifferentSizePrivder.getValueAt(request.getEffectPosition());
            BlendModeStrategy blendMode = blendModeProvider.getValueAt(request.getEffectPosition()).getBlendMode();
            boolean isScaled = false;
            ReadOnlyClipImage imageToUse = clip;
            if (!clip.isSameSizeAs(request.getCurrentFrame()) && scaleOnDifferentSize) {
                ScaleRequest scaleRequest = ScaleRequest.builder()
                        .withImage(clip)
                        .withNewWidth(request.getCurrentFrame().getWidth())
                        .withNewHeight(request.getCurrentFrame().getHeight())
                        .build();

                ClipImage scaledImage = scaleService.createScaledImage(scaleRequest);
                imageToUse = scaledImage;
                isScaled = true;
            }

            int commonWidth = Math.min(imageToUse.getWidth(), request.getCurrentFrame().getWidth());
            int commonHeight = Math.min(imageToUse.getHeight(), request.getCurrentFrame().getHeight());

            ThreadLocalProvider<int[]> intArrayProvider = () -> new int[4];

            ReadOnlyClipImage finalImageToUse = imageToUse;
            ClipImage result = independentPixelOperation.createNewImageWithAppliedTransformation(request.getCurrentFrame(), List.of(intArrayProvider), pixelRequest -> {
                if (pixelRequest.x < commonWidth && pixelRequest.y < commonHeight) {
                    int[] otherClipPixelData = pixelRequest.getThreadLocal(intArrayProvider);

                    for (int i = 0; i < 4; ++i) {
                        otherClipPixelData[i] = finalImageToUse.getColorComponentWithOffset(pixelRequest.x, pixelRequest.y, i);
                    }

                    blendMode.computeColor(pixelRequest.input, otherClipPixelData, pixelRequest.output);
                } else {
                    for (int i = 0; i < 4; ++i) {
                        pixelRequest.output[i] = pixelRequest.input[i];
                    }
                }
            });

            if (isScaled) {
                GlobalMemoryManagerAccessor.memoryManager.returnBuffer(imageToUse.getBuffer());
            }

            return result;
        } else {
            ReadOnlyClipImage currentFrame = request.getCurrentFrame();
            ClipImage result = ClipImage.sameSizeAs(currentFrame);
            return result.copyFrom(currentFrame);
        }
    }

    @Override
    protected void initializeValueProviderInternal() {
        dependentClipProvider = new DependentClipProvider(new StepStringInterpolator());
        blendModeProvider = new ValueListProvider<>(createBlendModes(), new StepStringInterpolator("normal"));
        scaleOnDifferentSizePrivder = new BooleanProvider(new MultiKeyframeBasedDoubleInterpolator(0.0));
    }

    private List<BlendModeValueListElement> createBlendModes() {
        return strategies
                .stream()
                .map(blendMode -> new BlendModeValueListElement(blendMode.getId(), blendMode.getId(), blendMode))
                .collect(Collectors.toList());
    }

    @Override
    protected List<ValueProviderDescriptor> getValueProvidersInternal() {
        ValueProviderDescriptor dependentClipProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(dependentClipProvider)
                .withName("Clip")
                .build();
        ValueProviderDescriptor blendModeProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(blendModeProvider)
                .withName("Mode")
                .build();
        ValueProviderDescriptor scaleOnDifferentSizeDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(scaleOnDifferentSizePrivder)
                .withName("Scale on different size")
                .build();

        return List.of(dependentClipProviderDescriptor, blendModeProviderDescriptor, scaleOnDifferentSizeDescriptor);
    }

    @Override
    public StatelessEffect cloneEffect(CloneRequestMetadata cloneRequestMetadata) {
        return new BlendEffect(this, cloneRequestMetadata);
    }

    @Override
    public List<String> getClipDependency(TimelinePosition position) {
        return List.of(dependentClipProvider.getValueAt(position));
    }

}
