package com.helospark.tactview.ui.javafx.inputmode.strategy;

import com.helospark.tactview.core.timeline.effect.interpolation.pojo.InterpolationLine;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;

import javafx.scene.canvas.GraphicsContext;

public class LineInputTypeStrategy implements InputTypeStrategy<InterpolationLine> {
    private InterpolationLine result = new InterpolationLine(new Point(0, 0), new Point(0, 0));
    private boolean hasEndPosition;
    private boolean hasStartPosition;

    @Override
    public void onMouseDownEvent(StrategyInput input) {
        double x = input.x;
        double y = input.y;
        if (!hasStartPosition) {
            result.start = new Point(x, y);
            result.end = new Point(x, y);
            hasStartPosition = true;
        }
    }

    @Override
    public void onMouseDraggedEvent(StrategyInput input) {
        double x = input.x;
        double y = input.y;
        result.end = new Point(x, y);
    }

    @Override
    public void onMouseUpEvent(StrategyInput input) {
        double x = input.x;
        double y = input.y;
        result.end = new Point(x, y);
        hasEndPosition = true;
    }

    @Override
    public ResultType getResultType() {
        if (hasStartPosition && hasEndPosition) {
            return ResultType.DONE;
        } else if (hasStartPosition) {
            return ResultType.PARTIAL;
        } else {
            return ResultType.NONE;
        }
    }

    @Override
    public void draw(GraphicsContext canvas) {

    }

    @Override
    public InterpolationLine getResult() {
        return result;
    }

}
