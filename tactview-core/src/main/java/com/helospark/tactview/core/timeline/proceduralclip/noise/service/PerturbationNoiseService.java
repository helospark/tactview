package com.helospark.tactview.core.timeline.proceduralclip.noise.service;

import com.helospark.lightdi.annotation.Service;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.util.IndependentPixelOperation;

import me.jordanpeck.FastNoise;
import me.jordanpeck.Vector3f;

@Service
public class PerturbationNoiseService {
    private IndependentPixelOperation independentPixelOperation;

    public PerturbationNoiseService(IndependentPixelOperation independentPixelOperation) {
        this.independentPixelOperation = independentPixelOperation;
    }

    public ClipImage createPerturbation(PerturbationRequestParameter parameterObject) {
        Integer seed = getSeed(parameterObject);
        FastNoise fastNoise = new FastNoise(seed);
        fastNoise.SetFrequency(parameterObject.getFrequency());
        fastNoise.SetGradientPerturbAmp(parameterObject.getGradientPerturb());

        ClipImage result = ClipImage.fromSize(parameterObject.getWidth(), parameterObject.getHeight());
        Point startPosition = getStartPosition(parameterObject);
        double colorScale = parameterObject.getColorScale();

        independentPixelOperation.executePixelTransformation(result.getWidth(), result.getHeight(), (x, y) -> {
            float xf = (float) ((((double) x) / result.getWidth() - startPosition.x) * 50.0);
            float yf = (float) ((((double) y) / result.getHeight() - startPosition.y) * 50.0);
            float zf = parameterObject.getzPos();

            Vector3f inputVector = new Vector3f(xf, yf, zf);

            if (parameterObject.isFractal()) {
                fastNoise.GradientPerturbFractal(inputVector);
            } else {
                fastNoise.GradientPerturb(inputVector);
            }

            xf -= inputVector.x;
            yf -= inputVector.y;
            zf -= inputVector.z;

            result.setColorComponentByOffset((int) (xf * colorScale), x, y, 0);
            result.setColorComponentByOffset((int) (yf * colorScale), x, y, 1);
            result.setColorComponentByOffset((int) (zf * colorScale), x, y, 2);
            result.setColorComponentByOffset(255, x, y, 3);
        });
        return result;
    }

    private Integer getSeed(PerturbationRequestParameter parameterObject) {
        Integer seed = parameterObject.getSeed();
        if (seed == null) {
            seed = 0;
        }
        return seed;
    }

    private Point getStartPosition(PerturbationRequestParameter parameterObject) {
        Point startPosition = parameterObject.getStartPoint();
        if (startPosition == null) {
            startPosition = new Point(0, 0);
        }
        return startPosition;
    }
}
