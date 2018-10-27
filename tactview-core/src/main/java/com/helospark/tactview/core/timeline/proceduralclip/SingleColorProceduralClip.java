package com.helospark.tactview.core.timeline.proceduralclip;

import java.nio.ByteBuffer;
import java.util.List;

import com.helospark.tactview.core.decoder.VisualMediaMetadata;
import com.helospark.tactview.core.decoder.framecache.GlobalMemoryManagerAccessor;
import com.helospark.tactview.core.timeline.ClipFrameResult;
import com.helospark.tactview.core.timeline.GetFrameRequest;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineClipType;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.VisualTimelineClip;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.DoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Color;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ColorProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.IntegerProvider;

public class SingleColorProceduralClip extends VisualTimelineClip {
    private ColorProvider colorProvider;
    private IntegerProvider alphaProvider;

    public SingleColorProceduralClip(VisualMediaMetadata visualMediaMetadata, TimelineInterval interval) {
        super(visualMediaMetadata, interval, TimelineClipType.IMAGE);
    }

    @Override
    public ClipFrameResult getFrame(GetFrameRequest request) {
        TimelinePosition relativePosition = request.calculateRelativePositionFrom(this);

        int width = request.getExpectedWidth();
        int height = request.getExpectedHeight();

        ByteBuffer buffer = GlobalMemoryManagerAccessor.memoryManager.requestBuffer(width * height * 4);
        ClipFrameResult frameResult = new ClipFrameResult(buffer, width, height);

        Color color = colorProvider.getValueAt(relativePosition);
        int[] colorComponents = new int[]{
                (int) (color.red * 255),
                (int) (color.green * 255),
                (int) (color.blue * 255),
                alphaProvider.getValueAt(relativePosition)};

        for (int i = 0; i < height; ++i) {
            for (int j = 0; j < width; ++j) {
                frameResult.setPixel(colorComponents, j, i);
            }
        }

        return applyEffects(relativePosition, frameResult, request);
    }

    @Override
    public ByteBuffer requestFrame(TimelinePosition position, int width, int height) {
        // TODO something is very wrong here
        throw new IllegalStateException();
    }

    @Override
    public List<ValueProviderDescriptor> getDescriptors() {
        List<ValueProviderDescriptor> result = super.getDescriptors();

        colorProvider = new ColorProvider(new DoubleProvider(new DoubleInterpolator(1.0)),
                new DoubleProvider(new DoubleInterpolator(1.0)),
                new DoubleProvider(new DoubleInterpolator(1.0)));

        alphaProvider = new IntegerProvider(0, 255, new DoubleInterpolator(TimelinePosition.ofZero(), 255.0));

        ValueProviderDescriptor colorDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(colorProvider)
                .withName("red")
                .build();
        ValueProviderDescriptor alphaDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(alphaProvider)
                .withName("alpha")
                .build();

        result.add(colorDescriptor);
        result.add(alphaDescriptor);

        return result;
    }

    @Override
    public VisualMediaMetadata getMediaMetadata() {
        return mediaMetadata;
    }

    @Override
    public boolean isResizable() {
        return true;
    }

    @Override
    protected TimelineClip cloneClip() {
        return new SingleColorProceduralClip(mediaMetadata, interval);
    }

}
