package com.helospark.tactview.ui.javafx.inputmode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.TimelineRenderResult.RegularRectangle;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Color;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.InterpolationLine;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Polygon;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Rectangle;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.SizeFunction;
import com.helospark.tactview.core.timeline.proceduralclip.polygon.impl.bezier.BezierPolygon;
import com.helospark.tactview.core.timeline.proceduralclip.polygon.impl.bezier.BezierPolygonPoint;
import com.helospark.tactview.ui.javafx.CanvasStateHolder;
import com.helospark.tactview.ui.javafx.DisplayUpdaterService;
import com.helospark.tactview.ui.javafx.GetVideoFrameRequest;
import com.helospark.tactview.ui.javafx.GlobalTimelinePositionHolder;
import com.helospark.tactview.ui.javafx.JavaDisplayableAudioVideoFragment;
import com.helospark.tactview.ui.javafx.PlaybackFrameAccessor;
import com.helospark.tactview.ui.javafx.inputmode.sizefunction.SizeFunctionImplementation;
import com.helospark.tactview.ui.javafx.inputmode.strategy.BezierPolygonInputTypeStrategy;
import com.helospark.tactview.ui.javafx.inputmode.strategy.ColorInputTypeStrategy;
import com.helospark.tactview.ui.javafx.inputmode.strategy.DrawRequestParameter;
import com.helospark.tactview.ui.javafx.inputmode.strategy.InputTypeStrategy;
import com.helospark.tactview.ui.javafx.inputmode.strategy.LineInputTypeStrategy;
import com.helospark.tactview.ui.javafx.inputmode.strategy.PointInputTypeStrategy;
import com.helospark.tactview.ui.javafx.inputmode.strategy.PolygonInputTypeStrategy;
import com.helospark.tactview.ui.javafx.inputmode.strategy.RectangleInputTypeStrategy;
import com.helospark.tactview.ui.javafx.inputmode.strategy.RelativePointInputTypeStrategy;
import com.helospark.tactview.ui.javafx.inputmode.strategy.ResultType;
import com.helospark.tactview.ui.javafx.inputmode.strategy.StrategyKeyInput;
import com.helospark.tactview.ui.javafx.inputmode.strategy.StrategyMouseInput;
import com.helospark.tactview.ui.javafx.inputmode.strategy.generalops.GeneralCanvasOperationStrategy;
import com.helospark.tactview.ui.javafx.inputmode.strategy.generalops.GeneralCanvasOperationsMouseRequest;
import com.helospark.tactview.ui.javafx.key.CurrentlyPressedKeyRepository;
import com.helospark.tactview.ui.javafx.repository.CleanableMode;
import com.helospark.tactview.ui.javafx.repository.SelectedNodeRepository;
import com.helospark.tactview.ui.javafx.repository.UiProjectRepository;
import com.helospark.tactview.ui.javafx.tabs.dockabletab.impl.PreviewDockableTab;
import com.helospark.tactview.ui.javafx.uicomponents.VideoStatusBarUpdater;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;

@Component
public class InputModeRepository implements CleanableMode {
    private SizeFunctionImplementation sizeFunctionImplementation;
    private Canvas canvas;
    private DisplayUpdaterService displayUpdaterService;
    private UiProjectRepository projectRepository;
    private InputModeInput<?> inputModeInput;
    private PlaybackFrameAccessor playbackController;
    private GlobalTimelinePositionHolder timelineManager;
    private VideoStatusBarUpdater videoStatusBarUpdater;
    private CurrentlyPressedKeyRepository currentlyPressedKeyRepository;
    private GeneralCanvasOperationStrategy generalCanvasOperationStrategy;
    private SelectedNodeRepository selectedNodeRepository;
    private List<Consumer<Boolean>> inputModeConsumer = new ArrayList<>();
    private CanvasStateHolder canvasStateHolder;
    private UiProjectRepository uiProjectRepository;

