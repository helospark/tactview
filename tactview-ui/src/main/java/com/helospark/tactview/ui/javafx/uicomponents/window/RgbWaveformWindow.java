package com.helospark.tactview.ui.javafx.uicomponents.window;

import static javafx.scene.paint.Color.BLACK;

import java.util.List;
import java.util.function.Function;

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
@Order(4001)
public class RgbWaveformWindow extends SingletonOpenableWindow implements DisplayUpdatedListener, MenuContribution {
    private static final int DEFAULT_WIDTH = 800;
    private static final int DEFAULT_HEIGHT = 255;
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

        double xScaler = canvas.getWidth() / (javafxImage.getWidth() * 3);

        graphics.setFill(new javafx.scene.paint.Color(0.8, 0.3, 0.3, 0.1));
        drawColor(graphics, javafxImage, xScaler, 0 * canvas.getWidth() / 3, color -> color.red);

        graphics.setFill(new javafx.scene.paint.Color(0.3, 0.8, 0.3, 0.1));
        drawColor(graphics, javafxImage, xScaler, 1 * canvas.getWidth() / 3, color -> color.green);

        graphics.setFill(new javafx.scene.paint.Color(0.3, 0.3, 0.8, 0.1));
        drawColor(graphics, javafxImage, xScaler, 2 * canvas.getWidth() / 3, color -> color.blue);
    }

    private void drawColor(GraphicsContext graphics, Image javafxImage, double xScaler, double startX, Function<Color, Double> colorToComponent) {
        for (int i = 0; i < javafxImage.getHeight(); ++i) {
            for (int j = 0; j < javafxImage.getWidth(); ++j) {
                javafx.scene.paint.Color javafxColor = javafxImage.getPixelReader().getColor(j, i);
                double color = colorToComponent.apply(new Color(javafxColor.getRed(), javafxColor.getGreen(), javafxColor.getBlue()));

                double newY = (canvas.getHeight() - MathUtil.clamp(color * 255.0, 0, canvas.getHeight()));
                double newX = MathUtil.clamp(startX + j * xScaler, 0, canvas.getWidth());

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
        return "RGB waveform";
    }

    @Override
    public List<String> getPath() {
        return List.of("Window", "RGB waveform");
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
