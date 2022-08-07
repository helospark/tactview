package com.helospark.tactview.core.timeline.effect.vignette;

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
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.IntegerProvider;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.util.IndependentPixelOperation;
import com.helospark.tactview.core.util.ReflectionUtil;

public class VignetteEffect extends StatelessVideoEffect {
    private IndependentPixelOperation independentPixelOperation;

    private DoubleProvider vignetteStrengthProvider;
    private IntegerProvider vignettePowerFactorProvider;

    public VignetteEffect(TimelineInterval interval, IndependentPixelOperation independentPixelOperation) {
        super(interval);
        this.independentPixelOperation = independentPixelOperation;
    }

    public VignetteEffect(VignetteEffect cloneFrom, CloneRequestMetadata cloneRequestMetadata) {
        super(cloneFrom, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(cloneFrom, this, cloneRequestMetadata);
    }

    public VignetteEffect(JsonNode node, LoadMetadata loadMetadata, IndependentPixelOperation independentPixelOperation2) {
        super(node, loadMetadata);
        this.independentPixelOperation = independentPixelOperation2;
    }

    @Override
    public ReadOnlyClipImage createFrame(StatelessEffectRequest request) {
        double strength = vignetteStrengthProvider.getValueAt(request.getEffectPosition(), request.getEvaluationContext());
        int power = vignettePowerFactorProvider.getValueAt(request.getEffectPosition(), request.getEvaluationContext());
        Point center = new Point((request.getCurrentFrame().getWidth() / 2), (request.getCurrentFrame().getHeight() / 2));
        double maxImageDistance = Math.max(center.x, center.y);
        return independentPixelOperation.createNewImageWithAppliedTransformation(request.getCurrentFrame(), pixelRequest -> {
            double imageDistance = center.distanceFrom(pixelRequest.x, pixelRequest.y) / maxImageDistance;
            double maskValue = Math.pow(Math.cos(imageDistance * strength), power);
            pixelRequest.output[0] = (int) (pixelRequest.input[0] * maskValue);
            pixelRequest.output[1] = (int) (pixelRequest.input[1] * maskValue);
            pixelRequest.output[2] = (int) (pixelRequest.input[2] * maskValue);
            pixelRequest.output[3] = pixelRequest.input[3];
        });
    }

    @Override
    protected void initializeValueProviderInternal() {
        vignetteStrengthProvider = new DoubleProvider(0.1, 2.0, new MultiKeyframeBasedDoubleInterpolator(0.7));
        vignettePowerFactorProvider = new IntegerProvider(1, 10, new MultiKeyframeBasedDoubleInterpolator(4.0));
    }

    @Override
    protected List<ValueProviderDescriptor> getValueProvidersInternal() {
        ValueProviderDescriptor vignetteStrengthProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(vignetteStrengthProvider)
                .withName("strenght")
                .build();

        ValueProviderDescriptor vignettePowerFactorProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(vignettePowerFactorProvider)
                .withName("power")
                .build();

        return List.of(vignettePowerFactorProviderDescriptor, vignetteStrengthProviderDescriptor);
    }

    @Override
    public StatelessEffect cloneEffect(CloneRequestMetadata cloneRequestMetadata) {
        return new VignetteEffect(this, cloneRequestMetadata);
    }

}
