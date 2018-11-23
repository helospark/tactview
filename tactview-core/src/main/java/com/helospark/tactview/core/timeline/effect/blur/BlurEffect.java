package com.helospark.tactview.core.timeline.effect.blur;

import static com.helospark.tactview.core.timeline.effect.interpolation.provider.SizeFunction.IMAGE_SIZE_IN_0_to_1_RANGE;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

import com.helospark.tactview.core.decoder.framecache.GlobalMemoryManagerAccessor;
import com.helospark.tactview.core.timeline.ClipFrameResult;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.StatelessVideoEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;
import com.helospark.tactview.core.timeline.effect.blur.opencv.OpenCVBasedGaussianBlur;
import com.helospark.tactview.core.timeline.effect.blur.opencv.OpenCVGaussianBlurRequest;
import com.helospark.tactview.core.timeline.effect.blur.opencv.OpenCVRegion;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.IntegerProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.LineProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.PointProvider;
import com.helospark.tactview.core.util.ReflectionUtil;

public class BlurEffect extends StatelessVideoEffect {
    private OpenCVBasedGaussianBlur openCVBasedBlur;
    private IntegerProvider kernelHeightProvider;
    private IntegerProvider kernelWidthProvider;

    private PointProvider topLeftPointProvider;
    private PointProvider bottomRightPointProvider;

    public BlurEffect(TimelineInterval interval, OpenCVBasedGaussianBlur openCVBasedBlur) {
        super(interval);
        this.openCVBasedBlur = openCVBasedBlur;
    }

    public BlurEffect(BlurEffect blurEffect) {
        super(blurEffect);
        ReflectionUtil.copyOrCloneFieldFromTo(blurEffect, this);
    }

    @Override
    public ClipFrameResult createFrame(StatelessEffectRequest request) {
        ByteBuffer buffer = GlobalMemoryManagerAccessor.memoryManager.requestBuffer(request.getCurrentFrame().getBuffer().capacity());
        ClipFrameResult currentFrame = request.getCurrentFrame();
        OpenCVGaussianBlurRequest nativeRequest = new OpenCVGaussianBlurRequest();
        nativeRequest.input = currentFrame.getBuffer();
        nativeRequest.output = buffer;
        nativeRequest.width = currentFrame.getWidth();
        nativeRequest.height = currentFrame.getHeight();
        nativeRequest.kernelWidth = (int) (kernelWidthProvider.getValueAt(request.getEffectPosition()) * request.getScale()) * 2 + 1;
        nativeRequest.kernelHeight = (int) (kernelHeightProvider.getValueAt(request.getEffectPosition()) * request.getScale()) * 2 + 1;
        nativeRequest.blurRegion = createBlurRegion(request);
        openCVBasedBlur.applyGaussianBlur(nativeRequest);

        return new ClipFrameResult(buffer, currentFrame.getWidth(), currentFrame.getHeight());
    }

    private OpenCVRegion createBlurRegion(StatelessEffectRequest request) {
        OpenCVRegion region = new OpenCVRegion();
        Point topLeft = topLeftPointProvider.getValueAt(request.getEffectPosition());
        Point bottomRight = bottomRightPointProvider.getValueAt(request.getEffectPosition());
        region.x = (int) (topLeft.x * request.getCurrentFrame().getWidth());
        region.y = (int) (topLeft.y * request.getCurrentFrame().getHeight());

        int rightX = (int) (bottomRight.x * request.getCurrentFrame().getWidth());
        int rightY = (int) (bottomRight.y * request.getCurrentFrame().getHeight());

        region.width = rightX - region.x;
        region.height = rightY - region.y;

        // TODO: better clamping
        if (region.width < 10) {
            region.width = 10;
        }
        if (region.height < 10) {
            region.height = 10;
        }

        return region;
    }

    @Override
    public List<ValueProviderDescriptor> getValueProviders() {
        kernelWidthProvider = new IntegerProvider(0, 100, new MultiKeyframeBasedDoubleInterpolator(20.0));
        kernelHeightProvider = new IntegerProvider(0, 100, new MultiKeyframeBasedDoubleInterpolator(20.0));
        kernelHeightProvider.setScaleDependent();
        kernelHeightProvider.setScaleDependent();

        topLeftPointProvider = new PointProvider(doubleProviderWithDefaultValue(0.0), doubleProviderWithDefaultValue(0.0));
        bottomRightPointProvider = new PointProvider(doubleProviderWithDefaultValue(1.0), doubleProviderWithDefaultValue(1.0));

        LineProvider lineProvider = new LineProvider(topLeftPointProvider, bottomRightPointProvider);

        ValueProviderDescriptor sizeDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(lineProvider)
                .withName("area")
                .build();

        ValueProviderDescriptor widthDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(kernelWidthProvider)
                .withName("kernelWidth")
                .build();

        ValueProviderDescriptor heightDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(kernelHeightProvider)
                .withName("kernelWidth")
                .build();

        return Arrays.asList(widthDescriptor, heightDescriptor, sizeDescriptor);
    }

    private DoubleProvider doubleProviderWithDefaultValue(double defaultValue) {
        return new DoubleProvider(IMAGE_SIZE_IN_0_to_1_RANGE, new MultiKeyframeBasedDoubleInterpolator(defaultValue));
    }

    @Override
    public StatelessEffect cloneEffect() {
        return new BlurEffect(this);
    }

}
