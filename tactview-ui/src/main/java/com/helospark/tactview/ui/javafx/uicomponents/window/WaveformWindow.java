package com.helospark.tactview.ui.javafx.uicomponents.window;

import static javafx.scene.paint.Color.BLACK;

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
import javafx.scene.effect.BlendMode;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;

@Component
@Order(4000)
public class WaveformWindow extends DetachableTab implements DisplayUpdatedListener {
    private static final String ID = "waveform-tab";
    private static final int DEFAULT_WIDTH = 400;
    private static final int DEFAULT_HEIGHT = 255;

    private DockableTabRepository dockableTabRepository;

    private Canvas canvas;

    private boolean isUsingCoolLookingAddition = false;

    private Image previouslyDisplayedImage = new WritableImage(10, 10);

    public WaveformWindow(DockableTabRepository dockableTabRepository) {
        super(ID);
        this.dockableTabRepository = dockableTabRepository;
        this.createTab();
    }

    @Override
    public void displayUpdated(DisplayUpdatedRequest request) {
        this.previouslyDisplayedImage = request.image;
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

        double xScaler = canvas.getWidth() / javafxImage.getWidth();
        double yScaler = canvas.getHeight() / 255;

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

                double newY = (canvas.getHeight() - MathUtil.clamp((color.red + color.green + color.blue) / 3.0 * 255.0 * yScaler, 0, canvas.getHeight()));
                double newX = MathUtil.clamp(j * xScaler, 0, canvas.getWidth());

                graphics.fillOval(newX, newY, 3.0, 3.0);
            }
        }
    }

    protected void createTab() {
        BorderPane borderPane = new BorderPane();

        canvas = new Canvas(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        canvas.widthProperty().bind(borderPane.widthProperty().subtract(10));
        canvas.heightProperty().bind(borderPane.heightProperty().subtract(10));

        canvas.widthProperty().addListener(e -> updateCanvasWithImage(previouslyDisplayedImage));
        canvas.heightProperty().addListener(e -> updateCanvasWithImage(previouslyDisplayedImage));

        borderPane.setCenter(canvas);

        this.setContent(borderPane);
        this.setText("waveform");

    }

}
