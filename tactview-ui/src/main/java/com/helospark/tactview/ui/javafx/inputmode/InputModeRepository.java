package com.helospark.tactview.ui.javafx.inputmode;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Line;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.SizeFunction;
import com.helospark.tactview.ui.javafx.DisplayUpdaterService;
import com.helospark.tactview.ui.javafx.inputmode.sizefunction.SizeFunctionImplementation;
import com.helospark.tactview.ui.javafx.inputmode.strategy.InputTypeStrategy;
import com.helospark.tactview.ui.javafx.inputmode.strategy.LineInputTypeStrategy;
import com.helospark.tactview.ui.javafx.inputmode.strategy.PointInputTypeStrategy;
import com.helospark.tactview.ui.javafx.inputmode.strategy.ResultType;
import com.helospark.tactview.ui.javafx.repository.UiProjectRepository;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

@Component
public class InputModeRepository {
    private SizeFunctionImplementation sizeFunctionImplementation;
    private Canvas canvas;
    private UiProjectRepository projectRepository;
    private DisplayUpdaterService displayUpdaterService;
    private InputModeInput<?> inputModeInput;
    private List<Consumer<Boolean>> inputModeConsumer = new ArrayList<>();

    public InputModeRepository(UiProjectRepository projectRepository, DisplayUpdaterService displayUpdaterService,
            SizeFunctionImplementation sizeFunctionImplementation) {
        this.projectRepository = projectRepository;
        this.displayUpdaterService = displayUpdaterService;
        this.sizeFunctionImplementation = sizeFunctionImplementation;
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

    public void requestLine(Consumer<Line> consumer, SizeFunction sizeFunction) {
        InputTypeStrategy<Line> currentStrategy = new LineInputTypeStrategy();
        this.inputModeInput = new InputModeInput<>(Line.class, consumer, currentStrategy, sizeFunction);
        inputModeChanged(true);
    }

    private void processStrategy() {
        canvas.setOnMousePressed(createHandler((x, y, e) -> inputModeInput.currentStrategy.onMouseDownEvent(x, y, e)));
        canvas.setOnMouseReleased(createHandler((x, y, e) -> inputModeInput.currentStrategy.onMouseUpEvent(x, y, e)));
        canvas.setOnMouseDragged(createHandler((x, y, e) -> inputModeInput.currentStrategy.onMouseDraggedEvent(x, y, e)));
        EventHandler<? super MouseEvent> mouseMoveHandler = createHandler((x, y, e) -> inputModeInput.currentStrategy.onMouseMovedEvent(x, y, e));
        canvas.setOnMouseMoved(e -> {
            mouseMoveHandler.handle(e);
            GraphicsContext graphics = canvas.getGraphicsContext2D();
            if (inputModeInput != null) {
                Platform.runLater(() -> inputModeInput.currentStrategy.draw(graphics));
            }
        });
        canvas.setOnMouseExited(e -> displayUpdaterService.updateCurrentPosition());
    }

    private EventHandler<? super MouseEvent> createHandler(TriFunction<Double, Double, MouseEvent> function) {
        return e -> {
            if (inputModeInput != null) {
                double x = (sizeFunctionImplementation.scalePreviewDataUsingSizeFunction(e.getX(), inputModeInput.sizeFunction, projectRepository.getPreviewWidth()));
                double y = (sizeFunctionImplementation.scalePreviewDataUsingSizeFunction(e.getY(), inputModeInput.sizeFunction, projectRepository.getPreviewHeight()));
                function.apply(x, y, e);
                if (inputModeInput.currentStrategy.getResultType() == ResultType.PARTIAL) {
                    handleStrategyHasResult();
                }
                if (inputModeInput.currentStrategy.getResultType() == ResultType.DONE) {
                    handleStrategyHasResult();
                    reset();
                }
            }
            // TODO: should not be here
            displayUpdaterService.updateCurrentPosition();
            Platform.runLater(() -> {
                GraphicsContext graphics = canvas.getGraphicsContext2D();
                graphics.setStroke(Color.RED);
                graphics.strokeLine(0, e.getY(), canvas.getWidth(), e.getY());
                graphics.strokeLine(e.getX(), 0, e.getX(), canvas.getHeight());
            });
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

    @FunctionalInterface
    private interface TriFunction<A, B, C> {
        void apply(A a, B b, C c);
    }

    public void reset() {
        inputModeInput = null;
        inputModeChanged(false);
    }

}
