package com.helospark.tactview.core.it.util.ui;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.repository.ProjectRepository;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.timeline.TimelineManagerFramesRequest;
import com.helospark.tactview.core.timeline.TimelineManagerRenderService;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;

@Component
public class FakePreviewRenderer {
    private TimelineManagerRenderService timelineManagerRenderService;
    private ProjectRepository projectRepository;

    public FakePreviewRenderer(TimelineManagerRenderService timelineManagerRenderService, ProjectRepository projectRepository) {
        this.timelineManagerRenderService = timelineManagerRenderService;
        this.projectRepository = projectRepository;
    }

    public ReadOnlyClipImage renderFrame(TimelineManagerAccessor timelineManager, double scale, TimelinePosition timelinePosition) {
        TimelineManagerFramesRequest frameRequest = TimelineManagerFramesRequest.builder()
                .withNeedSound(false)
                .withPosition(timelinePosition)
                .withPreviewWidth((int) (scale * projectRepository.getWidth()))
                .withPreviewHeight((int) (scale * projectRepository.getHeight()))
                .withScale(scale)
                .build();
        var frame = timelineManagerRenderService.getFrame(frameRequest);

        ReadOnlyClipImage videoFrame = frame.getAudioVideoFragment().getVideoResult();
        return videoFrame;
    }

}
