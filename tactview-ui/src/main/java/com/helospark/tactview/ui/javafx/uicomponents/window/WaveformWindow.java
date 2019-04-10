package com.helospark.tactview.ui.javafx.uicomponents.window;

import static javafx.scene.paint.Color.BLACK;

import java.util.List;

import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.annotation.Order;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Color;
import com.helospark.tactview.core.util.MathUtil;
import com.helospark.tactview.ui.javafx.menu.MenuContribution;
import com.helospark.tactview.ui.javafx.uicomponents.display.DisplayUpdatedListener;
import com.helospark.tactview.ui.javafx.uicomponents.display.DisplayUpdatedRequest;

import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;

@Component
@Order(4000)
public class WaveformWindow extends SingletonOpenableWindow implements DisplayUpdatedListener, MenuContribution {
    private static final int DEFAULT_WIDTH = 400;
    private static final int DEFAULT_HEIGHT = 255;
    private Canvas canvas;

    private boolean isUsingCoolLookingAddition = false;

    private Image previouslyDisplayedImage;

    @Override
    public void displayUpdated(DisplayUpdatedRequest request) {
        if (!isWindowOpen) {
            this.previouslyDisplayedImage = request.image;
            return;
        }
        Image javafxImage = request.image;
        updateCanvasWithImage(javafxImage);
    }

    private void updateCanvasWithImage(Image javafxImage) {
        GraphicsContext graphics = canvas.getGraphicsContext2D();

        graphics.setFill(BLACK);
        graphics.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        double xScaler = canvas.getWidth() / javafxImage.getWidth();

        if (isUsingCoolLookingAddition) {
            // this looks better, but very slow :/
            graphics.setFill(new javafx.scene.paint.Color(0.1, 0.4, 0.1, 0.1));
            graphics.setGlobalBlendMode(BlendMode.ADD);
        } else {
            graphics.setFill(new javafx.scene.paint.Color(0.3, 0.8, 0.3, 0.1));
            graphics.setGlobalBlendMode(BlendMode.SRC_OVER);
        }
        for (int i = 0; i < javafxImage.getHeight(); ++i) {
            for (int j = 0; j < javafxImage.getWidth(); ++j) {
                javafx.scene.paint.Color javafxColor = javafxImage.getPixelReader().getColor(j, i);
                Color color = new Color(javafxColor.getRed(), javafxColor.getGreen(), javafxColor.getBlue());

                double newY = (canvas.getHeight() - MathUtil.clamp((color.red + color.green + color.blue) / 3.0 * 255.0, 0, canvas.getHeight()));
                double newX = MathUtil.clamp(j * xScaler, 0, canvas.getWidth());

                graphics.fillOval(newX, newY, 3.0, 3.0);
            }
        }
    }

    @Override
    protected Scene createScene() {
        BorderPane borderPane = new BorderPane();

        canvas = new Canvas(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        canvas.widthProperty().bind(borderPane.widthProperty());

        borderPane.setCenter(canvas);

        return new Scene(borderPane, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    @Override
    public void open() {
        super.open();

        if (previouslyDisplayedImage != null) {
            updateCanvasWithImage(previouslyDisplayedImage);
        }
    }

    @Override
    public String getWindowId() {
        return "waveform";
    }

    @Override
    public List<String> getPath() {
        return List.of("Window", "Waveform");
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
