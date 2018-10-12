package com.helospark.tactview.core.timeline.proceduralclip;

import java.nio.ByteBuffer;
import java.util.List;

import com.helospark.tactview.core.decoder.VisualMediaMetadata;
import com.helospark.tactview.core.decoder.framecache.GlobalMemoryManagerAccessor;
import com.helospark.tactview.core.timeline.ClipFrameResult;
import com.helospark.tactview.core.timeline.GetFrameRequest;
import com.helospark.tactview.core.timeline.TimelineClipType;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.VisualTimelineClip;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.DoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.IntegerProvider;

public class SingleColorProceduralClip extends VisualTimelineClip {
    private IntegerProvider redProvider;
    private IntegerProvider greenProvider;
    private IntegerProvider blueProvider;
    private IntegerProvider alphaProvider;

    public SingleColorProceduralClip(VisualMediaMetadata visualMediaMetadata, TimelineInterval interval) {
        super(visualMediaMetadata, interval, TimelineClipType.IMAGE);
    }

    @Override
    public ClipFrameResult getFrame(GetFrameRequest request) {
        TimelinePosition relativePosition = request.getPosition().from(getInterval().getStartPosition());

        int width = request.getExpectedWidth();
        int height = request.getExpectedHeight();

        ByteBuffer buffer = GlobalMemoryManagerAccessor.memoryManager.requestBuffer(width * height * 4);
        ClipFrameResult frameResult = new ClipFrameResult(buffer, width, height);

        int[] color = new int[] { redProvider.getValueAt(relativePosition),
                greenProvider.getValueAt(relativePosition),
                blueProvider.getValueAt(relativePosition),
                alphaProvider.getValueAt(relativePosition) };

        for (int i = 0; i < height; ++i) {
            for (int j = 0; j < width; ++j) {
                frameResult.setPixel(color, j, i);
            }
        }

        return applyEffects(relativePosition, frameResult);
    }

    @Override
    public ByteBuffer requestFrame(TimelinePosition position, int width, int height) {
        // TODO something is very wrong here
        throw new IllegalStateException();
    }

    @Override
    public List<ValueProviderDescriptor> getDescriptors() {
        List<ValueProviderDescriptor> result = super.getDescriptors();

        redProvider = new IntegerProvider(0, 255, new DoubleInterpolator(TimelinePosition.ofZero(), 255.0));
        greenProvider = new IntegerProvider(0, 255, new DoubleInterpolator(TimelinePosition.ofZero(), 255.0));
        blueProvider = new IntegerProvider(0, 255, new DoubleInterpolator(TimelinePosition.ofZero(), 255.0));
        alphaProvider = new IntegerProvider(0, 255, new DoubleInterpolator(TimelinePosition.ofZero(), 255.0));

        ValueProviderDescriptor redDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(redProvider)
                .withName("red")
                .build();
        ValueProviderDescriptor greenDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(greenProvider)
                .withName("green")
                .build();
        ValueProviderDescriptor blueDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(blueProvider)
                .withName("blue")
                .build();
        ValueProviderDescriptor alphaDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(alphaProvider)
                .withName("alpha")
                .build();

        result.add(redDescriptor);
        result.add(greenDescriptor);
        result.add(blueDescriptor);
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

}
