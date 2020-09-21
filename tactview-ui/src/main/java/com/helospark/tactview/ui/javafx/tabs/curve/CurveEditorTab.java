package com.helospark.tactview.ui.javafx.tabs.curve;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.message.InterpolatorChangedMessage;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.EffectParametersRepository;
import com.helospark.tactview.core.timeline.effect.interpolation.KeyframeableEffect;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.DoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.EffectInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.IntegerProvider;
import com.helospark.tactview.core.timeline.message.ClipMovedMessage;
import com.helospark.tactview.core.timeline.message.EffectMovedMessage;
import com.helospark.tactview.core.timeline.message.KeyframeSuccesfullyAddedMessage;
import com.helospark.tactview.core.timeline.message.KeyframeSuccesfullyRemovedMessage;
import com.helospark.tactview.core.util.messaging.MessagingService;
import com.helospark.tactview.ui.javafx.TabCloseListener;
import com.helospark.tactview.ui.javafx.UiTimelineManager;
import com.helospark.tactview.ui.javafx.scenepostprocessor.ScenePostProcessor;
import com.helospark.tactview.ui.javafx.tabs.TabActiveRequest;
import com.helospark.tactview.ui.javafx.tabs.curve.curveeditor.ControlInitializationRequest;
import com.helospark.tactview.ui.javafx.tabs.curve.curveeditor.CurveDrawRequest;
import com.helospark.tactview.ui.javafx.tabs.curve.curveeditor.CurveEditor;
import com.helospark.tactview.ui.javafx.tabs.curve.curveeditor.CurveEditorMouseRequest;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

@Component
public class CurveEditorTab extends Tab implements ScenePostProcessor, TabCloseListener {
    private static final String CURVE_EDITOR_ID = "curve-editor";
    private static final double samplesPerPixel = 1.0;
    private final List<CurveEditor> curveEditors;
    private final UiTimelineManager timelineManager;
    private final EffectParametersRepository effectParametersRepository;
    private double positionOffset;

    private double secondsPerPixel = 1 / 20.0;
    private double scrollValue = 0.0;

    private Canvas canvas;
    private GridPane controlPane;

    private KeyframeableEffect currentKeyframeableEffect;
    private DoubleInterpolator currentInterpolator;
    private CurveEditor currentlyOpenEditor;
    private final MessagingService messagingService;

    private double maxValue;
    private double minValue;
    private double interval;
    private double displayScale;

    private Optional<Double> absoluteMinValue = Optional.empty();
    private Optional<Double> absoluteMaxValue = Optional.empty();

    Point lastMousePosition = null;

    /// dragging
    private boolean isLeftRightDragging = false;
    private boolean isDragging = false;
    private double lastDraggedX = 0;
    ///

    public CurveEditorTab(List<CurveEditor> curveEditors, MessagingService messagingService, UiTimelineManager timelineManager, EffectParametersRepository effectParametersRepository) {
        this.curveEditors = curveEditors;
        this.timelineManager = timelineManager;
        this.messagingService = messagingService;
        this.effectParametersRepository = effectParametersRepository;

        messagingService.register(KeyframeSuccesfullyAddedMessage.class, e -> {
            if (currentlyOpenEditor != null && e.getDescriptorId().equals(currentKeyframeableEffect.getId())) {
                Platform.runLater(() -> updateCanvas());
            }
        });
        messagingService.register(KeyframeSuccesfullyRemovedMessage.class, e -> {
            if (currentlyOpenEditor != null && e.getDescriptorId().equals(currentKeyframeableEffect.getId())) {
                Platform.runLater(() -> updateCanvas());
            }
        });
        messagingService.register(ClipMovedMessage.class, e -> {
            updateIfNeeded();
        });
        messagingService.register(EffectMovedMessage.class, e -> {
            updateIfNeeded();
        });
        messagingService.register(InterpolatorChangedMessage.class, e -> {
            if (this.currentKeyframeableEffect != null && (this.currentKeyframeableEffect.getId().equals(e.getDescriptorId()))) { // TODO: or child contains it
                Platform.runLater(() -> {
                    revealInEditor(this.currentKeyframeableEffect);
                    updateIfNeeded();
                });
            }
        });

        timelineManager.registerUiPlaybackConsumer(position -> {
            // TODO: only if it is revield
            updateCanvas();
        });
        this.setId(CURVE_EDITOR_ID);
    }

