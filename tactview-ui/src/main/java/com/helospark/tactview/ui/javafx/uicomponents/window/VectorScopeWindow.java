package com.helospark.tactview.ui.javafx.uicomponents.window;

import static javafx.scene.paint.Color.BLACK;

import java.util.List;

import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.annotation.Order;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Color;
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
@Order(4002)
public class VectorScopeWindow extends SingletonOpenableWindow implements DisplayUpdatedListener, MenuContribution {
    private static final int BORDER = 10;
    private static final int INNER_CIRCLE_BORDER = 128;
    private static final int DEFAULT_WIDTH = 255 * 2;
    private static final int DEFAULT_HEIGHT = 255 * 2;
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

        graphics.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        graphics.setFill(BLACK);
        graphics.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        graphics.setStroke(javafx.scene.paint.Color.gray(0.4));
        graphics.strokeOval(0, 0, DEFAULT_WIDTH, DEFAULT_HEIGHT);
        graphics.strokeOval(INNER_CIRCLE_BORDER, INNER_CIRCLE_BORDER, DEFAULT_WIDTH - 2 * INNER_CIRCLE_BORDER, DEFAULT_HEIGHT - 2 * INNER_CIRCLE_BORDER);

        graphics.strokeLine(BORDER, BORDER, DEFAULT_WIDTH - BORDER, DEFAULT_HEIGHT - BORDER);
        graphics.strokeLine(DEFAULT_WIDTH - BORDER, BORDER, BORDER, DEFAULT_HEIGHT - BORDER);

        double centerX = canvas.getWidth() / 2.0;
        double centerY = canvas.getHeight() / 2.0;

        graphics.setFill(new javafx.scene.paint.Color(0.3, 0.8, 0.3, 0.1));

        for (int i = 0; i < javafxImage.getHeight(); ++i) {
            for (int j = 0; j < javafxImage.getWidth(); ++j) {
                javafx.scene.paint.Color javafxColor = javafxImage.getPixelReader().getColor(j, i);
                Color color = new Color(javafxColor.getRed(), javafxColor.getGreen(), javafxColor.getBlue()).rgbToHsl();

                double colorRadian = (color.red * 2.0 - 1.0) * Math.PI;
                double x = centerX + Math.cos(colorRadian) * color.green * 255.0;
                double y = centerY + Math.sin(colorRadian) * color.green * 255.0;

                graphics.fillOval(x, y, 3.0, 3.0);
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
        return "Vectorscope";
    }

    @Override
    public List<String> getPath() {
        return List.of("Window", "Vectorscope");
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
