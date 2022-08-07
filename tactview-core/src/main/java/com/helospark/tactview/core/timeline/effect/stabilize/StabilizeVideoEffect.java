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
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.factory.functional.doubleinterpolator.impl.ConstantInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.IntegerProvider;
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
    private static final String FINISHED_POSTFIX = "_finished";

    private static final String WOBBLE_FILE_NAME = "motion_2_";

    private static final String MOTION_FILE_NAME = "motion_";

    private LongProcessRequestor longProcessRequestor;

    private OpenCVStabilizeVideoService openCVStabilizeVideoService;
    private ProjectRepository projectRepository;

    private IntegerProvider smoothingRadiusProvider;

    private volatile boolean uptoDateData = false;
    private volatile int stabilizerContextIndex = -1;

    public StabilizeVideoEffect(TimelineInterval interval, OpenCVStabilizeVideoService openCVStabilizeVideoService, ProjectRepository projectRepository) {
        super(interval);
        this.openCVStabilizeVideoService = openCVStabilizeVideoService;
        this.projectRepository = projectRepository;
    }

    public StabilizeVideoEffect(StabilizeVideoEffect blurEffect, CloneRequestMetadata cloneRequestMetadata) {
        super(blurEffect, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(blurEffect, this, cloneRequestMetadata);
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
            nativeRequest.index = stabilizerContextIndex;
            nativeRequest.frameIndex = request.getEffectPosition().getSeconds().divide(projectRepository.getFrameTime(), 10, RoundingMode.HALF_UP).intValue();

            openCVStabilizeVideoService.createStabilizedFrame(nativeRequest);

            return result;
        } else {
            return ClipImage.copyOf(request.getCurrentFrame());
        }
    }

    @Override
    protected void initializeValueProviderInternal() {
        smoothingRadiusProvider = new IntegerProvider(1, 500, new ConstantInterpolator(200.0));
    }

    @Override
    protected List<ValueProviderDescriptor> getValueProvidersInternal() {
        ValueProviderDescriptor radiusDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(smoothingRadiusProvider)
                .withName("Smoothing radius")
                .build();

        return List.of(radiusDescriptor);
    }

    @Override
    public StatelessEffect cloneEffect(CloneRequestMetadata cloneRequestMetadata) {
        return new StabilizeVideoEffect(this, cloneRequestMetadata);
    }

    @Override
    public void notifyAfterResize() {
        super.notifyAfterResize();

        requestLongProcess();
    }

    @Override
    public void notifyAfterInitialized() {
        super.notifyAfterInitialized();
        requestLongProcess();
    }

    @Override
    public void setLongProcessRequestor(LongProcessRequestor longProcessRequestor) {
        this.longProcessRequestor = longProcessRequestor;
    }

    @Override
    public void beginToPushLongImages() {
        System.out.println("Beginning to receive long images");

        uptoDateData = false;
        File motionFile = getOrCreateMotionFile(MOTION_FILE_NAME, FINISHED_POSTFIX);
        File wobbleFile = getOrCreateMotionFile(WOBBLE_FILE_NAME, FINISHED_POSTFIX);

        StabilizationInitRequest nativeRequest = new StabilizationInitRequest();
        nativeRequest.width = projectRepository.getWidth();
        nativeRequest.height = projectRepository.getHeight();
        nativeRequest.motionFile = motionFile.getAbsolutePath();
        nativeRequest.motion2File = wobbleFile.getAbsolutePath();
        nativeRequest.radius = smoothingRadiusProvider.getValueWithoutScriptAt(TimelinePosition.ofZero());

        stabilizerContextIndex = openCVStabilizeVideoService.initializeStabilizer(nativeRequest);
    }

    private File getOrCreateMotionFile(String filename, String finishedPostfix) {
        File inProgressFile = getMotionFileName(filename);
        File finishedFile = getFinishedMotionFile(filename, finishedPostfix);
        if (finishedFile.exists()) {
            return finishedFile;
        } else if (inProgressFile.exists()) {
            inProgressFile.delete();
            // will be created on native file
            return inProgressFile;
        } else {
            return inProgressFile;
        }
    }

    private File getFinishedMotionFile(String filename, String finishedPostfix) {
        return new File(System.getProperty("java.io.tmpdir"), filename + getId() + finishedPostfix);
    }

    private File getMotionFileName(String filename) {
        return new File(System.getProperty("java.io.tmpdir"), filename + getId());
    }

    @Override
    public void longProcessImage(LongProcessImagePushRequest pushRequest) {
        AddStabilizeFrameRequest addFrameRequest = new AddStabilizeFrameRequest();
        addFrameRequest.width = pushRequest.getImage().getWidth();
        addFrameRequest.height = pushRequest.getImage().getHeight();
        addFrameRequest.input = pushRequest.getImage().getBuffer();
        addFrameRequest.index = stabilizerContextIndex;

        openCVStabilizeVideoService.addFrame(addFrameRequest);
    }

    @Override
    public void abortedLongImagePush() {
        if (stabilizerContextIndex != -1) {
            openCVStabilizeVideoService.deallocate(stabilizerContextIndex);
        }
    }

    @Override
    public void endToPushLongImages() {
        openCVStabilizeVideoService.finishedAddingFrames(stabilizerContextIndex);
        uptoDateData = true;
        renamedToFinishedFile(MOTION_FILE_NAME);
        renamedToFinishedFile(WOBBLE_FILE_NAME);
        System.out.println("End to receive long images");
    }

    private void renamedToFinishedFile(String motionFileName) {
        File inProgressFile = getMotionFileName(motionFileName);
        File finishedFile = getFinishedMotionFile(motionFileName, FINISHED_POSTFIX);
        inProgressFile.renameTo(finishedFile);
    }

    @Override
    public void effectChanged(EffectChangedRequest request) {
        super.effectChanged(request);
        requestLongProcess();
    }

    private void requestLongProcess() {
        LongProcessFrameRequest request = LongProcessFrameRequest.builder().build();
        longProcessRequestor.requestFrames(this, request);
    }

}
