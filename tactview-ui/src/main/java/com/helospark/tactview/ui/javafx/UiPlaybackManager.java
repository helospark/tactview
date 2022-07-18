package com.helospark.tactview.ui.javafx;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.helospark.lightdi.annotation.Qualifier;
import com.helospark.tactview.core.decoder.framecache.GlobalMemoryManagerAccessor;
import com.helospark.tactview.core.preference.PreferenceValue;
import com.helospark.tactview.core.repository.ProjectRepository;
import com.helospark.tactview.core.timeline.AudioFrameResult;
import com.helospark.tactview.core.timeline.AudioVideoFragment;
import com.helospark.tactview.core.timeline.TimelineLength;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.ui.javafx.DisplayUpdaterService.FullScreenData;
import com.helospark.tactview.ui.javafx.DisplayUpdaterService.TimelineDisplayAsyncUpdateRequest;
import com.helospark.tactview.ui.javafx.audio.AudioStreamService;
import com.helospark.tactview.ui.javafx.audio.JavaByteArrayConverter;
import com.helospark.tactview.ui.javafx.repository.UiProjectRepository;
import com.helospark.tactview.ui.javafx.uicomponents.TimelineState;
import com.helospark.tactview.ui.javafx.uicomponents.display.AudioPlayedListener;
import com.helospark.tactview.ui.javafx.uicomponents.display.AudioPlayedRequest;

import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import sonic.Sonic;