    private void updateIfNeeded() {
        if (currentlyOpenEditor != null) {
            Platform.runLater(() -> updateCanvas());
        }
    }

    public void revealInEditor(KeyframeableEffect valueProvider) {
        if (valueProvider instanceof DoubleProvider) {
            absoluteMinValue = Optional.of(((DoubleProvider) valueProvider).getMin());
            absoluteMaxValue = Optional.of(((DoubleProvider) valueProvider).getMax());
        }
        if (valueProvider instanceof IntegerProvider) {
            absoluteMinValue = Optional.of((double) ((IntegerProvider) valueProvider).getMin());
            absoluteMaxValue = Optional.of((double) ((IntegerProvider) valueProvider).getMax());
        }

        EffectInterpolator effectInterpolator = valueProvider.getInterpolator();
        if (effectInterpolator instanceof DoubleInterpolator) {

            this.currentInterpolator = (DoubleInterpolator) effectInterpolator;
            this.currentKeyframeableEffect = valueProvider;
            currentlyOpenEditor = curveEditors.stream()
                    .filter(editor -> editor.supports(currentInterpolator))
                    .findFirst()
                    .orElseThrow();
            controlPane.getChildren().clear();

            ControlInitializationRequest controlInitializationRequest = ControlInitializationRequest.builder()
                    .withEffectInterpolator(effectInterpolator)
                    .withGridToInitialize(controlPane)
                    .withUpdateRunnable(() -> Platform.runLater(() -> updateCanvas()))
                    .build();

            currentlyOpenEditor.initializeControl(controlInitializationRequest);

            // reset defaults
            secondsPerPixel = 1 / 20.0;
            scrollValue = 0.0;
            isLeftRightDragging = false;

            updateCanvas();
            messagingService.sendAsyncMessage(new TabActiveRequest(CURVE_EDITOR_ID));

        } else {
            throw new RuntimeException("Only keyframe supporting double interpolator is supported for now");
        }
    }

