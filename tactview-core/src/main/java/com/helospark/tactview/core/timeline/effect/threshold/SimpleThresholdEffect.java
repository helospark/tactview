package com.helospark.tactview.core.timeline.effect.threshold;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.StatelessVideoEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.IntegerProvider;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.util.IndependentPixelOperation;
import com.helospark.tactview.core.util.ReflectionUtil;

public class SimpleThresholdEffect extends StatelessVideoEffect {
    private IndependentPixelOperation independentPixelOperation;

    private IntegerProvider thresholdLimitProvider;

    public SimpleThresholdEffect(TimelineInterval interval, IndependentPixelOperation independentPixelOperation) {
        super(interval);
        this.independentPixelOperation = independentPixelOperation;
    }

    public SimpleThresholdEffect(SimpleThresholdEffect cloneFrom, CloneRequestMetadata cloneRequestMetadata) {
        super(cloneFrom, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(cloneFrom, this, cloneRequestMetadata);
    }

    public SimpleThresholdEffect(JsonNode node, LoadMetadata loadMetadata, IndependentPixelOperation independentPixelOperation2) {
        super(node, loadMetadata);
        this.independentPixelOperation = independentPixelOperation2;
    }

    @Override
    public ReadOnlyClipImage createFrame(StatelessEffectRequest request) {
        Integer threshold = thresholdLimitProvider.getValueAt(request.getEffectPosition());
        return independentPixelOperation.createNewImageWithAppliedTransformation(request.getCurrentFrame(), pixelRequest -> {
            int[] input = pixelRequest.input;
            double value = (input[0] + input[1] + input[2]) / 3.0;
            if (value > threshold) {
                pixelRequest.output[0] = 255;
                pixelRequest.output[1] = 255;
                pixelRequest.output[2] = 255;
                pixelRequest.output[3] = input[3];
            } else {
                pixelRequest.output[0] = 0;
                pixelRequest.output[1] = 0;
                pixelRequest.output[2] = 0;
                pixelRequest.output[3] = input[3];
            }
        });
    }

    @Override
    protected List<ValueProviderDescriptor> getValueProvidersInternal() {

        ValueProviderDescriptor thresholdDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(thresholdLimitProvider)
                .withName("Added constant")
                .build();

        return List.of(thresholdDescriptor);
    }

    @Override
    public StatelessEffect cloneEffect(CloneRequestMetadata cloneRequestMetadata) {
        return new SimpleThresholdEffect(this, cloneRequestMetadata);
    }

    @Override
    protected void initializeValueProviderInternal() {
        thresholdLimitProvider = new IntegerProvider(0, 255, new MultiKeyframeBasedDoubleInterpolator(127.0));
    }

}
