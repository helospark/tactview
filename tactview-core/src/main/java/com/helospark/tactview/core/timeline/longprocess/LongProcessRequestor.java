package com.helospark.tactview.core.timeline.longprocess;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.repository.ProjectRepository;
import com.helospark.tactview.core.timeline.GetFrameRequest;
import com.helospark.tactview.core.timeline.StatelessVideoEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.VisualTimelineClip;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;

@Component
public class LongProcessRequestor {
    ExecutorService executor = Executors.newSingleThreadExecutor();

    private ProjectRepository projectRepository;
    private TimelineManagerAccessor timelineManagerAccessor;

    public LongProcessRequestor(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public <T extends StatelessVideoEffect & LongProcessVisualImagePushAware> void requestFrames(T target, LongProcessFrameRequest longProcessFrameRequest) {
        VisualTimelineClip clip = (VisualTimelineClip) timelineManagerAccessor.findClipForEffect(target.getId()).get();
        Optional<Integer> effectChannel = clip.getEffectChannelIndex(target.getId());
        executor.submit(() -> {
            TimelineInterval interval = target.getInterval();

            TimelinePosition currentPosition = interval.getStartPosition();

            BigDecimal step = BigDecimal.ONE.divide(projectRepository.getFps(), 10, RoundingMode.HALF_UP);

            target.beginToPushLongImages();

            while (currentPosition.isLessThan(interval.getEndPosition())) {
                GetFrameRequest frameRequest = GetFrameRequest.builder()
                        .withScale(1.0)
                        .withPosition(currentPosition)
                        .withExpectedWidth(projectRepository.getWidth())
                        .withExpectedHeight(projectRepository.getHeight())
                        .withApplyEffects(true)
                        .withApplyEffectsLessThanEffectChannel(effectChannel)
                        .build();

                ReadOnlyClipImage frameResult = clip.getFrame(frameRequest);

                LongProcessImagePushRequest pushRequest = LongProcessImagePushRequest.builder()
                        .withImage(frameResult)
                        .withPosition(currentPosition)
                        .build();

                target.longProcessImage(pushRequest);

                currentPosition = currentPosition.add(step);
            }
            target.endToPushLongImages();

        });
    }

    // Due to circular dependencies. TODO: avoid
    public void setTimelineManagerAccessor(TimelineManagerAccessor timelineManagerAccessor) {
        this.timelineManagerAccessor = timelineManagerAccessor;
    }
}
