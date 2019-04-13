package com.helospark.tactview.ui.javafx.tabs.curve;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.KeyframeableEffect;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.EffectInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.KeyframeSupportingDoubleInterpolator;
import com.helospark.tactview.core.timeline.message.KeyframeSuccesfullyAddedMessage;
import com.helospark.tactview.core.util.messaging.MessagingService;
import com.helospark.tactview.ui.javafx.scenepostprocessor.ScenePostProcessor;
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

@Component
public class CurveEditorTab extends Tab implements ScenePostProcessor {
    private static final double samplesPerPixel = 1.0;
    private List<CurveEditor> curveEditors;
    private MessagingService messagingService;

    private double secondsPerPixel = 1 / 20.0;
    private double scrollValue = 0.0;

    private Canvas canvas;
    private GridPane controlPane;

    private KeyframeableEffect currentKeyframeableEffect;
    private KeyframeSupportingDoubleInterpolator currentInterpolator;
    private CurveEditor currentlyOpenEditor;

    /// dragging
    private boolean isLeftRightDragging = false;
    private double lastDraggedX = 0;
    ///

    public CurveEditorTab(List<CurveEditor> curveEditors, MessagingService messagingService) {
        this.curveEditors = curveEditors;

        this.messagingService = messagingService;
        messagingService.register(KeyframeSuccesfullyAddedMessage.class, e -> {
            if (e.getDescriptorId().equals(currentKeyframeableEffect.getId())) {
                Platform.runLater(() -> updateCanvas());
            }
        });

    }

    public void revealInEditor(KeyframeableEffect valueProvider) {
        EffectInterpolator effectInterpolator = valueProvider.getInterpolator();
        if (effectInterpolator instanceof KeyframeSupportingDoubleInterpolator) {
            this.currentInterpolator = (KeyframeSupportingDoubleInterpolator) effectInterpolator;
            this.currentKeyframeableEffect = valueProvider;
            currentlyOpenEditor = curveEditors.stream()
                    .filter(editor -> editor.supports(currentInterpolator))
                    .findFirst()
                    .orElseThrow();
            controlPane.getChildren().clear();

            // reset defaults
            secondsPerPixel = 1 / 20.0;
            scrollValue = 0.0;
            isLeftRightDragging = false;

            updateCanvas();
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
        clearCanvas();
        GraphicsContext graphics = canvas.getGraphicsContext2D();
        graphics.setStroke(Color.MEDIUMSEAGREEN);
        double numberOfSamples = samplesPerPixel * canvas.getWidth();
        BigDecimal increment = new BigDecimal(secondsPerPixel);

        System.out.println("Increment=" + increment);

        List<Double> values = new ArrayList<>();
        TimelinePosition current = new TimelinePosition(scrollValue);
        for (int i = 0; i < numberOfSamples; ++i) {
            values.add(currentInterpolator.valueAt(current));
            current = current.add(increment);
        }
        double maxValue = Collections.max(values);
        double minValue = Collections.min(values);
        double interval = (maxValue - minValue);
        if (interval <= 0.01) {
            interval = 0.01;
            minValue -= 0.005;
        }
        double displayScale = canvas.getHeight() / interval;

        for (int i = 1; i < values.size(); ++i) {
            double previousValue = (values.get(i - 1) - minValue) * displayScale;
            double currentValue = (values.get(i) - minValue) * displayScale;
            graphics.strokeLine(i - 1, previousValue, i, currentValue);
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
                    System.out.println("Delta: " + delta);
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
        if (currentlyOpenEditor != null) {
            CurveEditorMouseRequest request = new CurveEditorMouseRequest(e, currentInterpolator);
            boolean shouldUpdate = consumer.apply(request);
            if (shouldUpdate) {
                Platform.runLater(() -> updateCanvas());
            }
        }
    }

}