public class UiPlaybackManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(UiPlaybackManager.class);
    private ThreadPoolExecutor playbackExecutorService = createExecutorService();
    private final int CACHE_MODULO = 1;
    private int numberOfFramesToCache = 2;
    private final List<Consumer<PlaybackStatus>> statusChangeConsumers = new ArrayList<>();

    private volatile boolean isPlaying;

    private Sonic sonic = null;

    private final ProjectRepository projectRepository;
    private final UiProjectRepository uiProjectRepository;
    private final TimelineState timelineState;
    private final PlaybackFrameAccessor playbackFrameAccessor;
    private final AudioStreamService audioStreamService;
    private DisplayUpdaterService displayUpdaterService;
    private UiPlaybackPreferenceRepository uiPlaybackPreferenceRepository;
    private JavaByteArrayConverter javaByteArrayConverter;
    private List<AudioPlayedListener> audioPlayedListeners; // TODO
    private GlobalTimelinePositionHolder globalTimelinePositionHolder;

    private ScheduledExecutorService scheduledExecutorService;

    private long lastTimeScreenUpdated = 0;

    public UiPlaybackManager(ProjectRepository projectRepository, TimelineState timelineState, PlaybackFrameAccessor playbackController,
            AudioStreamService audioStreamService, UiPlaybackPreferenceRepository uiPlaybackPreferenceRepository, JavaByteArrayConverter javaByteArrayConverter,
            List<AudioPlayedListener> audioPlayedListeners, @Qualifier("generalTaskScheduledService") ScheduledExecutorService scheduledExecutorService,
            UiProjectRepository uiProjectRepository, GlobalTimelinePositionHolder globalTimelinePositionHolder, DisplayUpdaterService displayUpdaterService) {
        this.projectRepository = projectRepository;
        this.timelineState = timelineState;
        this.playbackFrameAccessor = playbackController;
        this.audioStreamService = audioStreamService;
        this.uiPlaybackPreferenceRepository = uiPlaybackPreferenceRepository;
        this.javaByteArrayConverter = javaByteArrayConverter;
        this.audioPlayedListeners = audioPlayedListeners;
        this.scheduledExecutorService = scheduledExecutorService;
        this.uiProjectRepository = uiProjectRepository;
        this.globalTimelinePositionHolder = globalTimelinePositionHolder;
        this.displayUpdaterService = displayUpdaterService;

        scheduledExecutorService.scheduleAtFixedRate(() -> handleStuckPlayback(), 5000, 5000, TimeUnit.MILLISECONDS);

        globalTimelinePositionHolder.registerUiPlaybackConsumer(time -> {
            if (!isPlaying) { // avoid double update during playback
                TimelineDisplayAsyncUpdateRequest request = TimelineDisplayAsyncUpdateRequest.builder()
                        .withCanDropFrames(false)
                        .withCurrentPosition(time)
                        .withExpectedNextPositions(List.of())
                        .build();
                displayUpdaterService.updateDisplayAsync(request);
            }
        });
    }

    private ThreadPoolExecutor createExecutorService() {
        return new ThreadPoolExecutor(1, 1,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(),
                new ThreadFactoryBuilder().setNameFormat("playback-thread-%d").build());
    }

    public void registerStoppedConsumer(Consumer<PlaybackStatus> consumer) {
        this.statusChangeConsumers.add(consumer);
    }

    private void togglePlayback() {
        if (this.isPlaying) {
            stopPlayback();
        } else {
            startPlayback();
        }
    }

    public void startPlayback() {
        if (!isPlaying) {
            isPlaying = true;
            statusChangeConsumers.stream()
                    .forEach(consumer -> consumer.accept(PlaybackStatus.STARTED));
            lastTimeScreenUpdated = System.currentTimeMillis();
            playbackExecutorService.submit(() -> playback());
        }
    }

    private void playback() {
        try {
            int frame = 0;
            audioStreamService.startPlayback();
            while (isPlaying) {
                TimelinePosition nextFrame = this.expectedNextFrames(1).get(0);
                globalTimelinePositionHolder.jumpAbsolute(nextFrame);
                TimelinePosition currentPosition = globalTimelinePositionHolder.getCurrentPosition();

                boolean isMute = uiPlaybackPreferenceRepository.isMute();
                TimelineLength length = new TimelineLength(projectRepository.getFrameTime());
                AudioVideoFragment audioVideoFragment = playbackFrameAccessor.getSingleAudioFrameAtPosition(currentPosition, isMute, Optional.of(length));

                if (!uiPlaybackPreferenceRepository.getPlaybackSpeedMultiplier().equals(BigDecimal.ONE)) {
                    AudioFrameResult newResult = changePlaybackRateOfAudio(audioVideoFragment);
                    audioVideoFragment = audioVideoFragment.butFreeAndReplaceVideoFrame(newResult);
                }

                byte[] audioFrame = convertToPlayableFormat(audioVideoFragment);

                notifyAudioListeners(audioVideoFragment);

                audioStreamService.streamAudio(audioFrame);

                // finished writing currentPosition to buffer, play this frame

                TimelineDisplayAsyncUpdateRequest request = TimelineDisplayAsyncUpdateRequest.builder()
                        .withCanDropFrames(true)
                        .withCurrentPosition(currentPosition)
                        .withExpectedNextPositions(expectedNextFramesWithDroppedFrameModulo(10, CACHE_MODULO, frame + 1))
                        .build();

                audioVideoFragment.free();
                if (frame % CACHE_MODULO == 0) {
                    displayUpdaterService.updateDisplayAsync(request);
                }
                lastTimeScreenUpdated = System.currentTimeMillis();

                ++frame;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            audioStreamService.stopPlayback();
            isPlaying = false;
        }
    }

    private AudioFrameResult changePlaybackRateOfAudio(AudioVideoFragment audioVideoFragment) {
        float tempo = uiPlaybackPreferenceRepository.getPlaybackSpeedMultiplier().floatValue();
        AudioFrameResult audioResult = audioVideoFragment.getAudioResult();
        int sampleRate = audioResult.getSamplePerSecond();
        List<ByteBuffer> channels = audioResult.getChannels();
        int numberChannels = channels.size();
        if (sonic == null || sonic.getSampleRate() != audioResult.getSamplePerSecond() || sonic.getNumChannels() != numberChannels) {
            sonic = new Sonic(sampleRate, numberChannels);
            sonic.setPitch(1.0f);
            sonic.setRate(1.0f);
            sonic.setVolume(1.0f);
            sonic.setChordPitch(false);
            sonic.setQuality(0);
        }

        int samplesInInput = audioResult.getNumberSamples() * numberChannels;

        float[] samples = new float[samplesInInput];

        for (int i = 0; i < audioResult.getNumberSamples(); ++i) {
            for (int j = 0; j < numberChannels; ++j) {
                samples[i * numberChannels + j] = audioResult.getNormalizedSampleAt(j, i);
            }
        }

        sonic.setSpeed(tempo);

        sonic.writeFloatToStream(samples, audioResult.getNumberSamples());

        int actualOutputSamples = sonic.samplesAvailable();
        float[] outputSamples = new float[actualOutputSamples * numberChannels];
        actualOutputSamples = sonic.readFloatFromStream(outputSamples, actualOutputSamples);

        List<ByteBuffer> newChannels = new ArrayList<>();
        for (int i = 0; i < numberChannels; ++i) {
            newChannels.add(GlobalMemoryManagerAccessor.memoryManager.requestBuffer(actualOutputSamples * audioResult.getBytesPerSample()));
        }
        AudioFrameResult newResult = new AudioFrameResult(newChannels, audioResult.getSamplePerSecond(), audioResult.getBytesPerSample());
        for (int i = 0; i < actualOutputSamples; ++i) {
            for (int j = 0; j < numberChannels; ++j) {
                newResult.setNormalizedSampleAt(j, i, outputSamples[i * numberChannels + j]);
            }
        }
        return newResult;
    }

    private void notifyAudioListeners(AudioVideoFragment audioVideoFragment) {
        TimelinePosition currentPosition = globalTimelinePositionHolder.getCurrentPosition();
        audioPlayedListeners.stream()
                .forEach(listener -> listener.onAudioPlayed(new AudioPlayedRequest(currentPosition, audioVideoFragment.getAudioResult())));
    }

    private byte[] convertToPlayableFormat(AudioVideoFragment videoFragment) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = javaByteArrayConverter.convert(videoFragment.getAudioResult(), PlaybackFrameAccessor.CHANNELS); // move data to repository

            baos.write(buffer);

            return baos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    public void refreshDisplay() {
        refreshDisplay(false);
    }

    public void refreshDisplay(boolean invalidateCache) {
        TimelinePosition currentPosition = globalTimelinePositionHolder.getCurrentPosition();
        if (invalidateCache) {
            displayUpdaterService.updateDisplayWithCacheInvalidation(currentPosition);
        } else {
            displayUpdaterService.updateDisplay(currentPosition);
        }

        // TODO: this is also a consumer, but due to playback it would be doublecalled during playback
        TimelineLength length = new TimelineLength(projectRepository.getFrameTime());
        AudioVideoFragment audioVideoFragment = playbackFrameAccessor.getSingleAudioFrameAtPosition(currentPosition, false, Optional.of(length));
        notifyAudioListeners(audioVideoFragment);
        audioVideoFragment.free();
    }

    public void stopPlayback() {
        if (isPlaying) {
            isPlaying = false;
            statusChangeConsumers.stream()
                    .forEach(consumer -> consumer.accept(PlaybackStatus.STOPPED));

            scheduledExecutorService.schedule(() -> {
                if (!isPlaying && playbackExecutorService.getActiveCount() == 1) {
                    restartThreadpoolToAvoidStuckPlayback();
                }
            }, 200, TimeUnit.MILLISECONDS);
        }
    }

    private void handleStuckPlayback() {
        if (isPlaying && System.currentTimeMillis() - lastTimeScreenUpdated > 5000) {
            restartThreadpoolToAvoidStuckPlayback();
            playbackExecutorService.submit(() -> playback());
        }
    }

    // This is a workaround for something that appears to be either an OpenJDK or ALSA bug, but very difficult to reproduce.
    // See more explanation here: https://gist.github.com/helospark/9406f093cc39fe8c4ccf1ea61951e4ee
    private void restartThreadpoolToAvoidStuckPlayback() {
        LOGGER.error("Timeline playback stuck, attempting to interrupt and restart");
        var originalExecutorService = playbackExecutorService;
        playbackExecutorService = createExecutorService();
        originalExecutorService.shutdownNow();
    }

    public void refresh() {
        refreshDisplay();
    }

    public List<TimelinePosition> expectedNextFrames() {
        return expectedNextFrames(numberOfFramesToCache);

    }

    public List<TimelinePosition> expectedNextFrames(int number) {
        return expectedNextFramesWithDroppedFrameModulo(number, 1, 0);
    }

    private List<TimelinePosition> expectedNextFramesWithDroppedFrameModulo(int number, int modulo, int startingFrame) {
        BigDecimal increment = globalTimelinePositionHolder.getIncrement();
        if (isPlaying) {
            List<TimelinePosition> result = new ArrayList<>();
            TimelinePosition position = globalTimelinePositionHolder.getCurrentPosition();

            if (timelineState.loopingEnabled() && position.isLessThan(timelineState.getLoopALineProperties().get())) {
                position = timelineState.getLoopALineProperties().get();
            }

            while (result.size() < number) {
                position = position.add(increment);

                if (timelineState.loopingEnabled() && position.isGreaterThan(timelineState.getLoopBLineProperties().get())) {
                    position = timelineState.getLoopALineProperties().get();
                }

                if (startingFrame % modulo == 0) {
                    result.add(position);
                }
                ++startingFrame;
            }
            return result;
        } else {
            return Collections.emptyList();
        }
    }

    public boolean isPlaybackInProgress() {
        return isPlaying;
    }

    public void startFullscreenPlayback() {
        Rectangle2D screenBounds = Screen.getPrimary().getBounds();
        double screenAspectRatio = screenBounds.getWidth() / screenBounds.getHeight();
        double videoAspectRatio = uiProjectRepository.getAspectRatio();

        double videoWidth, videoHeight;

        if (videoAspectRatio > screenAspectRatio) {
            videoHeight = screenBounds.getHeight();
            videoWidth = videoHeight * videoAspectRatio;
        } else {
            videoWidth = screenBounds.getWidth();
            videoHeight = videoWidth / videoAspectRatio;
        }

        BorderPane boderPane = new BorderPane();
        Canvas canvas = new Canvas(videoWidth, videoHeight);
        boderPane.setCenter(canvas);

        Scene scene = new Scene(boderPane);

        Stage stage = new Stage();
        stage.setFullScreen(true);
        stage.setScene(scene);
        stage.show();

        scene.setOnKeyPressed(e -> {
            if (e.getCode().equals(KeyCode.ESCAPE)) {
                stage.close();
                displayUpdaterService.stopFullscreenPreview();
            }
            if (e.getCode().equals(KeyCode.SPACE)) {
                togglePlayback();
            }
        });
        scene.setOnMouseClicked(e -> {
            if (e.isPrimaryButtonDown() && e.getClickCount() == 1) {
                togglePlayback();
            }
        });

        FullScreenData fullscreenData = new FullScreenData(canvas, (int) videoWidth, (int) videoHeight, videoWidth / projectRepository.getWidth());

        displayUpdaterService.startFullscreenPreview(fullscreenData);
        displayUpdaterService.updateCurrentPositionWithInvalidatedCache();
    }

    public void stopFullscreen() {
        displayUpdaterService.stopFullscreenPreview();
    }

    @PreferenceValue(name = "Number of frames to preload during playback", defaultValue = "2", group = "Performance")
    public void setImageClipLength(Integer numberOfFrames) {
        numberOfFramesToCache = numberOfFrames;
    }

    public static enum PlaybackStatus {
        STARTED,
        STOPPED
    }
}
