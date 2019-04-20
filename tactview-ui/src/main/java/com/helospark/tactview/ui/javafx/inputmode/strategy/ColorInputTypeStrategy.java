package com.helospark.tactview.ui.javafx.inputmode.strategy;

import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Color;
import com.helospark.tactview.ui.javafx.key.CurrentlyPressedKeyRepository;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;

public class ColorInputTypeStrategy implements InputTypeStrategy<Color> {
    private static final int MULTISAMPLING_SIZE = 4;
    private Color result;
    private boolean done = false;

    private CurrentlyPressedKeyRepository currentlyPressedKeyRepository;

    public ColorInputTypeStrategy(CurrentlyPressedKeyRepository currentlyPressedKeyRepository) {
        this.currentlyPressedKeyRepository = currentlyPressedKeyRepository;
    }

    @Override
    public void onMouseUpEvent(StrategyMouseInput input) {
        double x = input.unscaledX;
        double y = input.unscaledY;

        Image canvasImage = input.canvasImage.get();
        PixelReader pixelReader = canvasImage.getPixelReader();
        javafx.scene.paint.Color javafxColor;
        if (isMultiPixelSamplingRequested()) {
            double r = 0;
            double g = 0;
            double b = 0;
            int count = 0;
            for (int yc = -MULTISAMPLING_SIZE; yc <= MULTISAMPLING_SIZE; ++yc) {
                for (int xc = -MULTISAMPLING_SIZE; xc <= MULTISAMPLING_SIZE; ++xc) {
                    int newX = (int) (x + xc);
                    int newY = (int) (y + yc);
                    int width = (int) canvasImage.getWidth();
                    int height = (int) canvasImage.getHeight();
                    if (newX >= 0 && newX < width && newY >= 0 && newY < height) {
                        javafx.scene.paint.Color readColor = pixelReader.getColor(newX, newY);
                        ++count;
                        r += readColor.getRed();
                        g += readColor.getGreen();
                        b += readColor.getBlue();
                    }
                }
            }
            javafxColor = new javafx.scene.paint.Color(r / count, g / count, b / count, 1.0);
        } else {
            javafxColor = pixelReader.getColor((int) x, (int) y);
        }

        result = new Color(javafxColor.getRed(), javafxColor.getGreen(), javafxColor.getBlue());

        done = true;
    }

    @Override
    public ResultType getResultType() {
        if (done) {
            return ResultType.DONE;
        } else {
            return ResultType.NONE;
        }
    }

    @Override
    public void draw(DrawRequestParameter parameterObject) {
        if (parameterObject.isMouseInCanvas()) {
            GraphicsContext graphics = parameterObject.getCanvas();
            MouseEvent causeEvent = parameterObject.getMouseEvent().get();

            if (isMultiPixelSamplingRequested()) {
                graphics.setStroke(javafx.scene.paint.Color.RED);
                graphics.strokeRect(causeEvent.getX() - MULTISAMPLING_SIZE, causeEvent.getY() - MULTISAMPLING_SIZE, 2 * MULTISAMPLING_SIZE + 1, 2 * MULTISAMPLING_SIZE + 1);
            }
        }
    }

    private boolean isMultiPixelSamplingRequested() {
        return currentlyPressedKeyRepository.isKeyDown(KeyCode.CONTROL);
    }

    @Override
    public Color getResult() {
        return result;
    }

}
