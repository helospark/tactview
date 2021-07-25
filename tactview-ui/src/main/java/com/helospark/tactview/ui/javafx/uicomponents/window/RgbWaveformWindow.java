package com.helospark.tactview.ui.javafx.uicomponents.window;

import static javafx.scene.paint.Color.BLACK;

import java.util.function.Function;

import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.annotation.Order;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Color;
import com.helospark.tactview.core.util.MathUtil;
import com.helospark.tactview.ui.javafx.tabs.dockabletab.DockableTabRepository;
import com.helospark.tactview.ui.javafx.tiwulfx.com.panemu.tiwulfx.control.DetachableTab;
import com.helospark.tactview.ui.javafx.uicomponents.display.DisplayUpdatedListener;
import com.helospark.tactview.ui.javafx.uicomponents.display.DisplayUpdatedRequest;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;

@Component
@Order(4001)
public class RgbWaveformWindow extends DetachableTab implements DisplayUpdatedListener {
    public static final String ID = "rgb-wave-window";
    private static final int DEFAULT_WIDTH = 800;
    private static final int DEFAULT_HEIGHT = 255;
    private DockableTabRepository dockableTabRepository;

    private Canvas canvas;

    private Image previouslyDisplayedImage = new WritableImage(10, 10);

    public RgbWaveformWindow(DockableTabRepository dockableTabRepository) {
        super(ID);
        this.dockableTabRepository = dockableTabRepository;

        this.openTab();
    }

    @Override
    public void displayUpdated(DisplayUpdatedRequest request) {
        previouslyDisplayedImage = request.image;
        if (!dockableTabRepository.isTabVisibleWithId(ID)) {
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
        double yScaler = canvas.getHeight() / 255.0;
        for (int i = 0; i < javafxImage.getHeight(); ++i) {
            for (int j = 0; j < javafxImage.getWidth(); ++j) {
                javafx.scene.paint.Color javafxColor = javafxImage.getPixelReader().getColor(j, i);
                double color = colorToComponent.apply(new Color(javafxColor.getRed(), javafxColor.getGreen(), javafxColor.getBlue()));

                double newY = (canvas.getHeight() - MathUtil.clamp(color * 255.0 * yScaler, 0, canvas.getHeight()));
                double newX = MathUtil.clamp(startX + j * xScaler, 0, canvas.getWidth());

                graphics.fillOval(newX, newY, 3.0, 3.0);
            }
        }
    }

    protected void openTab() {
        BorderPane borderPane = new BorderPane();

        canvas = new Canvas(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        canvas.widthProperty().bind(borderPane.widthProperty().subtract(10));
        canvas.heightProperty().bind(borderPane.heightProperty().subtract(10));

        borderPane.setCenter(canvas);

        canvas.widthProperty().addListener(e -> updateCanvasWithImage(previouslyDisplayedImage));
        canvas.heightProperty().addListener(e -> updateCanvasWithImage(previouslyDisplayedImage));

        this.setContent(borderPane);
        this.setText("RGB waveform");
    }

}
