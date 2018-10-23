package com.helospark.tactview.ui.javafx.inputmode.strategy;

import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Line;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;

public class LineInputTypeStrategy implements InputTypeStrategy<Line> {
    private Line result = new Line(new Point(0, 0), new Point(0, 0));
    private boolean hasEndPosition;
    private boolean hasStartPosition;

    @Override
    public void onMouseDownEvent(double x, double y, MouseEvent mouseEvent) {
        if (!hasStartPosition) {
            result.start = new Point(x, y);
            result.end = new Point(x, y);
            hasStartPosition = true;
        }
    }

    @Override
    public void onMouseDraggedEvent(double x, double y, MouseEvent e) {
        result.end = new Point(x, y);
    }

    @Override
    public void onMouseUpEvent(double x, double y, MouseEvent mouseEvent) {
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
    public Line getResult() {
        return result;
    }

}