    private void clearCanvas() {
        GraphicsContext graphics = canvas.getGraphicsContext2D();
        graphics.setFill(Color.BLACK);
        graphics.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    private void updateCanvas() {
        if (currentlyOpenEditor == null) {
            return;
        }
        positionOffset = effectParametersRepository.findGlobalPositionForValueProvider(currentKeyframeableEffect.getId())
                .map(a -> a.getSeconds().doubleValue())
                .orElse(0.0);
        clearCanvas();
        GraphicsContext graphics = canvas.getGraphicsContext2D();
        double numberOfSamples = samplesPerPixel * canvas.getWidth();
        BigDecimal increment = new BigDecimal(secondsPerPixel);

        List<Double> values = new ArrayList<>();
        TimelinePosition current = new TimelinePosition(scrollValue - positionOffset);
        for (int i = 0; i < numberOfSamples; ++i) {
            values.add(currentInterpolator.valueAt(current));
            current = current.add(increment);
        }

        if (!isDragging ||
                (isDragging && (lastMousePosition.y < 10.0 || (canvas.getHeight() - lastMousePosition.y) < 10.0))) {
            maxValue = Collections.max(values);
            minValue = Collections.min(values);

            interval = (maxValue - minValue);

            if (absoluteMinValue.isPresent()) {
                double absoluteInterval = absoluteMaxValue.get() - absoluteMinValue.get();
                if (minValue < absoluteMinValue.get()) {
                    minValue = absoluteMinValue.get() - (absoluteInterval * 0.05);
                }
                if (maxValue > absoluteMaxValue.get()) {
                    maxValue = absoluteMaxValue.get() + (absoluteInterval * 0.05);
                }
            }

            interval = (maxValue - minValue);

            if (interval <= 0.1) {
                interval = 0.1;
                minValue -= (interval / 2);
                maxValue += (interval / 2);
            } else {
                maxValue += interval / 5;
                minValue -= interval / 5;
            }
            interval = (maxValue - minValue);
            displayScale = canvas.getHeight() / interval;
        }

        double height = canvas.getHeight();

        drawCoordinateSystem(minValue, maxValue, interval);

        graphics.setStroke(Color.MEDIUMSEAGREEN);

        for (int i = 1; i < values.size(); ++i) {
            double previousValue = convertYToScreenSpace(values.get(i - 1), height);
            double currentValue = convertYToScreenSpace(values.get(i), height);
            graphics.strokeLine(i - 1, previousValue, i, currentValue);
        }

        CurveDrawRequest drawRequest = CurveDrawRequest.builder()
                .withCurrentProvider(currentKeyframeableEffect)
                .withCanvas(canvas)
                .withCurrentKeyframeableEffect(currentInterpolator)
                .withCurveViewerOffsetSeconds(scrollValue)
                .withDisplayScale(displayScale)
                .withMaxValue(maxValue)
                .withMinValue(minValue)
                .withSecondsPerPixel(secondsPerPixel)
                .withGraphics(graphics)
                .withHeight(canvas.getHeight())
                .withTimeOffset(positionOffset)
                .build();

        currentlyOpenEditor.drawAdditionalUi(drawRequest);

        graphics.setStroke(Color.YELLOW);
        double playheadPosition = ((timelineManager.getCurrentPosition().getSeconds().doubleValue()) * (1.0 / secondsPerPixel)) - scrollValue;
        graphics.strokeLine(playheadPosition, 0, playheadPosition, canvas.getHeight());
    }

    private double convertYToScreenSpace(double value, double height) {
        return height - ((value - minValue) * displayScale);
    }

    private void drawCoordinateSystem(double minValue, double maxValue, double interval) {
        // 0.123
        double distance = interval / 10;

        GraphicsContext graphics = canvas.getGraphicsContext2D();
        graphics.setStroke(Color.gray(0.2));
        graphics.setFont(new Font(8));

        for (double i = minValue; i <= maxValue; i += distance) {
            double screenY = convertYToScreenSpace(i, canvas.getHeight());
            graphics.strokeLine(0, screenY, canvas.getWidth(), screenY);

            String value = String.format("%.2f", i);

            graphics.strokeText(value, 5, screenY - 3);
        }

        int secondIncrement = (int) (canvas.getWidth() * secondsPerPixel) / 10;

        int firstLine = (int) scrollValue;

        if (secondIncrement > 0) {
            int second = firstLine;
            for (int i = 0; i < 100; ++i) {
                double screenX = (second - scrollValue) * (1.0 / secondsPerPixel);
                graphics.strokeLine(screenX, 0, screenX, canvas.getHeight());

                graphics.strokeText(second + "", screenX, canvas.getHeight() - 10);

                second += secondIncrement;

                if (screenX > canvas.getWidth()) {
                    break;
                }
            }
        }

    }

    @Override
    public void postProcess(Scene scene) {
        BorderPane borderPane = new BorderPane();
        canvas = new Canvas(400, 250);
        canvas.widthProperty().bind(borderPane.widthProperty());

        canvas.setOnMouseDragged(e -> {
            if (e.isMiddleButtonDown()) {
                if (!isLeftRightDragging) {
                    isLeftRightDragging = true;
                    lastDraggedX = e.getX();
                } else {
                    double delta = e.getX() - lastDraggedX;
                    this.lastDraggedX = e.getX();
                    scrollValue -= delta * secondsPerPixel;
                    if (scrollValue < 0) {
                        scrollValue = 0;
                    }
                    updateCanvas();
                }
            } else {
                isLeftRightDragging = false;
                sendToCurveEditorAndRedrawIfRequired(e, request -> currentlyOpenEditor.onMouseDragged(request));
            }
        });
        canvas.setOnMouseMoved(e -> sendToCurveEditorAndRedrawIfRequired(e, request -> currentlyOpenEditor.onMouseMoved(request)));
        canvas.setOnMouseClicked(e -> sendToCurveEditorAndRedrawIfRequired(e, request -> currentlyOpenEditor.onMouseClicked(request)));
        canvas.setOnMousePressed(e -> sendToCurveEditorAndRedrawIfRequired(e, request -> currentlyOpenEditor.onMouseDown(request)));
        canvas.setOnMouseReleased(e -> sendToCurveEditorAndRedrawIfRequired(e, request -> currentlyOpenEditor.onMouseUp(request)));
        canvas.setOnMouseDragExited(e -> sendToCurveEditorAndRedrawIfRequired(e, request -> currentlyOpenEditor.onMouseDragEnded(request)));

        canvas.setOnScroll(scrollEvent -> {
            double scrollAmount = scrollEvent.getDeltaY();
            secondsPerPixel -= (scrollAmount * 0.0001);
            if (secondsPerPixel < 1 / 1000.) {
                secondsPerPixel = 1 / 1000.0;
            }
            if (secondsPerPixel > 100) {
                secondsPerPixel = 100;
            }
            updateCanvas();
        });

        controlPane = new GridPane();
        controlPane.getChildren().add(new Label("Use show in curve editor in interpolated elements, to see curve here"));

        borderPane.setTop(controlPane);
        borderPane.setCenter(canvas);
        this.setText("Curve editor");

        this.setContent(borderPane);
    }

    private void sendToCurveEditorAndRedrawIfRequired(MouseEvent e, Function<CurveEditorMouseRequest, Boolean> consumer) {
        isLeftRightDragging = false;
        Point mouseDelta = new Point(0, 0);
        Point currentMousePosition = new Point(e.getX(), e.getY());
        if (lastMousePosition != null) {
            mouseDelta = currentMousePosition.subtract(lastMousePosition);
            mouseDelta.x *= secondsPerPixel;
            mouseDelta.y /= displayScale;
            mouseDelta.y *= -1;
        }
        lastMousePosition = currentMousePosition;
        isDragging = e.getEventType().equals(MouseEvent.MOUSE_DRAGGED);

        double remappedX = (currentMousePosition.x - scrollValue) * secondsPerPixel;
        double mouseY = currentMousePosition.y;
        if (mouseY < -20) {
            mouseY = -20;
        }
        if (mouseY > canvas.getHeight() + 20) {
            mouseY = canvas.getHeight() + 20;
        }
        double remappedY = minValue + ((1.0 - (mouseY / canvas.getHeight())) * (maxValue - minValue));

        if (absoluteMaxValue.isPresent() && remappedY > absoluteMaxValue.get()) {
            remappedY = absoluteMaxValue.get();
        }
        if (absoluteMinValue.isPresent() && remappedY < absoluteMinValue.get()) {
            remappedY = absoluteMinValue.get();
        }

        Point remappedMousePosition = new Point(remappedX, remappedY);

        if (currentlyOpenEditor != null) {
            CurveEditorMouseRequest request = CurveEditorMouseRequest.builder()
                    .withCurrentProvider(currentKeyframeableEffect)
                    .withCurrentKeyframeableEffect(currentInterpolator)
                    .withEvent(e)
                    .withMouseDelta(mouseDelta)
                    .withRemappedMousePosition(remappedMousePosition)
                    .withCurveViewerOffsetSeconds(scrollValue)
                    .withDisplayScale(displayScale)
                    .withMaxValue(maxValue)
                    .withMinValue(minValue)
                    .withSecondsPerPixel(secondsPerPixel)
                    .withScreenMousePosition(currentMousePosition)
                    .withHeight(canvas.getHeight())
                    .withCanvas(canvas)
                    .withTimeOffset(positionOffset)
                    .build();
            boolean shouldUpdate = consumer.apply(request);
            if (shouldUpdate) {
                Platform.runLater(() -> updateCanvas());
            }
        }
    }

    @Override
    public void tabClosed() {
        currentlyOpenEditor = null;
        clearCanvas();
    }

}
