package com.helospark.tactview.core.timeline.proceduralclip.gradient.service;

import org.apache.commons.math3.geometry.euclidean.twod.Line;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import com.helospark.lightdi.annotation.Service;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Color;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.InterpolationLine;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.util.IndependentPixelOperation;

@Service
public class LinearGradientService {
    private IndependentPixelOperation independentPixelOperation;

    public LinearGradientService(IndependentPixelOperation independentPixelOperation) {
        this.independentPixelOperation = independentPixelOperation;
    }

    public ClipImage render(LinearGradientRequest request) {
        int width = request.getWidth();
        int height = request.getHeight();

        ClipImage result = ClipImage.fromSize(width, height);

        InterpolationLine line = request.getNormalizedLine();
        Color startColor = request.getStartColor();
        Color endColor = request.getEndColor();

        Point startPositionInPixels = line.start.multiply(result.getWidth(), result.getHeight());
        Point endPositionInPixels = line.end.multiply(result.getWidth(), result.getHeight());

        Vector2D start = new Vector2D(startPositionInPixels.x, startPositionInPixels.y);
        Vector2D originalEnd = new Vector2D(endPositionInPixels.x, endPositionInPixels.y);

        double lineDistance = start.distance(originalEnd);
        Line perpendicularLine = getPerpendicularLine(start, originalEnd);

        if (!request.getSaturateOnEndSide()) {
            independentPixelOperation.executePixelTransformation(result.getWidth(), result.getHeight(), (x, y) -> {
                double pixelDistance = perpendicularLine.distance(new Vector2D(x, y)); // TODO: avoid new on every pixel

                if (pixelDistance > lineDistance) {
                    setColor(result, x, y, endColor);
                } else {
                    double factor = pixelDistance / lineDistance;
                    Color newColor = startColor.interpolate(endColor, factor);
                    setColor(result, x, y, newColor);
                }

            });
        } else {
            independentPixelOperation.executePixelTransformation(result.getWidth(), result.getHeight(), (x, y) -> {
                double pixelOffset = perpendicularLine.getOffset(new Vector2D(x, y)); // TODO: avoid new on every pixel
                double pixelDistance = Math.abs(pixelOffset);

                if (pixelOffset < 0.0) {
                    setColor(result, x, y, startColor);
                } else if (Math.abs(pixelDistance) > lineDistance) {
                    setColor(result, x, y, endColor);
                } else {
                    double factor = pixelDistance / lineDistance;
                    Color newColor = startColor.interpolate(endColor, factor);
                    setColor(result, x, y, newColor);
                }

            });
        }

        return result;
    }

    private Line getPerpendicularLine(Vector2D start, Vector2D originalEnd) {
        Vector2D direction = originalEnd.subtract(start);
        Vector2D perpendicularDirection = new Vector2D(direction.getY(), -direction.getX()).normalize();
        Vector2D end = start.add(perpendicularDirection);

        Line perpendicularLine = new Line(start, end, 0.0001);
        return perpendicularLine;
    }

    private void setColor(ClipImage result, Integer x, Integer y, Color endColor) {
        result.setRed((int) (endColor.red * 255), x, y);
        result.setGreen((int) (endColor.green * 255), x, y);
        result.setBlue((int) (endColor.blue * 255), x, y);
        result.setAlpha(255, x, y);
    }
}
