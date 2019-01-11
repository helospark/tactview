package com.helospark.tactview.ui.javafx.inputmode;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Color;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.InterpolationLine;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Polygon;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.SizeFunction;
import com.helospark.tactview.ui.javafx.DisplayUpdaterService;
import com.helospark.tactview.ui.javafx.PlaybackController;
import com.helospark.tactview.ui.javafx.UiTimelineManager;
import com.helospark.tactview.ui.javafx.inputmode.sizefunction.SizeFunctionImplementation;
import com.helospark.tactview.ui.javafx.inputmode.strategy.ColorInputTypeStrategy;
import com.helospark.tactview.ui.javafx.inputmode.strategy.InputTypeStrategy;
import com.helospark.tactview.ui.javafx.inputmode.strategy.LineInputTypeStrategy;
import com.helospark.tactview.ui.javafx.inputmode.strategy.PointInputTypeStrategy;
import com.helospark.tactview.ui.javafx.inputmode.strategy.PolygonInputTypeStrategy;
import com.helospark.tactview.ui.javafx.inputmode.strategy.ResultType;
import com.helospark.tactview.ui.javafx.inputmode.strategy.StrategyInput;
import com.helospark.tactview.ui.javafx.repository.UiProjectRepository;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;

@Component
public class InputModeRepository {
    private SizeFunctionImplementation sizeFunctionImplementation;
    private Canvas canvas;
    private UiProjectRepository projectRepository;
    private DisplayUpdaterService displayUpdaterService;
    private InputModeInput<?> inputModeInput;
    private PlaybackController playbackController;
    private UiTimelineManager timelineManager;
    private List<Consumer<Boolean>> inputModeConsumer = new ArrayList<>();

    public InputModeRepository(UiProjectRepository projectRepository, DisplayUpdaterService displayUpdaterService,
            SizeFunctionImplementation sizeFunctionImplementation, PlaybackController playbackController,
            UiTimelineManager timelineManager) {
        this.projectRepository = projectRepository;
        this.displayUpdaterService = displayUpdaterService;
        this.sizeFunctionImplementation = sizeFunctionImplementation;
        this.playbackController = playbackController;
        this.timelineManager = timelineManager;
    }

    // TODO: should this be in DI framework?
    public void setCanvas(Canvas canvas) {
        this.canvas = canvas;
        processStrategy();
    }

    public void registerInputModeChangeConsumerr(Consumer<Boolean> inputModeConsumer) {
        this.inputModeConsumer.add(inputModeConsumer);
    }

    public void requestPoint(Consumer<Point> consumer, SizeFunction sizeFunction) {
        InputTypeStrategy<Point> currentStrategy = new PointInputTypeStrategy();
        this.inputModeInput = new InputModeInput<>(Point.class, consumer, currentStrategy, sizeFunction);
        inputModeChanged(true);
    }

    public void requestLine(Consumer<InterpolationLine> consumer, SizeFunction sizeFunction) {
        InputTypeStrategy<InterpolationLine> currentStrategy = new LineInputTypeStrategy();
        this.inputModeInput = new InputModeInput<>(InterpolationLine.class, consumer, currentStrategy, sizeFunction);
        inputModeChanged(true);
    }

    public void requestPolygon(Consumer<Polygon> consumer, SizeFunction sizeFunction) {
        InputTypeStrategy<Polygon> currentStrategy = new PolygonInputTypeStrategy();
        this.inputModeInput = new InputModeInput<>(Polygon.class, consumer, currentStrategy, sizeFunction);
        inputModeChanged(true);
    }

    public void requestColor(Consumer<Color> consumer) {
        InputTypeStrategy<Color> currentStrategy = new ColorInputTypeStrategy();
        this.inputModeInput = new InputModeInput<>(Color.class, consumer, currentStrategy, SizeFunction.CLAMP_TO_MIN_MAX);
        inputModeChanged(true);
    }

    private void processStrategy() {
        canvas.setOnMousePressed(createHandler(input -> inputModeInput.currentStrategy.onMouseDownEvent(input)));
        canvas.setOnMouseReleased(createHandler(input -> inputModeInput.currentStrategy.onMouseUpEvent(input)));
        canvas.setOnMouseDragged(createHandler(input -> inputModeInput.currentStrategy.onMouseDraggedEvent(input)));
        EventHandler<? super MouseEvent> mouseMoveHandler = createHandler(input -> inputModeInput.currentStrategy.onMouseMovedEvent(input));
        canvas.setOnMouseMoved(e -> {
            mouseMoveHandler.handle(e);
            GraphicsContext graphics = canvas.getGraphicsContext2D();
            if (inputModeInput != null) {
                // TODO: should not be here
                displayUpdaterService.updateCurrentPosition();
                Platform.runLater(() -> {
                    graphics.setStroke(javafx.scene.paint.Color.RED);
                    graphics.strokeLine(0, e.getY(), canvas.getWidth(), e.getY());
                    graphics.strokeLine(e.getX(), 0, e.getX(), canvas.getHeight());
                    if (inputModeInput != null) {
                        inputModeInput.currentStrategy.draw(graphics);
                    }
                });
            }
        });
        canvas.setOnMouseExited(e -> displayUpdaterService.updateCurrentPosition());
    }

    private EventHandler<? super MouseEvent> createHandler(Consumer<StrategyInput> function) {
        return e -> {
            if (inputModeInput != null) {
                double x = (sizeFunctionImplementation.scalePreviewDataUsingSizeFunction(e.getX(), inputModeInput.sizeFunction, projectRepository.getPreviewWidth()));
                double y = (sizeFunctionImplementation.scalePreviewDataUsingSizeFunction(e.getY(), inputModeInput.sizeFunction, projectRepository.getPreviewHeight()));

                StrategyInput strategyInput = StrategyInput.builder()
                        .withx(x)
                        .withy(y)
                        .withMouseEvent(e)
                        .withUnscaledX(e.getX())
                        .withUnscaledY(e.getY())
                        .withCanvasImage(() -> {
                            return playbackController.getFrameAt(timelineManager.getCurrentPosition()).getImage();
                        })
                        .build();

                function.accept(strategyInput);
                if (inputModeInput.currentStrategy.getResultType() == ResultType.PARTIAL) {
                    handleStrategyHasResult();
                }
                if (inputModeInput.currentStrategy.getResultType() == ResultType.DONE) {
                    handleStrategyHasResult();
                    reset();
                }
            }
        };
    }

    private <T> void handleStrategyHasResult() {
        InputModeInput<T> commonType = (InputModeInput<T>) inputModeInput;
        commonType.consumer.accept(commonType.currentStrategy.getResult());
    }

    private void inputModeChanged(boolean b) {
        inputModeConsumer.stream()
                .forEach(consumer -> consumer.accept(b));
    }

    public void reset() {
        inputModeInput = null;
        inputModeChanged(false);
    }

}
