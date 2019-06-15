package com.helospark.tactview.core.timeline.effect.stabilize;

import java.io.File;
import java.math.RoundingMode;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.repository.ProjectRepository;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.StatelessVideoEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.stabilize.impl.AddStabilizeFrameRequest;
import com.helospark.tactview.core.timeline.effect.stabilize.impl.OpenCVStabilizeVideoService;
import com.helospark.tactview.core.timeline.effect.stabilize.impl.StabilizationInitRequest;
import com.helospark.tactview.core.timeline.effect.stabilize.impl.StabilizeFrameRequest;
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

    private OpenCVStabilizeVideoService openCVStabilizeVideoService;
    private ProjectRepository projectRepository;

    private volatile boolean uptoDateData = false;

    public StabilizeVideoEffect(TimelineInterval interval, OpenCVStabilizeVideoService openCVStabilizeVideoService, ProjectRepository projectRepository) {
        super(interval);
        this.openCVStabilizeVideoService = openCVStabilizeVideoService;
        this.projectRepository = projectRepository;
    }

    public StabilizeVideoEffect(StabilizeVideoEffect blurEffect, CloneRequestMetadata cloneRequestMetadata) {
        super(blurEffect, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(blurEffect, this);
    }

    public StabilizeVideoEffect(JsonNode node, LoadMetadata loadMetadata, OpenCVStabilizeVideoService openCVStabilizeVideoService, ProjectRepository projectRepository) {
        super(node, loadMetadata);
        this.openCVStabilizeVideoService = openCVStabilizeVideoService;
        this.projectRepository = projectRepository;

    }

    @Override
    public ReadOnlyClipImage createFrame(StatelessEffectRequest request) {
        if (uptoDateData) {
            StabilizeFrameRequest nativeRequest = new StabilizeFrameRequest();

            ClipImage result = ClipImage.sameSizeAs(request.getCurrentFrame());

            nativeRequest.input = request.getCurrentFrame().getBuffer();
            nativeRequest.width = request.getCurrentFrame().getWidth();
            nativeRequest.height = request.getCurrentFrame().getHeight();
            nativeRequest.output = result.getBuffer();
            nativeRequest.frameIndex = request.getEffectPosition().getSeconds().divide(projectRepository.getFrameTime(), 10, RoundingMode.HALF_UP).intValue();

            openCVStabilizeVideoService.createStabilizedFrame(nativeRequest);

            return result;
        } else {
            return ClipImage.copyOf(request.getCurrentFrame());
        }
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

        uptoDateData = false;

        StabilizationInitRequest nativeRequest = new StabilizationInitRequest();
        nativeRequest.width = projectRepository.getWidth();
        nativeRequest.height = projectRepository.getHeight();
        nativeRequest.motionFile = new File(System.getProperty("java.io.tmpdir"), "motion_" + getId()).getAbsolutePath();
        nativeRequest.motion2File = new File(System.getProperty("java.io.tmpdir"), "motion_2_" + getId()).getAbsolutePath();
        nativeRequest.radius = 300;

        openCVStabilizeVideoService.initializeStabilizer(nativeRequest);
    }

    @Override
    public void longProcessImage(LongProcessImagePushRequest pushRequest) {
        AddStabilizeFrameRequest addFrameRequest = new AddStabilizeFrameRequest();
        addFrameRequest.width = pushRequest.getImage().getWidth();
        addFrameRequest.height = pushRequest.getImage().getHeight();
        addFrameRequest.input = pushRequest.getImage().getBuffer();

        openCVStabilizeVideoService.addFrame(addFrameRequest);

        System.out.println("Long process image " + pushRequest.getPosition());
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void endToPushLongImages() {
        openCVStabilizeVideoService.finishedAddingFrames();
        uptoDateData = true;
        System.out.println("End to receive long images");
    }

}
