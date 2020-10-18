package com.helospark.tactview.ui.javafx.control;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Color;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.SkinBase;
import javafx.scene.input.MouseEvent;

public class ColorWheelPickerSkin extends SkinBase<ColorWheelPicker> {
    private static final int DEFAULT_SIZE = 150;

    private static final int SELECTION_MARKER_DIAMETER = 6;

    private ObjectProperty<javafx.scene.paint.Color> colorProperty;
    private ObjectProperty<javafx.scene.paint.Color> onActionProvider;
    private BooleanProperty onColorChangingProvider;

    private Canvas canvas;
    private int radius = 1;
    private int selectedX, selectedY;
    private int currentSize;

    protected ColorWheelPickerSkin(ColorWheelPicker control) {
        super(control);
        colorProperty = control.colorProperty();
        onActionProvider = control.onActionProperty();
        onColorChangingProvider = control.onValueChangingProperty();
        radius = DEFAULT_SIZE / 2;

        setSelectionPosition(toColor(colorProperty.get()));
        createColorWheel(DEFAULT_SIZE);

        colorProperty.addListener((change, oldValue, newValue) -> {
            setSelectionPosition(toColor(oldValue));
            updateCanvasPart(radius, canvas, selectedX - radius, selectedY - radius);
            setSelectionPosition(toColor(newValue));
            drawMarker(canvas.getGraphicsContext2D());
        });
    }

    private Color toColor(javafx.scene.paint.Color oldValue) {
        return new Color(oldValue.getRed(), oldValue.getGreen(), oldValue.getBlue());
    }

    private void createColorWheel(int size) {
        radius = size / 2;

        System.out.println("New color wheel");

        canvas = new Canvas(size, size);

        canvas.setOnMouseDragged(e -> {
            System.out.println("Dragged");
            onColorChangingProvider.setValue(true);
            handleMouseEvent(e);
        });

        canvas.setOnMouseReleased(e -> {
            System.out.println("Mouse release");
            onColorChangingProvider.setValue(false);
            handleMouseEvent(e);
        });

        updateCanvas(radius, canvas);

        getChildren().setAll(canvas);

        currentSize = size;
    }

    private void handleMouseEvent(MouseEvent e) {
        double x = e.getX() - radius;
        double y = e.getY() - radius;
        double distance = Math.sqrt(y * y + x * x);

        if (distance < radius) {
            Color color = calculateColor((int) x, (int) y, radius, distance);

            javafx.scene.paint.Color newColor = new javafx.scene.paint.Color(color.red, color.green, color.blue, 1.0);
            colorProperty.set(newColor);
            onActionProvider.set(newColor);
        }
    }

    private void updateCanvasPart(int radius, Canvas canvas, int previousSelectionX, int previousSelectionY) {
        GraphicsContext graphics = canvas.getGraphicsContext2D();
        for (int y = previousSelectionY - SELECTION_MARKER_DIAMETER; y <= previousSelectionY + SELECTION_MARKER_DIAMETER; ++y) {
            for (int x = previousSelectionX - SELECTION_MARKER_DIAMETER; x <= previousSelectionX + SELECTION_MARKER_DIAMETER; ++x) {
                fillPixel(radius, graphics, y, x);
            }
        }
    }

    private void setSelectionPosition(Color color) {
        Color selectedColor = color.rgbToHsbColor();
        double selectedDistance = selectedColor.blue;
        double angle = selectedColor.red * 2 * Math.PI - Math.PI;
        selectedX = (int) (selectedDistance * radius * Math.cos(angle)) + radius;
        selectedY = (int) (selectedDistance * radius * Math.sin(angle)) + radius;
    }

    private void updateCanvas(int radius, Canvas canvas) {
        GraphicsContext graphics = canvas.getGraphicsContext2D();
        for (int y = -radius; y < radius; ++y) {
            for (int x = -radius; x < radius; ++x) {
                fillPixel(radius, graphics, y, x);
            }
        }
        drawMarker(graphics);
    }

    private void fillPixel(int radius, GraphicsContext graphics, int y, int x) {
        double distance = Math.sqrt(y * y + x * x);
        javafx.scene.paint.Color fillColor;
        if (distance < radius) {
            Color rgbColor = calculateColor(x, y, radius, distance);
            fillColor = new javafx.scene.paint.Color(rgbColor.red, rgbColor.green, rgbColor.blue, 1.0);
        } else {
            fillColor = new javafx.scene.paint.Color(0, 0, 0, 0);
        }
        graphics.setFill(fillColor);
        graphics.fillRect(x + radius, y + radius, 1, 1);
    }

    private void drawMarker(GraphicsContext graphics) {
        int drawCenterX = selectedX - SELECTION_MARKER_DIAMETER / 2;
        int drawCenterY = selectedY - SELECTION_MARKER_DIAMETER / 2;
        graphics.setFill(colorProperty.get());
        graphics.fillOval(drawCenterX, drawCenterY, SELECTION_MARKER_DIAMETER, SELECTION_MARKER_DIAMETER);
        graphics.setStroke(javafx.scene.paint.Color.BLACK);
        graphics.strokeOval(drawCenterX, drawCenterY, SELECTION_MARKER_DIAMETER, SELECTION_MARKER_DIAMETER);
    }

    private Color calculateColor(int x, int y, int radius, double distance) {
        double theta = Math.atan2(y, x);
        double saturation = distance / radius;
        Color hsvColor = new Color((theta + Math.PI) / (2 * Math.PI), 1.0, saturation);
        Color rgbColor = hsvColor.hsbToRgbColor();
        return rgbColor;
    }

    @Override
    protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight) {
        int size = (int) Math.min(contentWidth, contentHeight);
        if (currentSize != size) {
            createColorWheel(size);
        }
    }

}
