package com.helospark.tactview.ui.javafx.uicomponents;

import com.helospark.tactview.ui.javafx.UiTimelineManager;

import javafx.beans.property.DoubleProperty;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;

// https://stackoverflow.com/questions/39827911/javafx-8-scaling-zooming-scrollpane-relative-to-mouse-position
public class ZoomableScrollPane extends ScrollPane {
    private TimelineState timelineState;
    private double scaleValue = 1.0;
    private double zoomIntensity = 0.02;
    private Node target;
    private Node zoomNode;

    public ZoomableScrollPane(Node target, TimelineState timelineState, UiTimelineManager uiTimelineManager) {
        this.timelineState = timelineState;
        this.target = target;
        this.zoomNode = new Group(target);
        setContent(outerNode(zoomNode));

        setPannable(false);
        setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        setFitToHeight(true); //center
        setFitToWidth(true); //center

        updateScale();

        addEventFilter(KeyEvent.ANY, e -> {
            if (e.getCode().equals(KeyCode.LEFT)) {
                uiTimelineManager.moveBackOneFrame();
                e.consume();
            }
            if (e.getCode().equals(KeyCode.RIGHT)) {
                uiTimelineManager.moveForwardOneFrame();
                e.consume();
            }
        });
    }

    private Node outerNode(Node node) {
        Node outerNode = vboxNode(node);
        outerNode.setOnScroll(e -> {
            if (e.isControlDown()) {
                e.consume();
                onScroll(e.getDeltaY(), new Point2D(e.getX(), e.getY()));
            }
        });
        return outerNode;
    }

    private Node vboxNode(Node node) {
        VBox vBox = new VBox(node);
        vBox.setAlignment(Pos.TOP_LEFT);
        return vBox;
    }

    private void updateScale() {
        target.setScaleX(scaleValue);
        //        target.setScaleY(scaleValue);
    }

    private void onScroll(double wheelDelta, Point2D mousePoint) {
        double zoomFactor = Math.exp((wheelDelta * 0.05) * zoomIntensity);

        Bounds innerBounds = zoomNode.getLayoutBounds();
        Bounds viewportBounds = getViewportBounds();

        // calculate pixel offsets from [0, 1] range
        double valX = this.getHvalue() * (innerBounds.getWidth() - viewportBounds.getWidth());
        //        double valY = this.getVvalue() * (innerBounds.getHeight() - viewportBounds.getHeight());

        scaleValue = scaleValue * zoomFactor;
        if (scaleValue < TimelineState.MIN_ZOOM)
            scaleValue = TimelineState.MIN_ZOOM;
        if (scaleValue > TimelineState.MAX_ZOOM)
            scaleValue = TimelineState.MAX_ZOOM;
        updateScale();
        this.layout(); // refresh ScrollPane scroll positions & target bounds

        // convert target coordinates to zoomTarget coordinates
        Point2D posInZoomTarget = target.parentToLocal(zoomNode.parentToLocal(mousePoint));

        // calculate adjustment of scroll position (pixels)
        Point2D adjustment = target.getLocalToParentTransform().deltaTransform(posInZoomTarget.multiply(zoomFactor - 1));

        // convert back to [0, 1] range
        // (too large/small values are automatically corrected by ScrollPane)
        Bounds updatedInnerBounds = zoomNode.getBoundsInLocal();
        double hValue = (valX + adjustment.getX()) / (updatedInnerBounds.getWidth() - viewportBounds.getWidth());
        this.setHvalue(hValue);
        timelineState.setZoom(scaleValue);
        //        this.setVvalue((valY + adjustment.getY()) / (updatedInnerBounds.getHeight() - viewportBounds.getHeight()));
    }

    public DoubleProperty zoomProperty() {
        return target.scaleXProperty();
    }

    public void reset() {
        scaleValue = 1.0;
        updateScale();
    }
}