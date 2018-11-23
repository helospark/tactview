package com.helospark.tactview.core.timeline.proceduralclip.singlecolor;

import java.nio.ByteBuffer;
import java.util.List;

import com.helospark.tactview.core.decoder.VisualMediaMetadata;
import com.helospark.tactview.core.decoder.framecache.GlobalMemoryManagerAccessor;
import com.helospark.tactview.core.timeline.ClipFrameResult;
import com.helospark.tactview.core.timeline.GetFrameRequest;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Color;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ColorProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.IntegerProvider;
import com.helospark.tactview.core.timeline.proceduralclip.ProceduralVisualClip;
import com.helospark.tactview.core.util.IndependentPixelOperation;

public class SingleColorProceduralClip extends ProceduralVisualClip {
    private ColorProvider colorProvider;
    private IntegerProvider alphaProvider;

    private IndependentPixelOperation independentPixelOperation;

    public SingleColorProceduralClip(VisualMediaMetadata visualMediaMetadata, TimelineInterval interval, IndependentPixelOperation independentPixelOperation) {
        super(visualMediaMetadata, interval);
        this.independentPixelOperation = independentPixelOperation;
    }

    public SingleColorProceduralClip(SingleColorProceduralClip singleColorProceduralClip) {
        super(singleColorProceduralClip);
        this.independentPixelOperation = singleColorProceduralClip.independentPixelOperation;
    }

    @Override
    public ClipFrameResult createProceduralFrame(GetFrameRequest request, TimelinePosition relativePosition) {
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

        independentPixelOperation.executePixelTransformation(width, height, (x, y) -> {
            frameResult.setPixel(colorComponents, x, y);
        });

        return applyEffects(relativePosition, frameResult, request);
    }

    @Override
    public List<ValueProviderDescriptor> getDescriptorsInternal() {
        List<ValueProviderDescriptor> result = super.getDescriptorsInternal();

        colorProvider = new ColorProvider(new DoubleProvider(new MultiKeyframeBasedDoubleInterpolator(1.0)),
                new DoubleProvider(new MultiKeyframeBasedDoubleInterpolator(1.0)),
                new DoubleProvider(new MultiKeyframeBasedDoubleInterpolator(1.0)));

        alphaProvider = new IntegerProvider(0, 255, new MultiKeyframeBasedDoubleInterpolator(TimelinePosition.ofZero(), 255.0));

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
    protected TimelineClip cloneClip() {
        return new SingleColorProceduralClip(this);
    }

}
