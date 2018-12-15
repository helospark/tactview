package com.helospark.tactview.core.timeline.effect.rotate;

import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.decoder.framecache.GlobalMemoryManagerAccessor;
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
import com.helospark.tactview.core.util.ReflectionUtil;

public class RotateEffect extends StatelessVideoEffect {
    private DoubleProvider angleProvider;

    private OpenCVRotateEffectImplementation implementation;

    public RotateEffect(TimelineInterval interval, OpenCVRotateEffectImplementation implementation) {
        super(interval);
        this.implementation = implementation;
    }

    public RotateEffect(RotateEffect cloneFrom) {
        super(cloneFrom);
        ReflectionUtil.copyOrCloneFieldFromTo(cloneFrom, this);
    }

    public RotateEffect(JsonNode node, LoadMetadata loadMetadata, OpenCVRotateEffectImplementation implementation2) {
        super(node, loadMetadata);
        this.implementation = implementation2;
    }

    @Override
    public ReadOnlyClipImage createFrame(StatelessEffectRequest request) {
        double degrees = angleProvider.getValueAt(request.getClipPosition());

        int originalWidth = request.getCurrentFrame().getWidth();
        int originalHeight = request.getCurrentFrame().getHeight();

        // double radians = Math.toRadians(degrees);
        // double sin = Math.sin(radians);
        // double cos = Math.cos(radians);
        // int newWidth = (int) ((originalHeight * Math.abs(sin)) + (originalWidth * Math.abs(cos)));
        // int newHeight = (int) ((originalHeight * Math.abs(cos)) + (originalWidth * Math.abs(sin)));
        int newWidth = (int) Math.sqrt(originalHeight * originalHeight + originalWidth * originalWidth);
        int newHeight = newWidth;

        int rotationCenterX = originalWidth / 2;
        int rotationCenterY = originalHeight / 2;

        OpenCVRotateRequest nativeRequest = new OpenCVRotateRequest();
        nativeRequest.rotationDegrees = degrees;
        nativeRequest.rotationPointX = rotationCenterX;
        nativeRequest.rotationPointY = rotationCenterY;
        nativeRequest.input = request.getCurrentFrame().getBuffer();
        nativeRequest.originalWidth = originalWidth;
        nativeRequest.originalHeight = originalHeight;
        nativeRequest.output = GlobalMemoryManagerAccessor.memoryManager.requestBuffer(newWidth * newHeight * 4);
        nativeRequest.newWidth = newWidth;
        nativeRequest.newHeight = newHeight;

        implementation.rotateImage(nativeRequest);

        return new ClipImage(nativeRequest.output, newWidth, newHeight);
    }

    @Override
    public void initializeValueProvider() {
        angleProvider = new DoubleProvider(0, 360, new MultiKeyframeBasedDoubleInterpolator(0.0));
    }

    @Override
    public List<ValueProviderDescriptor> getValueProviders() {

        ValueProviderDescriptor angleDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(angleProvider)
                .withName("angle")
                .build();

        return Arrays.asList(angleDescriptor);
    }

    @Override
    public StatelessEffect cloneEffect() {
        return new RotateEffect(this);
    }

}
