package com.helospark.tactview.core.timeline.effect.rotate;

import java.util.Arrays;
import java.util.List;

import com.helospark.tactview.core.decoder.framecache.GlobalMemoryManagerAccessor;
import com.helospark.tactview.core.timeline.ClipFrameResult;
import com.helospark.tactview.core.timeline.StatelessVideoEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;

public class RotateEffect extends StatelessVideoEffect {
    private DoubleProvider angleProvider;

    private OpenCVRotateEffectImplementation implementation;

    public RotateEffect(TimelineInterval interval, OpenCVRotateEffectImplementation implementation) {
        super(interval);
        this.implementation = implementation;
    }

    @Override
    public ClipFrameResult createFrame(StatelessEffectRequest request) {
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

        return new ClipFrameResult(nativeRequest.output, newWidth, newHeight);
    }

    @Override
    public List<ValueProviderDescriptor> getValueProviders() {
        angleProvider = new DoubleProvider(0, 360, new MultiKeyframeBasedDoubleInterpolator(TimelinePosition.ofZero(), 0.0));

        ValueProviderDescriptor angleDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(angleProvider)
                .withName("angle")
                .build();

        return Arrays.asList(angleDescriptor);
    }

}