    public InputModeRepository(UiProjectRepository projectRepository,
            SizeFunctionImplementation sizeFunctionImplementation, PlaybackFrameAccessor playbackController,
            GlobalTimelinePositionHolder timelineManager, VideoStatusBarUpdater videoStatusBarUpdater,
            CurrentlyPressedKeyRepository currentlyPressedKeyRepository, GeneralCanvasOperationStrategy generalCanvasOperationStrategy, SelectedNodeRepository selectedNodeRepository,
            CanvasStateHolder canvasStateHolder, UiProjectRepository uiProjectRepository, PreviewDockableTab previewDockableTab) {
        this.projectRepository = projectRepository;
        this.sizeFunctionImplementation = sizeFunctionImplementation;
        this.playbackController = playbackController;
        this.timelineManager = timelineManager;
        this.videoStatusBarUpdater = videoStatusBarUpdater;
        this.currentlyPressedKeyRepository = currentlyPressedKeyRepository;
        this.generalCanvasOperationStrategy = generalCanvasOperationStrategy;
        this.selectedNodeRepository = selectedNodeRepository;
        this.canvasStateHolder = canvasStateHolder;
        this.uiProjectRepository = uiProjectRepository;
        this.displayUpdaterService = previewDockableTab.getDisplayUpdaterService();
    }

    // TODO: should this be in DI framework?
    public void setCanvas(Canvas canvas) {
        this.canvas = canvas;
        processStrategy();
    }

    public void registerInputModeChangeConsumerr(Consumer<Boolean> inputModeConsumer) {
        this.inputModeConsumer.add(inputModeConsumer);
    }

    public void requestPoint(Consumer<Point> consumer, SizeFunction sizeFunction, Point previousValue) {
        InputTypeStrategy<Point> currentStrategy = new PointInputTypeStrategy(previousValue);
        this.inputModeInput = new InputModeInput<>(Point.class, consumer, currentStrategy, sizeFunction);
        inputModeChanged(true);
        Platform.runLater(() -> updateCanvasWithStrategy(canvas.getGraphicsContext2D(), null));
    }

    public void requestRelativePoint(Consumer<Point> consumer, SizeFunction sizeFunction, Point currentPoint) {
        InputTypeStrategy<Point> currentStrategy = new RelativePointInputTypeStrategy(currentPoint);
        this.inputModeInput = new InputModeInput<>(Point.class, consumer, currentStrategy, sizeFunction);
        inputModeChanged(true);
        Platform.runLater(() -> updateCanvasWithStrategy(canvas.getGraphicsContext2D(), null));
    }

    public void requestLine(Consumer<InterpolationLine> consumer, InterpolationLine interpolationLine, SizeFunction sizeFunction) {
        InputTypeStrategy<InterpolationLine> currentStrategy = new LineInputTypeStrategy(interpolationLine);
        this.inputModeInput = new InputModeInput<>(InterpolationLine.class, consumer, currentStrategy, sizeFunction);
        inputModeChanged(true);
        Platform.runLater(() -> updateCanvasWithStrategy(canvas.getGraphicsContext2D(), null));
    }

    public void requestPolygon(Consumer<Polygon> consumer, SizeFunction sizeFunction) {
        InputTypeStrategy<Polygon> currentStrategy = new PolygonInputTypeStrategy();
        this.inputModeInput = new InputModeInput<>(Polygon.class, consumer, currentStrategy, sizeFunction);
        inputModeChanged(true);
        Platform.runLater(() -> updateCanvasWithStrategy(canvas.getGraphicsContext2D(), null));
    }

    public void requestRectangle(Consumer<Rectangle> consumer, List<Point> rectangle, SizeFunction sizeFunction) {
        InputTypeStrategy<Rectangle> currentStrategy = new RectangleInputTypeStrategy(rectangle);
        this.inputModeInput = new InputModeInput<>(Rectangle.class, consumer, currentStrategy, sizeFunction);
        inputModeChanged(true);
        Platform.runLater(() -> updateCanvasWithStrategy(canvas.getGraphicsContext2D(), null));
    }

    public void requestPolygonPrefilled(Consumer<Polygon> consumer, SizeFunction sizeFunction, List<Point> polygon) {
        InputTypeStrategy<Polygon> currentStrategy = new PolygonInputTypeStrategy(polygon);
        this.inputModeInput = new InputModeInput<>(Polygon.class, consumer, currentStrategy, sizeFunction);
        inputModeChanged(true);
        Platform.runLater(() -> updateCanvasWithStrategy(canvas.getGraphicsContext2D(), null));
    }

