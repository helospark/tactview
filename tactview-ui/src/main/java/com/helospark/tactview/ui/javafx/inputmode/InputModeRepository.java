package com.helospark.tactview.ui.javafx.inputmode;

import java.util.function.Consumer;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;
import com.helospark.tactview.ui.javafx.inputmode.strategy.InputTypeStrategy;
import com.helospark.tactview.ui.javafx.inputmode.strategy.PointInputTypeStrategy;
import com.helospark.tactview.ui.javafx.inputmode.strategy.ResultType;
import com.helospark.tactview.ui.javafx.repository.UiProjectRepository;

import javafx.event.EventHandler;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseEvent;

@Component
public class InputModeRepository {
    private Canvas canvas;
    private UiProjectRepository projectRepository;
    private InputModeInput<?> inputModeInput;

    public InputModeRepository(UiProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    // TODO: should this be in DI framework?
    public void setCanvas(Canvas canvas) {
        this.canvas = canvas;
        processStrategy();
    }

    public void requestPoint(Consumer<Point> consumer) {
        InputTypeStrategy<Point> currentStrategy = new PointInputTypeStrategy();
        this.inputModeInput = new InputModeInput<>(Point.class, consumer, currentStrategy);
    }

    private void processStrategy() {
        canvas.setOnMousePressed(createHandler((x, y, e) -> inputModeInput.currentStrategy.onMouseDownEvent(x, y, e)));
        canvas.setOnMouseReleased(createHandler((x, y, e) -> inputModeInput.currentStrategy.onMouseUpEvent(x, y, e)));
        canvas.setOnMouseDragged(createHandler((x, y, e) -> inputModeInput.currentStrategy.onMouseDraggedEvent(x, y, e)));
        EventHandler<? super MouseEvent> mouseMoveHandler = createHandler((x, y, e) -> inputModeInput.currentStrategy.onMouseMovedEvent(x, y, e));
        canvas.setOnMouseMoved(e -> {
            mouseMoveHandler.handle(e);
            if (inputModeInput != null) {
                inputModeInput.currentStrategy.draw(canvas.getGraphicsContext2D());
            }
        });
    }

    private EventHandler<? super MouseEvent> createHandler(TriFunction<Integer, Integer, MouseEvent> function) {
        return e -> {
            if (inputModeInput != null) {
                int x = (int) (e.getX() * 1.0 / projectRepository.getScaleFactor());
                int y = (int) (e.getY() * 1.0 / projectRepository.getScaleFactor());
                function.apply(x, y, e);
                if (inputModeInput.currentStrategy.getResultType() == ResultType.DONE) {
                    handleStrategyFinished();
                }
            }
        };
    }

    private <T> void handleStrategyFinished() {
        InputModeInput<T> commonType = (InputModeInput<T>) inputModeInput;
        commonType.consumer.accept(commonType.currentStrategy.getResult());
        inputModeInput = null;
    }

    @FunctionalInterface
    private interface TriFunction<A, B, C> {
        void apply(A a, B b, C c);
    }
}
