package com.helospark.tactview.core.timeline.effect.contractbrightness;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.util.IndependentPixelOperationImpl;

@Component
public class BrignessContrastService {
    private IndependentPixelOperationImpl independentPixelOperation;

    public BrignessContrastService(IndependentPixelOperationImpl independentPixelOperation) {
        this.independentPixelOperation = independentPixelOperation;
    }

    public ClipImage createImageWithBrighnessContrastChange(ReadOnlyClipImage currentFrame, BrignessContrastServiceRequest request) {
        ClipImage result = ClipImage.sameSizeAs(currentFrame);
        applyBrightnessContrastChangeToImage(currentFrame, result, request);
        return result;
    }

    public void applyBrightnessContrastChangeToImage(ReadOnlyClipImage currentFrame, ClipImage result, BrignessContrastServiceRequest request) {
        if (!currentFrame.isSameSizeAs(result)) {
            throw new IllegalArgumentException("Only same size images are supported");
        }

        double contrast = request.getContrast();
        double brightness = request.getBrightness();

        independentPixelOperation.executePixelTransformation(currentFrame.getWidth(), currentFrame.getHeight(), (x, y) -> {
            int red = (int) ((contrast * (currentFrame.getColorComponentWithOffset(x, y, 0) / 255.0 - 0.5) + 0.5 + brightness) * 255.0);
            int green = (int) ((contrast * (currentFrame.getColorComponentWithOffset(x, y, 1) / 255.0 - 0.5) + 0.5 + brightness) * 255.0);
            int blue = (int) ((contrast * (currentFrame.getColorComponentWithOffset(x, y, 2) / 255.0 - 0.5) + 0.5 + brightness) * 255.0);
            int alpha = currentFrame.getColorComponentWithOffset(x, y, 3);

            result.setColorComponentByOffset(red, x, y, 0);
            result.setColorComponentByOffset(green, x, y, 1);
            result.setColorComponentByOffset(blue, x, y, 2);
            result.setColorComponentByOffset(alpha, x, y, 3);
        });
    }

}