    public void requestBezierPolygon(Consumer<BezierPolygon> consumer, SizeFunction sizeFunction) {
        InputTypeStrategy<BezierPolygon> currentStrategy = new BezierPolygonInputTypeStrategy();
        this.inputModeInput = new InputModeInput<>(BezierPolygon.class, consumer, currentStrategy, sizeFunction);
        inputModeChanged(true);
        Platform.runLater(() -> updateCanvasWithStrategy(canvas.getGraphicsContext2D(), null));
    }

    public void requestBezierPolygonPrefilled(Consumer<BezierPolygon> consumer, SizeFunction sizeFunction, List<BezierPolygonPoint> points) {
        InputTypeStrategy<BezierPolygon> currentStrategy = new BezierPolygonInputTypeStrategy(points);
        this.inputModeInput = new InputModeInput<>(BezierPolygon.class, consumer, currentStrategy, sizeFunction);
        inputModeChanged(true);
        Platform.runLater(() -> updateCanvasWithStrategy(canvas.getGraphicsContext2D(), null));
    }

    public void requestColor(Consumer<Color> consumer) {
        InputTypeStrategy<Color> currentStrategy = new ColorInputTypeStrategy(currentlyPressedKeyRepository);
        this.inputModeInput = new InputModeInput<>(Color.class, consumer, currentStrategy, SizeFunction.CLAMP_TO_MIN_MAX);
        inputModeChanged(true);
        Platform.runLater(() -> updateCanvasWithStrategy(canvas.getGraphicsContext2D(), null));
    }

    private void processStrategy() {
        canvas.setOnMousePressed(createMouseHandler(input -> inputModeInput.currentStrategy.onMouseDownEvent(input), input -> generalCanvasOperationStrategy.onMouseDownEvent(input)));
        canvas.setOnMouseReleased(createMouseHandler(input -> inputModeInput.currentStrategy.onMouseUpEvent(input), input -> generalCanvasOperationStrategy.onMouseUpEvent(input)));
        canvas.setOnMouseDragged(createMouseHandler(input -> inputModeInput.currentStrategy.onMouseDraggedEvent(input), input -> generalCanvasOperationStrategy.onMouseDraggedEvent(input)));
        currentlyPressedKeyRepository.onKeyDown(createKeyHandler(input -> inputModeInput.currentStrategy.onKeyPressedEvent(input)));

        EventHandler<? super MouseEvent> mouseMoveHandler = createMouseHandler(input -> inputModeInput.currentStrategy.onMouseMovedEvent(input),
                input -> generalCanvasOperationStrategy.onMouseMovedEvent(input));

        canvas.setOnMouseMoved(e -> {
            mouseMoveHandler.handle(e);
            updateCanvas(e);
        });
        canvas.setOnMouseExited(e -> {
            displayUpdaterService.updateCurrentPositionWithInvalidatedCache();
            generalCanvasOperationStrategy.onMouseExited(canvas);
            updateCanvasWithStrategy(canvas.getGraphicsContext2D(), null);
        });
    }

    private void updateCanvas(MouseEvent e) {
        GraphicsContext graphics = canvas.getGraphicsContext2D();
        if (inputModeInput != null) {
            // TODO: should not be here
            displayUpdaterService.updateCurrentPositionWithInvalidatedCache();
            Platform.runLater(() -> {
                graphics.setStroke(javafx.scene.paint.Color.RED);
                graphics.strokeLine(0, e.getY(), canvas.getWidth(), e.getY());
                graphics.strokeLine(e.getX(), 0, e.getX(), canvas.getHeight());
                updateCanvasWithStrategy(graphics, e);
            });
        }
    }

    private void updateCanvasWithStrategy(GraphicsContext graphics, MouseEvent e) {
        if (inputModeInput != null) {
            DrawRequestParameter request = DrawRequestParameter.builder()
                    .withCanvas(graphics)
                    .withWidth(uiProjectRepository.getPreviewWidth())
                    .withHeight(uiProjectRepository.getPreviewHeight())
                    .withMouseInCanvas(e != null)
                    .withMouseEvent(Optional.ofNullable(e))
                    .withCanvasTranslateX(canvasStateHolder.getTranslateX())
                    .withCanvasTranslateY(canvasStateHolder.getTranslateY())
                    .build();
            inputModeInput.currentStrategy.draw(request);
        }
    }

