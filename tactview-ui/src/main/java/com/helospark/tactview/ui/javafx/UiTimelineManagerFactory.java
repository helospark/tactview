package com.helospark.tactview.ui.javafx;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.annotation.Qualifier;
import com.helospark.tactview.core.repository.ProjectRepository;
import com.helospark.tactview.ui.javafx.audio.AudioStreamService;
import com.helospark.tactview.ui.javafx.audio.JavaByteArrayConverter;
import com.helospark.tactview.ui.javafx.repository.UiProjectRepository;
import com.helospark.tactview.ui.javafx.uicomponents.TimelineState;
import com.helospark.tactview.ui.javafx.uicomponents.display.AudioPlayedListener;

@Component
public class UiTimelineManagerFactory {
    private final ProjectRepository projectRepository;
    private final UiProjectRepository uiProjectRepository;
    private final TimelineState timelineState;
    private final PlaybackFrameAccessor playbackFrameAccessor;
    private final AudioStreamService audioStreamService;
    private UiPlaybackPreferenceRepository uiPlaybackPreferenceRepository;
    private JavaByteArrayConverter javaByteArrayConverter;
    private List<AudioPlayedListener> audioPlayedListeners;
    private ScheduledExecutorService scheduledExecutorService;

    public UiTimelineManagerFactory(ProjectRepository projectRepository, TimelineState timelineState, PlaybackFrameAccessor playbackController,
            AudioStreamService audioStreamService, UiPlaybackPreferenceRepository uiPlaybackPreferenceRepository, JavaByteArrayConverter javaByteArrayConverter,
            List<AudioPlayedListener> audioPlayedListeners, @Qualifier("generalTaskScheduledService") ScheduledExecutorService scheduledExecutorService,
            UiProjectRepository uiProjectRepository, CanvasStateHolder canvasStateHolder) {
        this.projectRepository = projectRepository;
        this.timelineState = timelineState;
        this.playbackFrameAccessor = playbackController;
        this.audioStreamService = audioStreamService;
        this.uiPlaybackPreferenceRepository = uiPlaybackPreferenceRepository;
        this.javaByteArrayConverter = javaByteArrayConverter;
        this.audioPlayedListeners = audioPlayedListeners;
        this.scheduledExecutorService = scheduledExecutorService;
        this.uiProjectRepository = uiProjectRepository;
    }

    public UiTimelineManager create(CanvasStateHolder canvasStateHolder) {
        return new UiTimelineManager(projectRepository, timelineState, playbackFrameAccessor, audioStreamService, uiPlaybackPreferenceRepository, javaByteArrayConverter, audioPlayedListeners,
                scheduledExecutorService, uiProjectRepository, canvasStateHolder);
    }
}
