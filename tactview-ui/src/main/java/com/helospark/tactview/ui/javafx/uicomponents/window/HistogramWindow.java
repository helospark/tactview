package com.helospark.tactview.ui.javafx.uicomponents.window;

import static javafx.scene.paint.Color.BLACK;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

@Component
@Order(4003)
public class HistogramWindow extends DetachableTab implements DisplayUpdatedListener {
    public static final String ID = "histogram-tab";
    private static final int DEFAULT_WIDTH = 500;
    private static final int DEFAULT_HEIGHT = 300;

    private static final Map<String, Function<Color, Double>> COLOR_MAPPERS;
    static {
        COLOR_MAPPERS = new LinkedHashMap<>();
        COLOR_MAPPERS.put("value", color -> ((color.red + color.green + color.blue) / 3.0));
        COLOR_MAPPERS.put("red", color -> (color.red));
        COLOR_MAPPERS.put("green", color -> (color.green));
        COLOR_MAPPERS.put("blue", color -> (color.blue));
    }

    private DockableTabRepository dockableTabRepository;

    private Canvas canvas;

    private Image previouslyDisplayedImage = new WritableImage(10, 10);
    private ToggleGroup group;

    public HistogramWindow(DockableTabRepository dockableTabRepository) {
        super(ID);
        this.dockableTabRepository = dockableTabRepository;

        this.openTab();
    }

    @Override
    public void displayUpdated(DisplayUpdatedRequest request) {
        previouslyDisplayedImage = request.image;
        updateDisplayWithPreviousImageIfRequired();
    }

    private void updateDisplayWithPreviousImageIfRequired() {
        if (!dockableTabRepository.isTabVisibleWithId(ID)) {
            return;
        }
        Image javafxImage = previouslyDisplayedImage;
        updateCanvasWithImage(javafxImage);
    }

    private void updateCanvasWithImage(Image javafxImage) {
        GraphicsContext graphics = canvas.getGraphicsContext2D();

        Function<Color, Double> colorMapper = getColorMapper();

        graphics.setFill(BLACK);
        graphics.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        int[] values = new int[256];

        for (int i = 0; i < javafxImage.getHeight(); ++i) {
            for (int j = 0; j < javafxImage.getWidth(); ++j) {
                javafx.scene.paint.Color javafxColor = javafxImage.getPixelReader().getColor(j, i);
                Color color = new Color(javafxColor.getRed(), javafxColor.getGreen(), javafxColor.getBlue()).multiplyComponents(255.0);

                int index = MathUtil.clampToInt(colorMapper.apply(color), 0, 255);

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

    private Function<Color, Double> getColorMapper() {
        return COLOR_MAPPERS.get(((RadioButton) group.getSelectedToggle()).getId());
    }

    protected void openTab() {
        BorderPane borderPane = new BorderPane();

        canvas = new Canvas(DEFAULT_WIDTH, DEFAULT_HEIGHT);

        borderPane.setCenter(canvas);

        group = new ToggleGroup();

        List<RadioButton> radioButtons = COLOR_MAPPERS.entrySet()
                .stream()
                .map(entry -> {
                    RadioButton radio = new RadioButton(entry.getKey());
                    radio.setId(entry.getKey());
                    radio.setToggleGroup(group);

                    return radio;
                })
                .collect(Collectors.toList());
        radioButtons.get(0).setSelected(true);

        group.selectedToggleProperty().addListener((e, oldValue, newValue) -> updateDisplayWithPreviousImageIfRequired());

        HBox hbox = new HBox();
        hbox.getChildren().addAll(radioButtons);
        borderPane.setTop(hbox);
        canvas.widthProperty().bind(borderPane.widthProperty().subtract(10));
        canvas.heightProperty().bind(borderPane.heightProperty().subtract(10).subtract(hbox.heightProperty()));

        canvas.widthProperty().addListener(e -> this.updateCanvasWithImage(previouslyDisplayedImage));
        canvas.heightProperty().addListener(e -> this.updateCanvasWithImage(previouslyDisplayedImage));

        this.setContent(borderPane);
        this.updateCanvasWithImage(previouslyDisplayedImage);
        this.setText("Histogram");
    }

}
