package com.helospark.tactview.core.timeline.effect.scale;

import java.util.Arrays;
import java.util.List;

import com.helospark.tactview.core.decoder.framecache.GlobalMemoryManagerAccessor;
import com.helospark.tactview.core.timeline.ClipFrameResult;
import com.helospark.tactview.core.timeline.StatelessVideoEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.DoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;

public class ScaleEffect extends StatelessVideoEffect {
    private DoubleProvider widthScale;
    private DoubleProvider heightScale;

    private OpenCVScaleEffectImplementation implementation;

    public ScaleEffect(TimelineInterval interval, OpenCVScaleEffectImplementation implementation) {
        super(interval);
        this.implementation = implementation;
    }

    @Override
    public ClipFrameResult createFrame(StatelessEffectRequest request) {
        ClipFrameResult currentFrame = request.getCurrentFrame();
        int newWidth = (int) (currentFrame.getWidth() * widthScale.getValueAt(request.getClipPosition()));
        int newHeight = (int) (currentFrame.getHeight() * heightScale.getValueAt(request.getClipPosition()));

        OpenCVScaleRequest nativeRequest = new OpenCVScaleRequest();
        nativeRequest.input = currentFrame.getBuffer();
        nativeRequest.interpolationType = 0; // todo
        nativeRequest.newHeight = newHeight;
        nativeRequest.newWidth = newWidth;
        nativeRequest.originalWidth = currentFrame.getWidth();
        nativeRequest.originalHeight = currentFrame.getHeight();
        nativeRequest.output = GlobalMemoryManagerAccessor.memoryManager.requestBuffer(newWidth * newHeight * 4);

        implementation.scaleImage(nativeRequest);

        return new ClipFrameResult(nativeRequest.output, newWidth, newHeight);
    }

    @Override
    public List<ValueProviderDescriptor> getValueProviders() {
        widthScale = new DoubleProvider(0, 20, new DoubleInterpolator(TimelinePosition.ofZero(), 1.0));
        heightScale = new DoubleProvider(0, 20, new DoubleInterpolator(TimelinePosition.ofZero(), 1.0));

        ValueProviderDescriptor widthDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(widthScale)
                .withName("width scale")
                .build();

        ValueProviderDescriptor heightDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(heightScale)
                .withName("height scale")
                .build();

        return Arrays.asList(widthDescriptor, heightDescriptor);
    }

}
