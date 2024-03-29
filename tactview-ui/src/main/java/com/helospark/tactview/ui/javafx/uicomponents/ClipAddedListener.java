package com.helospark.tactview.ui.javafx.uicomponents;

import java.math.BigDecimal;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.decoder.VideoMetadata;
import com.helospark.tactview.core.decoder.VisualMediaMetadata;
import com.helospark.tactview.core.repository.ProjectRepository;
import com.helospark.tactview.core.timeline.AudibleTimelineClip;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.VideoClip;
import com.helospark.tactview.core.timeline.VisualTimelineClip;
import com.helospark.tactview.core.timeline.message.ClipAddedMessage;
import com.helospark.tactview.core.util.MathUtil;
import com.helospark.tactview.core.util.logger.Slf4j;
import com.helospark.tactview.ui.javafx.UiMessagingService;
import com.helospark.tactview.ui.javafx.menu.defaultmenus.projectsize.ProjectSizeInitializer;
import com.helospark.tactview.ui.javafx.repository.NameToIdRepository;

@Component
public class ClipAddedListener {
    private UiMessagingService messagingService;
    private ProjectRepository projectRepository;
    private ProjectSizeInitializer projectSizeInitializer;
    private NameToIdRepository nameToIdRepository;
    private TimelineState timelineState;

    @Slf4j
    private Logger logger;

    public ClipAddedListener(UiMessagingService messagingService, ProjectRepository projectRepository, ProjectSizeInitializer projectSizeInitializer, NameToIdRepository nameToIdRepository,
            TimelineState timelineState) {
        this.messagingService = messagingService;
        this.projectRepository = projectRepository;
        this.projectSizeInitializer = projectSizeInitializer;
        this.nameToIdRepository = nameToIdRepository;
        this.timelineState = timelineState;
    }

    @PostConstruct
    public void setUp() {
        messagingService.register(ClipAddedMessage.class, message -> addClip(message));
    }

    private void addClip(ClipAddedMessage message) {
        TimelineClip clip = message.getClip();
        initializeProjectOnFirstVideoClipAdded(clip);
        nameToIdRepository.generateAndAddNameForIdIfNotPresent(clip.getClass().getSimpleName(), clip.getId());

        logger.debug("Clip {} added successfuly", message.getClipId());
    }

    private void initializeProjectOnFirstVideoClipAdded(TimelineClip clip) {
        if (!projectRepository.isVideoInitialized() && clip instanceof VisualTimelineClip) {
            VisualTimelineClip visualClip = (VisualTimelineClip) clip;
            VisualMediaMetadata metadata = visualClip.getMediaMetadata();
            int width = metadata.getWidth();
            int height = metadata.getHeight();

            if (metadata instanceof VideoMetadata && visualClip instanceof VideoClip) {
                double rotation = ((VideoMetadata) metadata).getRotation();
                if (MathUtil.fuzzyEquals(Math.abs(rotation), 90.0) && ((VideoClip) visualClip).isRotationEnabledAt(TimelinePosition.ofZero(), null)) {
                    int tmp = width;
                    width = height;
                    height = tmp;
                }
            }

            BigDecimal fps = metadata instanceof VideoMetadata ? new BigDecimal(((VideoMetadata) metadata).getFps()) : new BigDecimal("30");

            projectSizeInitializer.initializeProjectSize(width, height, fps);
        }
        if (!projectRepository.isAudioInitialized() && clip instanceof AudibleTimelineClip) {
            AudibleTimelineClip audioClip = (AudibleTimelineClip) clip;
            int sampleRate = audioClip.getMediaMetadata().getSampleRate();
            int bytesPerSample = audioClip.getMediaMetadata().getBytesPerSample();
            int numberOfChannels = audioClip.getMediaMetadata().getChannels();
            projectRepository.initializeAudio(sampleRate, bytesPerSample, numberOfChannels);
        }
    }

}
