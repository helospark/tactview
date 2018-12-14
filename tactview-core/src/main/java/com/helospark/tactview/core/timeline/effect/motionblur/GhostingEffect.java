package com.helospark.tactview.core.timeline.effect.motionblur;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.api.LoadMetadata;
import com.helospark.tactview.core.decoder.framecache.GlobalMemoryManagerAccessor;
import com.helospark.tactview.core.timeline.GetFrameRequest;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.StatelessVideoEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.VisualTimelineClip;
import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.IntegerProvider;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.util.IndependentPixelOperation;
import com.helospark.tactview.core.util.ReflectionUtil;

// Known limitation: cannot be used on a clip which depends on other clips
public class GhostingEffect extends StatelessVideoEffect {
    private IndependentPixelOperation independentPixelOperation;

    private IntegerProvider numberOfGhostProvider;
    private DoubleProvider ghostTimeProvider;
    private DoubleProvider alphaProvider;

    public GhostingEffect(TimelineInterval interval, IndependentPixelOperation independentPixelOperation) {
        super(interval);
        this.independentPixelOperation = independentPixelOperation;
    }

    public GhostingEffect(GhostingEffect ghostingEffect) {
        super(ghostingEffect);
        ReflectionUtil.copyOrCloneFieldFromTo(ghostingEffect, this);
    }

    public GhostingEffect(JsonNode node, LoadMetadata loadMetadata, IndependentPixelOperation independentPixelOperation2) {
        super(node, loadMetadata);
        this.independentPixelOperation = independentPixelOperation2;
    }

    @Override
    public ReadOnlyClipImage createFrame(StatelessEffectRequest request) {
        VisualTimelineClip clip = request.getCurrentTimelineClip();
        ReadOnlyClipImage currentFrame = request.getCurrentFrame();
        ClipImage result = ClipImage.sameSizeAs(currentFrame);

        double endAlpha = alphaProvider.getValueAt(request.getEffectPosition());
        int numberOfGhosts = numberOfGhostProvider.getValueAt(request.getEffectPosition());
        double timeStep = ghostTimeProvider.getValueAt(request.getEffectPosition());

        BigDecimal timeBetweenGhosts = BigDecimal.valueOf(timeStep);

        double alpha = endAlpha;
        int startIndex = numberOfGhosts - 2;
        for (int i = startIndex; i >= 0; --i) {
            BigDecimal absoluteEffectPosition = request.getClipPosition().getSeconds().subtract(timeBetweenGhosts.multiply(BigDecimal.valueOf(i + 1)));

            if (absoluteEffectPosition.compareTo(BigDecimal.ZERO) < 0) { // TODO: has to be part of clip
                break;
            }

            GetFrameRequest frameRequest = GetFrameRequest.builder()
                    .withApplyEffects(true)
                    .withApplyEffectsLessThanEffectChannel(Optional.of(request.getEffectChannel()))
                    .withExpectedWidth(currentFrame.getWidth())
                    .withExpectedHeight(currentFrame.getHeight())
                    .withPosition(new TimelinePosition(absoluteEffectPosition))
                    .withScale(request.getScale())
                    .build();

            ReadOnlyClipImage frame = clip.getFrame(frameRequest);

            if (i == startIndex) {
                result.copyFrom(frame);
            } else {
                mergeWithAlpha(result, frame, alpha);
            }
            GlobalMemoryManagerAccessor.memoryManager.returnBuffer(frame.getBuffer());
        }
        mergeWithAlpha(result, currentFrame, endAlpha);
        return result;
    }

    private void mergeWithAlpha(ClipImage result, ReadOnlyClipImage frame, double currentAlpha) {
        independentPixelOperation.executePixelTransformation(frame.getWidth(), frame.getHeight(), (x, y) -> {
            for (int j = 0; j < 4; ++j) {
                int firstImageColorComponent = result.getColorComponentWithOffset(x, y, j);
                int secondImageColorComponent = frame.getColorComponentWithOffset(x, y, j);

                int resultComponent = (int) (secondImageColorComponent * currentAlpha + firstImageColorComponent * (1.0 - currentAlpha));

                result.setColorComponentByOffset(resultComponent, x, y, j);
            }
        });
    }

    @Override
    public void initializeValueProvider() {
        numberOfGhostProvider = new IntegerProvider(1, 10, new MultiKeyframeBasedDoubleInterpolator(3.0));
        ghostTimeProvider = new DoubleProvider(0.0, 1.0, new MultiKeyframeBasedDoubleInterpolator(0.4));
        alphaProvider = new DoubleProvider(0.1, 0.95, new MultiKeyframeBasedDoubleInterpolator(0.5));
    }

    @Override
    public List<ValueProviderDescriptor> getValueProviders() {

        ValueProviderDescriptor numberOfGhostProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(numberOfGhostProvider)
                .withName("Number of ghosts")
                .build();

        ValueProviderDescriptor ghostTimeProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(ghostTimeProvider)
                .withName("Ghost time")
                .build();

        ValueProviderDescriptor alphaProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(alphaProvider)
                .withName("End alpha")
                .build();

        return List.of(numberOfGhostProviderDescriptor, ghostTimeProviderDescriptor, alphaProviderDescriptor);
    }

    @Override
    public StatelessEffect cloneEffect() {
        return new GhostingEffect(this);
    }

}
