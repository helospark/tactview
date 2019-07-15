package com.helospark.tactview.core.timeline.effect.colorize;

import java.awt.Color;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.util.IndependentPixelOperationImpl;

@Component
public class ColorizeService {
    private IndependentPixelOperationImpl independentPixelOperation;

    public ColorizeService(IndependentPixelOperationImpl independentPixelOperation) {
        this.independentPixelOperation = independentPixelOperation;
    }

    public ClipImage colorize(ReadOnlyClipImage currentFrame, ColorizeRequest request) {

        ClipImage result = ClipImage.sameSizeAs(currentFrame);

        applyColorizeToFrame(currentFrame, result, request);

        return result;
    }

    public void applyColorizeToFrame(ReadOnlyClipImage currentFrame, ClipImage result, ColorizeRequest request) {
        double hueChange = request.getHueChange();
        double saturationChange = request.getSaturationChange();
        double valueChange = request.getValueChange();

        independentPixelOperation.executePixelTransformation(currentFrame.getWidth(), currentFrame.getHeight(), (x, y) -> {
            int originalR = currentFrame.getRed(x, y);
            int originalG = currentFrame.getGreen(x, y);
            int originalB = currentFrame.getBlue(x, y);

            float[] floatArray = Color.RGBtoHSB(originalR, originalG, originalB, null);

            floatArray[0] = saturateBetweenInclusive(0.0f, 1.0f, (float) (floatArray[0] + hueChange));
            floatArray[1] = saturateBetweenInclusive(0.0f, 1.0f, (float) (floatArray[1] + saturationChange));
            floatArray[2] = saturateBetweenInclusive(0.0f, 1.0f, (float) (floatArray[2] + valueChange));

            int pixel = Color.HSBtoRGB(floatArray[0], floatArray[1], floatArray[2]);

            int newR = (pixel >> 16) & 0xFF;
            int newG = (pixel >> 8) & 0xFF;
            int newB = (pixel >> 0) & 0xFF;
            int alpha = currentFrame.getAlpha(x, y);

            result.setColorComponentByOffset(newR, x, y, 0);
            result.setColorComponentByOffset(newG, x, y, 1);
            result.setColorComponentByOffset(newB, x, y, 2);
            result.setColorComponentByOffset(alpha, x, y, 3);
        });
    }

    private float saturateBetweenInclusive(float low, float high, float value) {
        if (value < low) {
            return low;
        }
        if (value > high) {
            return high;
        }
        return value;
    }
}
