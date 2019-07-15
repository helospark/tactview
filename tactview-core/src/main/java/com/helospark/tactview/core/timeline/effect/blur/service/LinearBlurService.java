package com.helospark.tactview.core.timeline.effect.blur.service;

import com.helospark.lightdi.annotation.Service;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.util.IndependentPixelOperation;

@Service
public class LinearBlurService {
    private IndependentPixelOperation independentPixelOperation;

    public LinearBlurService(IndependentPixelOperation independentPixelOperation) {
        this.independentPixelOperation = independentPixelOperation;
    }

    public ClipImage linearBlur(LinearBlurRequest request) {
        double length = request.direction.length();
        Point normalizedDirection = request.direction.end.subtract(request.direction.start).normalize();

        if (length <= 0.0001) {
            ClipImage result = ClipImage.sameSizeAs(request.input);
            result.copyFrom(request.input);
            return result;
        }

        return independentPixelOperation.createNewImageWithAppliedTransformation(request.input, pixelRequest -> {
            int sumR = 0;
            int sumG = 0;
            int sumB = 0;
            int sumA = 0;
            int count = 0;
            for (int i = 0; i < length; ++i) {
                double positionX = pixelRequest.x + i * normalizedDirection.x;
                double positionY = pixelRequest.y + i * normalizedDirection.y;

                sumR += request.input.getColorComponentWithOffsetUsingInterpolation(positionX, positionY, 0);
                sumG += request.input.getColorComponentWithOffsetUsingInterpolation(positionX, positionY, 1);
                sumB += request.input.getColorComponentWithOffsetUsingInterpolation(positionX, positionY, 2);
                sumA += request.input.getColorComponentWithOffsetUsingInterpolation(positionX, positionY, 3);
                ++count;
            }
            pixelRequest.output[0] = sumR / count;
            pixelRequest.output[1] = sumG / count;
            pixelRequest.output[2] = sumB / count;
            pixelRequest.output[3] = sumA / count;
        });
    }

}
