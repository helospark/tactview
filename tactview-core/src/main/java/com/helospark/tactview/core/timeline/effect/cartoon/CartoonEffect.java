package com.helospark.tactview.core.timeline.effect.cartoon;

import java.util.Collections;
import java.util.List;

import com.helospark.tactview.core.timeline.ClipFrameResult;
import com.helospark.tactview.core.timeline.StatelessVideoEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;
import com.helospark.tactview.core.timeline.effect.cartoon.opencv.OpenCVCartoonEffectImplementation;
import com.helospark.tactview.core.timeline.effect.cartoon.opencv.OpenCVCartoonRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;

public class CartoonEffect extends StatelessVideoEffect {
    private OpenCVCartoonEffectImplementation implementation;

    public CartoonEffect(TimelineInterval interval, OpenCVCartoonEffectImplementation implementation) {
        super(interval);
        this.implementation = implementation;
    }

    @Override
    public ClipFrameResult createFrame(StatelessEffectRequest request) {
        ClipFrameResult currentFrame = request.getCurrentFrame();
        ClipFrameResult result = ClipFrameResult.sameSizeAs(currentFrame);

        OpenCVCartoonRequest nativeRequest = new OpenCVCartoonRequest();
        nativeRequest.input = currentFrame.getBuffer();
        nativeRequest.output = result.getBuffer();
        nativeRequest.width = currentFrame.getWidth();
        nativeRequest.height = currentFrame.getHeight();

        implementation.cartoon(nativeRequest);

        return result;
    }

    @Override
    public List<ValueProviderDescriptor> getValueProviders() {
        return Collections.emptyList();
    }

}
