package com.helospark.tactview.core.timeline.proceduralclip.gradient.service;

import com.helospark.lightdi.annotation.Service;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Color;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.util.IndependentPixelOperationImpl;

@Service
public class RadialGradientService {
    private IndependentPixelOperationImpl independentPixelOperation;

    public RadialGradientService(IndependentPixelOperationImpl independentPixelOperation) {
        this.independentPixelOperation = independentPixelOperation;
    }

    public ClipImage createImageWithGradient(RadialGradientRequest request) {
        Point center = request.getCenter();
        double radius = request.getRadius();
        Color startColor = request.getStartColor();
        Color endColor = request.getEndColor();
        double innerSaturation = request.getInnerSaturation();

        ClipImage result = ClipImage.fromSize(request.getWidth(), request.getHeight());

        independentPixelOperation.executePixelTransformation(request.getWidth(), request.getHeight(), (x, y) -> {
            double distance = center.distanceFrom(x, y);
            if (distance > radius) {
                setColor(result, x, y, endColor);
            } else {
                double factor = (distance / radius);
                if (factor <= innerSaturation) {
                    setColor(result, x, y, startColor);
                } else {
                    double realDistanceNormalized = 1.0 - innerSaturation;
                    factor = (factor - innerSaturation) / realDistanceNormalized;
                    Color newColor = startColor.interpolate(endColor, factor);
                    setColor(result, x, y, newColor);
                }
            }
        });

        return result;
    }

    private void setColor(ClipImage result, Integer x, Integer y, Color endColor) {
        result.setRed((int) (endColor.red * 255), x, y);
        result.setGreen((int) (endColor.green * 255), x, y);
        result.setBlue((int) (endColor.blue * 255), x, y);
        result.setAlpha(255, x, y);
    }
}
