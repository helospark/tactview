package com.helospark.tactview.ui.javafx.uicomponents.audiocomponent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.AudioFrameResult;
import com.helospark.tactview.core.timeline.AudioVideoFragment;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.util.MathUtil;
import com.helospark.tactview.ui.javafx.PlaybackController;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

@Component
public class AudioVisualizationComponent {
    private static final int EXPECTED_NUMBER_OF_CHANNELS = 2;
    private static final int BAR_RADIUS = 6;
    private static final double EXPECTED_MAX_VALUE = 14000.0;
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

    public AudioVisualizationComponent(PlaybackController playbackController) {
        canvas = new Canvas(NUMBER_OF_BARS * (BAR_WIDTH + BAR_SPACE_WIDTH), CHANNEL_HEIGHT * EXPECTED_NUMBER_OF_CHANNELS + 10);
        this.playbackController = playbackController;
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
            if (audioFrame.getBytesPerSample() == 0 || audioFrame.getNumberSamples() == 0) {
                return;
            }
            clearCanvas();

            for (int i = 0; i < audioFrame.getChannels().size(); ++i) {
                double value = calculateRmsForChannel(i, audioFrame);
                updateUiForChannel(i, value);
            }

        } finally {
            isThreadAvailable = true;
        }
    }

    public void clearCanvas() {
        GraphicsContext graphics = canvas.getGraphicsContext2D();
        graphics.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    private void updateUiForChannel(int channel, double value) {
        GraphicsContext graphics = canvas.getGraphicsContext2D();
        double normalizedValue = MathUtil.clamp(value / EXPECTED_MAX_VALUE, 0.0, 1.0);
        double increment = 1.0 / NUMBER_OF_BARS;
        for (int i = 0; i < NUMBER_OF_BARS; ++i) {
            if (i * increment < normalizedValue) {
                graphics.setFill(startColor.interpolate(endColor, i * increment));
                graphics.fillRoundRect(i * (BAR_WIDTH + BAR_SPACE_WIDTH), channel * (CHANNEL_HEIGHT + CHANNEL_HEIGHT_GAP), BAR_WIDTH, CHANNEL_HEIGHT, BAR_RADIUS, BAR_RADIUS);
            } else {
                break;
            }
        }
    }

    private double calculateRmsForChannel(int channel, AudioFrameResult audioFrame) {
        long sum = 0;
        for (int i = 0; i < audioFrame.getNumberSamples(); ++i) {
            int currentSample = audioFrame.getSampleAt(channel, i);
            sum += currentSample * currentSample;
        }

        return Math.sqrt((double) sum / audioFrame.getNumberSamples());

    }

}
