package com.helospark.tactview.core.timeline.effect.colorize;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.util.IndependentPixelOperation;

@Component
public class ColorTemperatureService {
    private IndependentPixelOperation independentPixelOperation;

    public ColorTemperatureService(IndependentPixelOperation independentPixelOperation) {
        this.independentPixelOperation = independentPixelOperation;
    }

    public ClipImage createNewImageWithAppliedTemperatureChange(ReadOnlyClipImage clipImage, ColorTemperatureChangeRequest request) {
        ClipImage result = ClipImage.sameSizeAs(clipImage);

        result.copyFrom(clipImage);

        applyColorTemperatureChange(result, request);

        return result;
    }

    public void applyColorTemperatureChange(ClipImage clipImage, ColorTemperatureChangeRequest request) {
        independentPixelOperation.executePixelTransformation(clipImage.getWidth(), clipImage.getHeight(), (x, y) -> {
            int red = (int) (clipImage.getColorComponentWithOffset(x, y, 0) + request.temperatureChange * 255.0);
            int green = (int) (clipImage.getColorComponentWithOffset(x, y, 1) + request.tintChange * 255.0);
            int blue = (int) (clipImage.getColorComponentWithOffset(x, y, 2) - request.temperatureChange * 255.0);
            int alpha = clipImage.getColorComponentWithOffset(x, y, 3);

            clipImage.setColorComponentByOffset(red, x, y, 0);
            clipImage.setColorComponentByOffset(green, x, y, 1);
            clipImage.setColorComponentByOffset(blue, x, y, 2);
            clipImage.setColorComponentByOffset(alpha, x, y, 3);
        });
    }

}
