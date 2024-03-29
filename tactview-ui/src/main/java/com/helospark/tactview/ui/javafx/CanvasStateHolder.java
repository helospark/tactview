package com.helospark.tactview.ui.javafx;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.markers.ResettableBean;
import com.helospark.tactview.core.util.messaging.MessagingService;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.canvas.Canvas;

@Component
public class CanvasStateHolder implements ResettableBean {
    private Canvas canvas;
    private DoubleProperty translateX = new SimpleDoubleProperty(0.0);
    private DoubleProperty translateY = new SimpleDoubleProperty(0.0);

    public CanvasStateHolder(MessagingService messagingService) {
        translateX.addListener((a, b, c) -> {
            messagingService.sendMessage(new DisplayUpdateRequestMessage(false));
        });
        translateY.addListener((a, b, c) -> {
            messagingService.sendMessage(new DisplayUpdateRequestMessage(false));
        });
    }

    public void setCanvas(Canvas canvas) {
        this.canvas = canvas;
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public double getTranslateX() {
        return translateX.doubleValue() * -1.0;
    }

    public double getTranslateY() {
        return translateY.doubleValue() * -1.0;
    }

    public void increaseTranslateX(double x) {
        translateX.set(translateX.get() + x * -1.0);
    }

    public void increaseTranslateY(double y) {
        translateY.set(translateY.get() + y * -1.0);
    }

    @Override
    public void resetDefaults() {
        translateX.set(0.0);
        translateY.set(0.0);
    }

    public void setTranslateX(double x) {
        this.translateX.set(x * -1.0);
    }

    public void setTranslateY(double y) {
        this.translateY.set(y * -1.0);
    }

    public DoubleProperty getTranslateXProperty() {
        return translateX;
    }

    public DoubleProperty getTranslateYProperty() {
        return translateY;
    }

    public double getAvailableWidth() {
        double canvasWidth = this.canvas.getWidth();
        if (canvasWidth <= 0.0) {
            return 320;
        }
        return canvasWidth;
    }

    public double getAvailableHeight() {
        double canvasHeight = this.canvas.getHeight();
        if (canvasHeight <= 0.0) {
            return 240;
        }
        return canvasHeight;
    }

    public DoubleProperty getPreviewAvailableWidthProperty() {
        return this.canvas.widthProperty();
    }

}
