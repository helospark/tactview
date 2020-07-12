package com.helospark.tactview.ui.javafx.uicomponents.audiocomponent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.AudioFrameResult;
import com.helospark.tactview.core.timeline.AudioVideoFragment;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.util.MathUtil;
import com.helospark.tactview.ui.javafx.PlaybackController;
import com.helospark.tactview.ui.javafx.repository.SoundRmsRepository;
import com.helospark.tactview.ui.javafx.uicomponents.util.AudioRmsCalculator;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

@Component
public class AudioVisualizationComponent {
    private static final int EXPECTED_NUMBER_OF_CHANNELS = 2;
    private static final int BAR_RADIUS = 6;
    Color startColor = Color.GREEN;
    Color endColor = Color.RED;
    static final int NUMBER_OF_BARS = 45;
    static final int CHANNEL_HEIGHT = 20;
    static final int CHANNEL_HEIGHT_GAP = 3;
    static final int BAR_WIDTH = 5;
    static final int BAR_SPACE_WIDTH = 2;
    private boolean enabled = true;
    private volatile boolean isThreadAvailable = true;
    private ExecutorService executorService = Executors.newFixedThreadPool(1);
    private Canvas canvas;

    private PlaybackController playbackController;
    private SoundRmsRepository soundRmsRepository;
    private AudioRmsCalculator audioRmsCalculator;

    public AudioVisualizationComponent(PlaybackController playbackController, SoundRmsRepository soundRmsRepository, AudioRmsCalculator audioRmsCalculator) {
        canvas = new Canvas(NUMBER_OF_BARS * (BAR_WIDTH + BAR_SPACE_WIDTH) + 2, (CHANNEL_HEIGHT + CHANNEL_HEIGHT_GAP) * EXPECTED_NUMBER_OF_CHANNELS + 2);
        this.playbackController = playbackController;
        this.soundRmsRepository = soundRmsRepository;
        this.audioRmsCalculator = audioRmsCalculator;
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public void updateAudioComponent(TimelinePosition position) {
        if (enabled && isThreadAvailable) {
            executorService.execute(() -> updateUI(position));
        }
    }

    private void updateUI(TimelinePosition position) {
        try {
            isThreadAvailable = false;
            // TODO: Do not query the sound again! Use the already queried sound
            AudioVideoFragment frame = playbackController.getSingleAudioFrameAtPosition(position);
            AudioFrameResult audioFrame = frame.getAudioResult();
            clearCanvas();

            for (int i = 0; i < 2; ++i) {
                double value = i < audioFrame.getChannels().size() ? audioRmsCalculator.calculateRms(audioFrame, i) : 0.0;
                updateUiForChannel(i, value);
            }

        } finally {
            isThreadAvailable = true;
        }
    }

    public void clearCanvas() {
        GraphicsContext graphics = canvas.getGraphicsContext2D();
        graphics.setFill(Color.rgb(60, 60, 60));
        graphics.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    private void updateUiForChannel(int channel, double value) {
        double maxRms = soundRmsRepository.getMaxRms();
        GraphicsContext graphics = canvas.getGraphicsContext2D();
        double normalizedValue = MathUtil.clamp(value / maxRms, 0.0, 1.0);
        double increment = 1.0 / NUMBER_OF_BARS;
        for (int i = 0; i < NUMBER_OF_BARS; ++i) {
            if (i * increment < normalizedValue) {
                graphics.setFill(startColor.interpolate(endColor, i * increment));
            } else {
                graphics.setFill(Color.gray(0.5, 0.1));
            }
            graphics.fillRoundRect(i * (BAR_WIDTH + BAR_SPACE_WIDTH) + 1, channel * (CHANNEL_HEIGHT + CHANNEL_HEIGHT_GAP) + CHANNEL_HEIGHT_GAP, BAR_WIDTH, CHANNEL_HEIGHT, BAR_RADIUS, BAR_RADIUS);
        }
    }

}
