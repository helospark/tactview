package com.helospark.tactview.core.timeline.effect.displacementmap;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.decoder.framecache.GlobalMemoryManagerAccessor;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.StatelessVideoEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.StringInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DependentClipProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.effect.scale.service.ScaleRequest;
import com.helospark.tactview.core.timeline.effect.scale.service.ScaleService;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.util.IndependentPixelOperation;
import com.helospark.tactview.core.util.ReflectionUtil;

public class DisplacementMapEffect extends StatelessVideoEffect {
    private DependentClipProvider displacementMapProvider;
    private DoubleProvider verticalDisplacementMultiplierProvider;
    private DoubleProvider horizontalDisplacementMultiplierProvider;

    private IndependentPixelOperation independentPixelOperation;
    private ScaleService scaleService;

    public DisplacementMapEffect(TimelineInterval interval, ScaleService scaleService, IndependentPixelOperation independentPixelOperation) {
        super(interval);
        this.scaleService = scaleService;
        this.independentPixelOperation = independentPixelOperation;
    }

    public DisplacementMapEffect(DisplacementMapEffect cloneFrom) {
        super(cloneFrom);
        ReflectionUtil.copyOrCloneFieldFromTo(cloneFrom, this);
    }

    public DisplacementMapEffect(JsonNode node, ScaleService scaleService2, IndependentPixelOperation independentPixelOperation2) {
        super(node);
        this.independentPixelOperation = independentPixelOperation2;
    }

    @Override
    public ReadOnlyClipImage createFrame(StatelessEffectRequest request) {
        Optional<ReadOnlyClipImage> optionalDisplacementMap = displacementMapProvider.getValueAt(request.getEffectPosition(), request.getRequestedClips());

        if (optionalDisplacementMap.isPresent()) {
            return applyDisplacementMap(optionalDisplacementMap.get(), request);
        } else {
            ClipImage result = ClipImage.sameSizeAs(request.getCurrentFrame());
            result.copyFrom(request.getCurrentFrame());
            return result;
        }
    }

    private ClipImage applyDisplacementMap(ReadOnlyClipImage displacementMap, StatelessEffectRequest request) {
        ReadOnlyClipImage currentFrame = request.getCurrentFrame();

        if (!currentFrame.isSameSizeAs(displacementMap)) {
            ScaleRequest scaleRequest = ScaleRequest.builder()
                    .withImage(displacementMap)
                    .withNewWidth(currentFrame.getWidth())
                    .withNewHeight(currentFrame.getHeight())
                    .build();
            ReadOnlyClipImage scaledImage = scaleService.createScaledImage(scaleRequest);
            ClipImage result = applyDisplacementMapOnSameDisplacementMapSize(currentFrame, scaledImage, request);
            GlobalMemoryManagerAccessor.memoryManager.returnBuffer(scaledImage.getBuffer());
            return result;
        } else {
            return applyDisplacementMapOnSameDisplacementMapSize(currentFrame, displacementMap, request);
        }

    }

    private ClipImage applyDisplacementMapOnSameDisplacementMapSize(ReadOnlyClipImage currentFrame, ReadOnlyClipImage displacementMap, StatelessEffectRequest request) {
        ClipImage result = ClipImage.sameSizeAs(currentFrame);
        double verticalMultiplier = verticalDisplacementMultiplierProvider.getValueAt(request.getEffectPosition()) * request.getScale();
        double horizontalMultiplier = horizontalDisplacementMultiplierProvider.getValueAt(request.getEffectPosition()) * request.getScale();

        independentPixelOperation.executePixelTransformation(result.getWidth(), result.getHeight(), (x, y) -> {
            int displacedX = clip(x + (((displacementMap.getRed(x, y) - 128) / 128.0) * horizontalMultiplier), 0, result.getWidth() - 1);
            int displacedY = clip(y + (((displacementMap.getGreen(x, y) - 128) / 128.0) * verticalMultiplier), 0, result.getHeight() - 1);

            for (int i = 0; i < 4; ++i) {
                int component = currentFrame.getColorComponentWithOffset(displacedX, displacedY, i);
                result.setColorComponentByOffset(component, x, y, i);
            }
        });
        return result;
    }

    private int clip(double value, int min, int max) {
        if (value > max) {
            return max;
        }
        if (value < min) {
            return min;
        }
        return (int) value;
    }

    @Override
    public List<String> getClipDependency(TimelinePosition position) {
        List<String> result = super.getClipDependency(position);
        String displacementMap = displacementMapProvider.getValueAt(position);

        if (!displacementMap.isEmpty()) {
            result.add(displacementMap);
        }

        return result;
    }

    @Override
    public void initializeValueProvider() {
        displacementMapProvider = new DependentClipProvider(new StringInterpolator());
        verticalDisplacementMultiplierProvider = new DoubleProvider(0, 100, new MultiKeyframeBasedDoubleInterpolator(30.0));
        verticalDisplacementMultiplierProvider.setScaleDependent();

        horizontalDisplacementMultiplierProvider = new DoubleProvider(0, 100, new MultiKeyframeBasedDoubleInterpolator(30.0));
        horizontalDisplacementMultiplierProvider.setScaleDependent();
    }

    @Override
    public List<ValueProviderDescriptor> getValueProviders() {
        List<ValueProviderDescriptor> result = new ArrayList<>();

        ValueProviderDescriptor displacementMapProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(displacementMapProvider)
                .withName("displacement map")
                .build();
        ValueProviderDescriptor verticalDisplacementMultiplierProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(verticalDisplacementMultiplierProvider)
                .withName("vertical multiplier")
                .build();
        ValueProviderDescriptor horizontalDisplacementMultiplierProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(horizontalDisplacementMultiplierProvider)
                .withName("horizontal multiplier")
                .build();

        result.add(displacementMapProviderDescriptor);
        result.add(verticalDisplacementMultiplierProviderDescriptor);
        result.add(horizontalDisplacementMultiplierProviderDescriptor);

        return result;
    }

    @Override
    public StatelessEffect cloneEffect() {
        return new DisplacementMapEffect(this);
    }

}
