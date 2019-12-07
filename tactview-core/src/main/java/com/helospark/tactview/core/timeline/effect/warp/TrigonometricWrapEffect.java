package com.helospark.tactview.core.timeline.effect.warp;

import java.math.BigDecimal;
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
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.util.IndependentPixelOperation;
import com.helospark.tactview.core.util.ReflectionUtil;

public class TrigonometricWrapEffect extends StatelessVideoEffect {
    private IndependentPixelOperation independentPixelOperation;

    private DoubleProvider waveSpeedProvider;
    private DoubleProvider frequencyProviderX;
    private DoubleProvider frequencyProviderY;
    private DoubleProvider amplitudeProviderX;
    private DoubleProvider amplitudeProviderY;

    public TrigonometricWrapEffect(TimelineInterval interval, IndependentPixelOperation independentPixelOperation) {
        super(interval);
        this.independentPixelOperation = independentPixelOperation;
    }

    public TrigonometricWrapEffect(TrigonometricWrapEffect cloneFrom, CloneRequestMetadata cloneRequestMetadata) {
        super(cloneFrom, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(cloneFrom, this);
    }

    public TrigonometricWrapEffect(JsonNode node, LoadMetadata loadMetadata, IndependentPixelOperation independentPixelOperation2) {
        super(node, loadMetadata);
        this.independentPixelOperation = independentPixelOperation2;
    }

    @Override
    public ReadOnlyClipImage createFrame(StatelessEffectRequest request) {
        ReadOnlyClipImage currentFrame = request.getCurrentFrame();
        ClipImage result = ClipImage.sameSizeAs(currentFrame);

        double amplitudeX = amplitudeProviderX.getValueAt(request.getEffectPosition());
        double amplitudeY = amplitudeProviderY.getValueAt(request.getEffectPosition());
        double frequencyX = frequencyProviderX.getValueAt(request.getEffectPosition());
        double frequencyY = frequencyProviderY.getValueAt(request.getEffectPosition());
        double millisecIntoEffect = request.getEffectPosition().getSeconds().multiply(BigDecimal.valueOf(1000)).doubleValue();
        double progress = millisecIntoEffect * waveSpeedProvider.getValueAt(request.getEffectPosition());

        independentPixelOperation.executePixelTransformation(currentFrame.getWidth(), currentFrame.getHeight(), (x, y) -> {
            double deltaX = amplitudeX * Math.sin(Math.toRadians(x * frequencyX + y * frequencyY + progress)) * request.getScale();
            double deltaY = amplitudeY * Math.sin(Math.toRadians(y * frequencyY + x * frequencyX + progress)) * request.getScale();
            // these could use some interpolation of neighbouring pixels
            int warpedX = (int) (x + deltaX);
            int warpedY = (int) (y + deltaY);

            if (currentFrame.inBounds(warpedX, warpedY)) {
                int red = currentFrame.getRed(warpedX, warpedY);
                int green = currentFrame.getGreen(warpedX, warpedY);
                int blue = currentFrame.getBlue(warpedX, warpedY);
                int alpha = currentFrame.getAlpha(warpedX, warpedY);

                result.setRed(red, x, y);
                result.setGreen(green, x, y);
                result.setBlue(blue, x, y);
                result.setAlpha(alpha, x, y);
            }
        });

        return result;
    }

    @Override
    protected void initializeValueProviderInternal() {
        waveSpeedProvider = new DoubleProvider(-10.0, 10.0, new MultiKeyframeBasedDoubleInterpolator(1.0));
        frequencyProviderX = new DoubleProvider(0.1, 20.0, new MultiKeyframeBasedDoubleInterpolator(3.0));
        frequencyProviderY = new DoubleProvider(0.1, 20.0, new MultiKeyframeBasedDoubleInterpolator(3.0));
        amplitudeProviderX = new DoubleProvider(0, 100.0, new MultiKeyframeBasedDoubleInterpolator(10.0));
        amplitudeProviderY = new DoubleProvider(0, 100.0, new MultiKeyframeBasedDoubleInterpolator(10.0));
    }

    @Override
    protected List<ValueProviderDescriptor> getValueProvidersInternal() {

        ValueProviderDescriptor progressProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(waveSpeedProvider)
                .withName("Wave speed")
                .build();

        ValueProviderDescriptor frequencyXProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(frequencyProviderX)
                .withName("Frequency X")
                .build();
        ValueProviderDescriptor frequencyYProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(frequencyProviderY)
                .withName("Frequency Y")
                .build();

        ValueProviderDescriptor amplitudeXProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(amplitudeProviderX)
                .withName("Amplitude X")
                .build();

        ValueProviderDescriptor amplitudeYProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(amplitudeProviderY)
                .withName("Amplitude Y")
                .build();

        return List.of(progressProviderDescriptor, frequencyXProviderDescriptor, frequencyYProviderDescriptor, amplitudeXProviderDescriptor, amplitudeYProviderDescriptor);
    }

    @Override
    public StatelessEffect cloneEffect(CloneRequestMetadata cloneRequestMetadata) {
        return new TrigonometricWrapEffect(this, cloneRequestMetadata);
    }

}
