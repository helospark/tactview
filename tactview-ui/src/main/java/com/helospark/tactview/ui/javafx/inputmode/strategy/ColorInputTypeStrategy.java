package com.helospark.tactview.ui.javafx.inputmode.strategy;

import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Color;

import javafx.scene.canvas.GraphicsContext;

public class ColorInputTypeStrategy implements InputTypeStrategy<Color> {
    private Color result;
    private boolean done = false;

    @Override
    public void onMouseUpEvent(StrategyMouseInput input) {
        double x = input.unscaledX;
        double y = input.unscaledY;

        javafx.scene.paint.Color javafxColor = input.canvasImage.get().getPixelReader().getColor((int) x, (int) y);

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
    public void draw(GraphicsContext canvas, int width, int height) {

    }

    @Override
    public Color getResult() {
        return result;
    }

}
