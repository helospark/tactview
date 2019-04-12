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
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;

@Component
@Order(4003)
public class HistogramWindow extends SingletonOpenableWindow implements DisplayUpdatedListener, MenuContribution {
    private static final int DEFAULT_WIDTH = 500;
    private static final int DEFAULT_HEIGHT = 300;
    private Canvas canvas;

    private Image previouslyDisplayedImage;

    @Override
    public void displayUpdated(DisplayUpdatedRequest request) {
        if (!isWindowOpen) {
            previouslyDisplayedImage = request.image;
            return;
        }
        Image javafxImage = request.image;
        updateCanvasWithImage(javafxImage);
    }

    private void updateCanvasWithImage(Image javafxImage) {
        GraphicsContext graphics = canvas.getGraphicsContext2D();

        graphics.setFill(BLACK);
        graphics.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        int[] values = new int[256];

        for (int i = 0; i < javafxImage.getHeight(); ++i) {
            for (int j = 0; j < javafxImage.getWidth(); ++j) {
                javafx.scene.paint.Color javafxColor = javafxImage.getPixelReader().getColor(j, i);
                Color color = new Color(javafxColor.getRed(), javafxColor.getGreen(), javafxColor.getBlue()).multiplyComponents(255.0);

                int index = (int) MathUtil.clamp((color.red + color.green + color.blue) / 3.0, 0, 255);

                values[index] += 1;
            }
        }

        int max = 0;
        for (int i = 0; i < values.length; ++i) {
            if (values[i] > max) {
                max = values[i];
            }
        }

        double columnWidth = canvas.getWidth() / values.length;
        double columnScaler = canvas.getHeight() / max;

        graphics.setFill(javafx.scene.paint.Color.GREEN);
        for (int i = 0; i < values.length; ++i) {
            double height = values[i] * columnScaler;
            graphics.fillRect(i * columnWidth, canvas.getHeight() - height, columnWidth, height);
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
        return "Histogram";
    }

    @Override
    public List<String> getPath() {
        return List.of("Window", "Histogram");
    }

    @Override
    public void onAction(ActionEvent event) {
        this.open();
    }

    @Override
    protected boolean isResizable() {
        return true;
    }

}
