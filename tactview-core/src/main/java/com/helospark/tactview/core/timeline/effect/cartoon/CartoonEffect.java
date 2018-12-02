package com.helospark.tactview.core.timeline.effect.cartoon;

import java.util.Collections;
import java.util.List;

import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.StatelessVideoEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;
import com.helospark.tactview.core.timeline.effect.cartoon.opencv.OpenCVCartoonEffectImplementation;
import com.helospark.tactview.core.timeline.effect.cartoon.opencv.OpenCVCartoonRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.util.ReflectionUtil;

public class CartoonEffect extends StatelessVideoEffect {
    private OpenCVCartoonEffectImplementation implementation;

    public CartoonEffect(TimelineInterval interval, OpenCVCartoonEffectImplementation implementation) {
        super(interval);
        this.implementation = implementation;
    }

    public CartoonEffect(CartoonEffect cloneFrom) {
        super(cloneFrom);
        ReflectionUtil.copyOrCloneFieldFromTo(cloneFrom, this);
    }

    @Override
    public ClipImage createFrame(StatelessEffectRequest request) {
        ReadOnlyClipImage currentFrame = request.getCurrentFrame();
        ClipImage result = ClipImage.sameSizeAs(currentFrame);

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

    @Override
    public StatelessEffect cloneEffect() {
        return new CartoonEffect(this);
    }

}
