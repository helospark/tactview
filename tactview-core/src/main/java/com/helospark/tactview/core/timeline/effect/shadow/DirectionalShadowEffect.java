package com.helospark.tactview.core.timeline.effect.shadow;

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
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Color;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.InterpolationLine;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ColorProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.LineProvider;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.util.IndependentPixelOperation;
import com.helospark.tactview.core.util.ReflectionUtil;

public class DirectionalShadowEffect extends StatelessVideoEffect {
    private IndependentPixelOperation independentPixelOperation;

    private DoubleProvider shadowAlphaProvider;
    private ColorProvider shadowColorProvider;
    private LineProvider directionProvider;

    public DirectionalShadowEffect(TimelineInterval interval, IndependentPixelOperation independentPixelOperation) {
        super(interval);
        this.independentPixelOperation = independentPixelOperation;
    }

    public DirectionalShadowEffect(DirectionalShadowEffect dropShadowEffect, CloneRequestMetadata cloneRequestMetadata) {
        super(dropShadowEffect, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(dropShadowEffect, this, cloneRequestMetadata);
    }

    public DirectionalShadowEffect(JsonNode node, LoadMetadata loadMetadata, IndependentPixelOperation independentPixelOperation) {
        super(node, loadMetadata);
        this.independentPixelOperation = independentPixelOperation;
    }

    @Override
    public ReadOnlyClipImage createFrame(StatelessEffectRequest request) {
        ReadOnlyClipImage currentFrame = request.getCurrentFrame();
        ClipImage shadow = ClipImage.sameSizeAs(currentFrame);

        InterpolationLine direction = directionProvider.getValueAt(request.getEffectPosition());
        Point normalizedVector = direction.getNormalize4dVector();
        Color shadowColor = shadowColorProvider.getValueAt(request.getEffectPosition()).multiplyComponents(255.0);
        int shadowAlpha = (int) (shadowAlphaProvider.getValueAt(request.getEffectPosition()) * 255.0);

        independentPixelOperation.executePixelTransformation(currentFrame.getWidth(), currentFrame.getHeight(), (x, y) -> {
            int currentAlpha = currentFrame.getAlpha(x, y);

            if (currentAlpha != 0) {
                shadow.copyColorFrom(currentFrame, x, y, x, y);
            } else {
                double currentX = x;
                double currentY = y;

                while (currentFrame.inBounds((int) currentX, (int) currentY)) {
                    currentAlpha = currentFrame.getAlpha((int) currentX, (int) currentY);
                    if (currentAlpha != 0) {
                        shadow.setRed((int) shadowColor.red, x, y);
                        shadow.setGreen((int) shadowColor.green, x, y);
                        shadow.setBlue((int) shadowColor.blue, x, y);
                        shadow.setAlpha(shadowAlpha, x, y);
                        break;
                    }
                    currentX -= normalizedVector.x;
                    currentY -= normalizedVector.y;
                }
            }
        });

        return shadow;
    }

    @Override
    protected void initializeValueProviderInternal() {
        shadowAlphaProvider = new DoubleProvider(0.0, 1.0, new MultiKeyframeBasedDoubleInterpolator(0.7));
        shadowColorProvider = ColorProvider.fromDefaultValue(0, 0, 0);
        directionProvider = LineProvider.ofNormalizedScreenCoordinates(0.4, 0.4, 0.6, 0.6);

    }

    @Override
    protected List<ValueProviderDescriptor> getValueProvidersInternal() {
        ValueProviderDescriptor directionProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(directionProvider)
                .withName("Direction")
                .build();
        ValueProviderDescriptor shadowColorProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(shadowColorProvider)
                .withName("Shadow color")
                .build();
        ValueProviderDescriptor shadowAlphaProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(shadowAlphaProvider)
                .withName("Shadow alpha")
                .build();

        return List.of(directionProviderDescriptor, shadowColorProviderDescriptor, shadowAlphaProviderDescriptor);
    }

    @Override
    public StatelessEffect cloneEffect(CloneRequestMetadata cloneRequestMetadata) {
        return new DirectionalShadowEffect(this, cloneRequestMetadata);
    }

}
