package com.helospark.tactview.ui.javafx.uicomponents.window;

import java.util.List;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.annotation.Order;
import com.helospark.tactview.core.timeline.AudioFrameResult;
import com.helospark.tactview.core.util.MathUtil;
import com.helospark.tactview.ui.javafx.menu.SelectableMenuContribution;
import com.helospark.tactview.ui.javafx.uicomponents.display.AudioPlayedListener;
import com.helospark.tactview.ui.javafx.uicomponents.display.AudioPlayedRequest;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.CheckBox;
import javafx.scene.effect.BlendMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

@Component
@Order(4000)
public class AudioSpectrumWindow extends SingletonOpenableWindow implements AudioPlayedListener, SelectableMenuContribution {
    private static final int MAX_FREQUENCY = 15000;
    private static final int DEFAULT_WIDTH = 600;
    private static final int DEFAULT_HEIGHT = 255;
    private static final int LABEL_HEIGHT = 20;
    private static final int AVG_SIZE = 3;

    private FastFourierTransformer fastFourierTransformer = new FastFourierTransformer(DftNormalization.UNITARY);

    private Canvas canvas;
    private CheckBox logCheckbox;

    private AudioFrameResult previousAudioFrame;

    @Override
    public void onAudioPlayed(AudioPlayedRequest request) {
        this.previousAudioFrame = request.getAudioFrameResult().onHeapCopy();
        if (!isWindowOpen) {
            return;
        }

        AudioFrameResult audioFragment = previousAudioFrame;

        updateCanvasWithAudioFragment(audioFragment);
    }

    private void updateCanvasWithAudioFragment(AudioFrameResult audioFragment) {

        double[] transformData = convertData(audioFragment);

        Complex[] fftResult = fastFourierTransformer.transform(transformData, TransformType.FORWARD);

        double frequencyPerBucket = (double) audioFragment.getSamplePerSecond() / transformData.length;
        double[] fftMagnitudes = convertMagnitudes(fftResult, frequencyPerBucket);

        double xScaler = canvas.getWidth() / fftMagnitudes.length;
        double yScaler = 100.0;

        Platform.runLater(() -> {
            GraphicsContext graphics = canvas.getGraphicsContext2D();

            graphics.setFill(Color.BLACK);
            graphics.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
            graphics.setFill(new javafx.scene.paint.Color(0.3, 0.8, 0.3, 0.1));
            graphics.setGlobalBlendMode(BlendMode.SRC_OVER);

            for (int i = 0; i < fftMagnitudes.length; i += AVG_SIZE) {
                Color color = Color.WHITE;
                graphics.setFill(color);

                double avg = 0;
                for (int j = 0; j < AVG_SIZE; ++j) {
                    avg += (i + j) < fftMagnitudes.length ? fftMagnitudes[i + j] : 0;
                }
                avg /= AVG_SIZE;

                double height = 0.0;

                if (logCheckbox.isSelected()) {
                    height = MathUtil.clamp(20 * Math.log10(avg) + 100, 0, canvas.getHeight() - LABEL_HEIGHT) * 1;
                } else {
                    height = MathUtil.clamp(avg * yScaler, 0, canvas.getHeight() - LABEL_HEIGHT) * 1;
                }
                double newX = MathUtil.clamp(i * xScaler, 0, canvas.getWidth());

                graphics.fillRect(newX, canvas.getHeight() - LABEL_HEIGHT - height, xScaler * AVG_SIZE, height);
            }

            int stepSize = fftMagnitudes.length / 10;

            for (int i = 0; i < fftMagnitudes.length; i += stepSize) {
                String text = Integer.toString((int) (i * frequencyPerBucket));
                double textWidth = computeTextWidth(graphics.getFont(), text);

                graphics.setFill(Color.GRAY);
                graphics.fillText(text, i * xScaler - textWidth / 2, canvas.getHeight());
                graphics.fillRect(i * xScaler, 0, 0.5, canvas.getHeight() - LABEL_HEIGHT);
            }
        });
    }

    private double computeTextWidth(Font font, String text) {
        Text helper = new Text();
        helper.setFont(font);
        helper.setText(text);
        helper.setWrappingWidth(0);
        helper.setLineSpacing(0);
        double w = helper.prefWidth(-1);
        helper.setWrappingWidth((int) Math.ceil(w));
        double textWidth = Math.ceil(helper.getLayoutBounds().getWidth());
        return textWidth;
    }

    private double[] convertMagnitudes(Complex[] fftResult, double frequencyPerBucket) {
        int size = fftResult.length / 2;
        double frequencyRange = size * frequencyPerBucket;
        if (frequencyRange > MAX_FREQUENCY) {
            size = (int) (MAX_FREQUENCY / frequencyPerBucket);
        }
        double[] result = new double[size];
        for (int i = 0; i < size; ++i) {
            double imaginary = fftResult[i].getImaginary();
            double real = fftResult[i].getReal();
            result[i] = Math.sqrt(imaginary * imaginary + real * real);
        }
        return result;
    }

    private double[] convertData(AudioFrameResult audioFragment) {
        int samples = audioFragment.getNumberSamples();

        int nextPower = nextPowerOfTwo(samples);

        double[] result = new double[nextPower];

        for (int i = 0; i < audioFragment.getNumberSamples(); ++i) {
            result[i] = audioFragment.getNormalizedSampleAt(0, i);
        }
        for (int i = audioFragment.getNumberSamples(); i < result.length; ++i) {
            result[i] = 0.0;
        }

        return result;
    }

    private int nextPowerOfTwo(int samples) {
        int i = 1;

        while (i < samples) {
            i = i * 2;
        }
        return i;
    }

    @Override
    protected Scene createScene() {
        BorderPane borderPane = new BorderPane();

        canvas = new Canvas(DEFAULT_WIDTH, DEFAULT_HEIGHT + LABEL_HEIGHT - 19); // TODO: 19 = topBox.getHeight()
        canvas.widthProperty().bind(borderPane.widthProperty());

        VBox topBox = new VBox();
        logCheckbox = new CheckBox("Logarithmic");
        logCheckbox.selectedProperty().addListener(e -> {
            updateCanvasWithAudioFragment(previousAudioFrame);
        });

        topBox.getChildren().add(logCheckbox);

        borderPane.setCenter(canvas);
        borderPane.setTop(topBox);

        return new Scene(borderPane, DEFAULT_WIDTH, DEFAULT_HEIGHT + LABEL_HEIGHT);
    }

    @Override
    public void open() {
        super.open();

        if (previousAudioFrame != null) {
            updateCanvasWithAudioFragment(previousAudioFrame);
        }
    }

    @Override
    public void close() {
        super.close();
        this.isWindowOpen = false;
    }

    @Override
    public String getWindowId() {
        return "audiospectrum";
    }

    @Override
    public List<String> getPath() {
        return List.of("Window", "Audio spectrum");
    }

    @Override
    public void onAction(ActionEvent event) {
        this.open();
    }

    @Override
    protected boolean isResizable() {
        return false;
    }

}