    private EventHandler<? super MouseEvent> createMouseHandler(Consumer<StrategyMouseInput> function, Consumer<GeneralCanvasOperationsMouseRequest> fallbackHandler) {
        return e -> {
            JavaDisplayableAudioVideoFragment cacheCurrentImage = displayUpdaterService.getCacheCurrentImage();
            double unmodifiedX = e.getX() - canvasStateHolder.getTranslateX();
            double unmodifiedY = e.getY() - canvasStateHolder.getTranslateY();
            if (inputModeInput != null) {
                double x = (sizeFunctionImplementation.scalePreviewDataUsingSizeFunction(unmodifiedX, inputModeInput.sizeFunction, projectRepository.getPreviewWidth()));
                double y = (sizeFunctionImplementation.scalePreviewDataUsingSizeFunction(unmodifiedY, inputModeInput.sizeFunction, projectRepository.getPreviewHeight()));

                StrategyMouseInput strategyInput = StrategyMouseInput.builder()
                        .withx(x)
                        .withy(y)
                        .withMouseEvent(e)
                        .withUnscaledX(unmodifiedX)
                        .withUnscaledY(unmodifiedY)
                        .withCanvasImage(() -> {
                            return playbackController.getVideoFrameAt(new GetVideoFrameRequest(timelineManager.getCurrentPosition(), Optional.empty(), false, false)).getImage();
                        })
                        .withCurrentlyPressedKeyRepository(currentlyPressedKeyRepository)
                        .build();

                function.accept(strategyInput);
                if (inputModeInput.currentStrategy.getResultType() == ResultType.PARTIAL) {
                    handleStrategyHasResult();
                }
                if (inputModeInput.currentStrategy.getResultType() == ResultType.DONE) {
                    handleStrategyHasResult();
                    reset();
                }
                updateCanvas(e);
            } else if (cacheCurrentImage != null) {
                Map<String, RegularRectangle> filteredRectangles = cacheCurrentImage.getClipRectangle()
                        .entrySet()
                        .stream()
                        .filter(entry -> selectedNodeRepository.getSelectedClipIds().contains(entry.getKey()))
                        .collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue()));
                GeneralCanvasOperationsMouseRequest fallbackRequest = GeneralCanvasOperationsMouseRequest.builder()
                        .withCanvas(canvas)
                        .withUnscaledX(unmodifiedX)
                        .withUnscaledY(unmodifiedY)
                        .withCanvasRelativeX(e.getX())
                        .withCanvasRelativeY(e.getY())
                        .withx(unmodifiedX / projectRepository.getPreviewWidth())
                        .withy(unmodifiedY / projectRepository.getPreviewHeight())
                        .withMouseEvent(e)
                        .withRectangles(filteredRectangles)
                        .build();
                fallbackHandler.accept(fallbackRequest);
            }
        };
    }

    private Consumer<KeyCode> createKeyHandler(Consumer<StrategyKeyInput> function) {
        return e -> {
            if (inputModeInput != null) {
                StrategyKeyInput strategyInput = new StrategyKeyInput(e);

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

    private void inputModeChanged(boolean active) {
        inputModeConsumer.stream()
                .forEach(consumer -> consumer.accept(active));
        if (active) {
            String statusMessage = inputModeInput.currentStrategy.getStatusMessage();
            videoStatusBarUpdater.setText(statusMessage);
        }
    }

    public ResultType getResultType() {
        return this.inputModeInput.currentStrategy.getResultType();
    }

    public void reset() {
        inputModeInput = null;
        inputModeChanged(false);
        videoStatusBarUpdater.setText("");
    }

    @Override
    public void clean() {
        reset();
    }

    @Override
    public boolean isClean() {
        return inputModeInput == null;
    }

    @Override
    public int cleanPriority() {
        return 100;
    }

    @Override
    public boolean shouldConsumeCleanEvent() {
        return true;
    }

}
