package com.helospark.tactview.core.timeline.effect.stabilize;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.StatelessVideoEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.timeline.longprocess.LongProcessAware;
import com.helospark.tactview.core.timeline.longprocess.LongProcessFrameRequest;
import com.helospark.tactview.core.timeline.longprocess.LongProcessImagePushRequest;
import com.helospark.tactview.core.timeline.longprocess.LongProcessRequestor;
import com.helospark.tactview.core.timeline.longprocess.LongProcessVisualImagePushAware;
import com.helospark.tactview.core.util.ReflectionUtil;

public class StabilizeVideoEffect extends StatelessVideoEffect implements LongProcessAware, LongProcessVisualImagePushAware {
    private LongProcessRequestor longProcessRequestor;

    public StabilizeVideoEffect(TimelineInterval interval) {
        super(interval);
    }

    public StabilizeVideoEffect(StabilizeVideoEffect blurEffect, CloneRequestMetadata cloneRequestMetadata) {
        super(blurEffect, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(blurEffect, this);
    }

    public StabilizeVideoEffect(JsonNode node, LoadMetadata loadMetadata) {
        super(node, loadMetadata);

    }

    @Override
    public ReadOnlyClipImage createFrame(StatelessEffectRequest request) {
        return ClipImage.copyOf(request.getCurrentFrame());
    }

    @Override
    public void initializeValueProvider() {
    }

    @Override
    public List<ValueProviderDescriptor> getValueProviders() {
        return List.of();
    }

    @Override
    public StatelessEffect cloneEffect(CloneRequestMetadata cloneRequestMetadata) {
        return new StabilizeVideoEffect(this, cloneRequestMetadata);
    }

    @Override
    public void notifyAfterResize() {
        super.notifyAfterResize();

        LongProcessFrameRequest request = LongProcessFrameRequest.builder().build();

        longProcessRequestor.requestFrames(this, request);
    }

    @Override
    public void notifyAfterInitialized() {
        super.notifyAfterInitialized();
        longProcessRequestor.requestFrames(this, null);
    }

    @Override
    public void setLongProcessRequestor(LongProcessRequestor longProcessRequestor) {
        this.longProcessRequestor = longProcessRequestor;
    }

    @Override
    public void beginToPushLongImages() {
        System.out.println("Beginning to receive long images");
    }

    @Override
    public void longProcessImage(LongProcessImagePushRequest pushRequest) {
        System.out.println("Long process image " + pushRequest.getPosition());
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void endToPushLongImages() {
        System.out.println("End to receive long images");
    }

}
