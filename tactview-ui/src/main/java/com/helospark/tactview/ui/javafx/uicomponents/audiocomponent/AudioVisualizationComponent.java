package com.helospark.tactview.ui.javafx.uicomponents.audiocomponent;

import com.helospark.tactview.core.init.PostInitializationArgsCallback;
import com.helospark.tactview.core.timeline.AudioFrameResult;
import com.helospark.tactview.core.util.AudioRmsCalculator;
import com.helospark.tactview.core.util.MathUtil;
import com.helospark.tactview.ui.javafx.PlaybackFrameAccessor;
import com.helospark.tactview.ui.javafx.uicomponents.display.AudioPlayedListener;
import com.helospark.tactview.ui.javafx.uicomponents.display.AudioPlayedRequest;

import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class AudioVisualizationComponent implements AudioPlayedListener, PostInitializationArgsCallback {
    private static final int EXPECTED_NUMBER_OF_CHANNELS = 2;
    private static final int BAR_RADIUS = 6;
    Color startColor = Color.GREEN;
    Color endColor = Color.RED;
    static final int CHANNEL_HEIGHT = 10;
    static final int CHANNEL_HEIGHT_GAP = 1;
    static final int BAR_WIDTH = 5;
    static final int BAR_SPACE_WIDTH = 2;
    private final boolean enabled = true;
    private volatile boolean isThreadAvailable = true;
    private final Canvas canvas;

    private int numberOfBars = 45;

    private final AudioRmsCalculator audioRmsCalculator;
    private final Canvas previewCanvas;

    public AudioVisualizationComponent(PlaybackFrameAccessor playbackController, AudioRmsCalculator audioRmsCalculator, Canvas previewCanvas) {
        canvas = new Canvas(numberOfBars * (BAR_WIDTH + BAR_SPACE_WIDTH) + 2, (CHANNEL_HEIGHT + CHANNEL_HEIGHT_GAP) * EXPECTED_NUMBER_OF_CHANNELS + 2);
        this.audioRmsCalculator = audioRmsCalculator;
        this.previewCanvas = previewCanvas;
    }

    @Override
    public void call(String[] args) {
        previewCanvas.widthProperty().addListener((e, oldV, newV) -> {
            if (newV.doubleValue() <= 0.0) {
                return;
            }
            int newNumberOfBars = (int) (newV.doubleValue() / (BAR_WIDTH + BAR_SPACE_WIDTH));
            canvas.widthProperty().set(newV.doubleValue() - 40);

            numberOfBars = newNumberOfBars;

            clearCanvas();
            Platform.runLater(() -> {
                updateUI(new double[] { 0.0, 0.0 });
            });
        });
    }

    public Canvas getCanvas() {
        return canvas;
    }

    private void updateUI(double[] rms) {
        isThreadAvailable = false;

        clearCanvas();

        Platform.runLater(() -> {
            for (int i = 0; i < 2; ++i) {
                double value = rms[i];
                updateUiForChannel(i, value);
            }
            isThreadAvailable = true;
        });
    }

    public void clearCanvas() {
        Platform.runLater(() -> {
            GraphicsContext graphics = canvas.getGraphicsContext2D();
            graphics.setFill(Color.rgb(60, 60, 60));
            graphics.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        });
    }

    private void updateUiForChannel(int channel, double value) {
        int numberOfBarsLocal = numberOfBars;
        GraphicsContext graphics = canvas.getGraphicsContext2D();
        double normalizedValue = MathUtil.clamp(value, 0.0, 1.0);
        double increment = 1.0 / numberOfBarsLocal;
        for (int i = 0; i < numberOfBarsLocal; ++i) {
            if (i * increment < normalizedValue) {
                graphics.setFill(startColor.interpolate(endColor, i * increment));
            } else {
                graphics.setFill(Color.gray(0.5, 0.1));
            }
            graphics.fillRoundRect(i * (BAR_WIDTH + BAR_SPACE_WIDTH) + 1, channel * (CHANNEL_HEIGHT + CHANNEL_HEIGHT_GAP) + CHANNEL_HEIGHT_GAP, BAR_WIDTH, CHANNEL_HEIGHT, BAR_RADIUS, BAR_RADIUS);
        }
    }

    @Override
    public void onAudioPlayed(AudioPlayedRequest request) {
        if (enabled && isThreadAvailable) {
            AudioFrameResult audioFrame = request.getAudioFrameResult();
            double rms[] = new double[2];
            for (int i = 0; i < 2; ++i) {
                double value = i < audioFrame.getChannels().size() ? audioRmsCalculator.calculateRms(audioFrame, i) : 0.0;
                rms[i] = value;
            }
            Platform.runLater(() -> updateUI(rms));
        }
    }

}
