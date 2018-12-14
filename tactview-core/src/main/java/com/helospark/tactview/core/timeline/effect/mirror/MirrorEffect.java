package com.helospark.tactview.core.timeline.effect.mirror;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.api.LoadMetadata;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.StatelessVideoEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.factory.function.impl.StepInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.BooleanProvider;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.util.IndependentPixelOperation;
import com.helospark.tactview.core.util.ReflectionUtil;

public class MirrorEffect extends StatelessVideoEffect {
    private BooleanProvider mirrorVerticallyProvider;
    private BooleanProvider mirrorHorizontalProvider;

    private IndependentPixelOperation independentPixelOperation;

    public MirrorEffect(TimelineInterval interval, IndependentPixelOperation independentPixelOperation) {
        super(interval);
        this.independentPixelOperation = independentPixelOperation;
    }

    public MirrorEffect(MirrorEffect cloneFrom) {
        super(cloneFrom);
        ReflectionUtil.copyOrCloneFieldFromTo(cloneFrom, this);
    }

    public MirrorEffect(JsonNode node, LoadMetadata loadMetadata, IndependentPixelOperation independentPixelOperation2) {
        super(node, loadMetadata);
        this.independentPixelOperation = independentPixelOperation2;
    }

    @Override
    public ReadOnlyClipImage createFrame(StatelessEffectRequest request) {
        boolean verticallyMirrorred = mirrorVerticallyProvider.getValueAt(request.getEffectPosition());
        boolean horizontallyMirrorred = mirrorHorizontalProvider.getValueAt(request.getEffectPosition());

        ReadOnlyClipImage currentFrame = request.getCurrentFrame();
        int width = currentFrame.getWidth();
        int height = currentFrame.getHeight();

        ClipImage result = ClipImage.sameSizeAs(currentFrame);

        independentPixelOperation.executePixelTransformation(width, height, (x, y) -> {
            int mirrorredX = horizontallyMirrorred ? width - x - 1 : x;
            int mirrorredY = verticallyMirrorred ? height - y - 1 : y;

            int red = currentFrame.getRed(mirrorredX, mirrorredY);
            int green = currentFrame.getGreen(mirrorredX, mirrorredY);
            int blue = currentFrame.getBlue(mirrorredX, mirrorredY);
            int alpha = currentFrame.getAlpha(mirrorredX, mirrorredY);

            result.setRed(red, x, y);
            result.setGreen(green, x, y);
            result.setBlue(blue, x, y);
            result.setAlpha(alpha, x, y);
        });
        return result;
    }

    @Override
    public void initializeValueProvider() {
        mirrorVerticallyProvider = new BooleanProvider(new MultiKeyframeBasedDoubleInterpolator(TimelinePosition.ofZero(), 0.0, new StepInterpolator()));
        mirrorHorizontalProvider = new BooleanProvider(new MultiKeyframeBasedDoubleInterpolator(TimelinePosition.ofZero(), 0.0, new StepInterpolator()));
    }

    @Override
    public List<ValueProviderDescriptor> getValueProviders() {

        ValueProviderDescriptor mirrorVerticallyDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(mirrorVerticallyProvider)
                .withName("Mirror vertically")
                .build();
        ValueProviderDescriptor mirrorHorizontalDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(mirrorHorizontalProvider)
                .withName("Mirror horizontally")
                .build();

        return List.of(mirrorHorizontalDescriptor, mirrorVerticallyDescriptor);
    }

    @Override
    public StatelessEffect cloneEffect() {
        return new MirrorEffect(this);
    }

}
