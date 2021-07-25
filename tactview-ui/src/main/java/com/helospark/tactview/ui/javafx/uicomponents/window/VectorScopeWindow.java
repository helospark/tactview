package com.helospark.tactview.ui.javafx.uicomponents.window;

import static javafx.scene.paint.Color.BLACK;

import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.annotation.Order;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Color;
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
@Order(4002)
public class VectorScopeWindow extends DetachableTab implements DisplayUpdatedListener {
    public static final String ID = "vectorscope-tab";
    private static final int BORDER = 10;
    private static final int INNER_CIRCLE_BORDER = 128;
    private static final int DEFAULT_WIDTH = 255 * 2;
    private static final int DEFAULT_HEIGHT = 255 * 2;
    private Canvas canvas;

    private Image previouslyDisplayedImage = new WritableImage(10, 10);

    private DockableTabRepository dockableTabRepository;

    public VectorScopeWindow(DockableTabRepository dockableTabRepository) {
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

        this.openTab();
    }

    private void updateCanvasWithImage(Image javafxImage) {
        double size = Math.min(canvas.getWidth(), canvas.getHeight());
        double width = size;
        double height = size;
        double innerCircleBorder = width / 4.0;
        GraphicsContext graphics = canvas.getGraphicsContext2D();

        graphics.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        graphics.setFill(BLACK);
        graphics.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        graphics.setStroke(javafx.scene.paint.Color.gray(0.4));
        graphics.strokeOval(0, 0, width, height);
        graphics.strokeOval(innerCircleBorder, innerCircleBorder, width - 2 * innerCircleBorder, height - 2 * innerCircleBorder);

        graphics.strokeLine(BORDER, BORDER, width - BORDER, height - BORDER);
        graphics.strokeLine(width - BORDER, BORDER, BORDER, height - BORDER);

        double centerX = width / 2.0;
        double centerY = height / 2.0;

        graphics.setFill(new javafx.scene.paint.Color(0.3, 0.8, 0.3, 0.1));

        double xScaler = width / 255.0 / 2.0;
        double yScaler = height / 255.0 / 2.0;

        for (int i = 0; i < javafxImage.getHeight(); ++i) {
            for (int j = 0; j < javafxImage.getWidth(); ++j) {
                javafx.scene.paint.Color javafxColor = javafxImage.getPixelReader().getColor(j, i);
                Color color = new Color(javafxColor.getRed(), javafxColor.getGreen(), javafxColor.getBlue()).rgbToHsl();

                double colorRadian = (color.red * 2.0 - 1.0) * Math.PI;
                double x = centerX + Math.cos(colorRadian) * color.green * 255.0 * xScaler;
                double y = centerY + Math.sin(colorRadian) * color.green * 255.0 * yScaler;

                graphics.fillOval(x, y, 3.0, 3.0);
            }
        }
    }

    protected void openTab() {
        BorderPane borderPane = new BorderPane();

        canvas = new Canvas(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        canvas.widthProperty().bind(borderPane.widthProperty().subtract(10));
        canvas.heightProperty().bind(borderPane.heightProperty().subtract(10));
        canvas.widthProperty().addListener(e -> updateCanvasWithImage(previouslyDisplayedImage));
        canvas.heightProperty().addListener(e -> updateCanvasWithImage(previouslyDisplayedImage));

        borderPane.setCenter(canvas);

        this.setContent(borderPane);
        this.setText("Vectorscope");
    }

}
