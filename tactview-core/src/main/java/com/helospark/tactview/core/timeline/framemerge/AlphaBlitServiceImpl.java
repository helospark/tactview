package com.helospark.tactview.core.timeline.framemerge;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.blendmode.BlendModeStrategy;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.util.IndependentPixelOperation;

@Component
public class AlphaBlitServiceImpl implements AlphaBlitService {
    private IndependentPixelOperation independentPixelOperation;

    public AlphaBlitServiceImpl(IndependentPixelOperation independentPixelOperation) {
        this.independentPixelOperation = independentPixelOperation;
    }

    // Expensive method, during preview & render uses 30% of all CPU
    // TODO: optimize this. Also create a HW accelerated version.
    // Consider native JNA implementation as well
    @Override
    public void alphaBlitFrame(ClipImage result, ReadOnlyClipImage clipFrameResult, Integer width, Integer height, BlendModeStrategy blendMode, double globalAlpha) {

        independentPixelOperation.executePixelTransformation(width, height, (x, y) -> {
            int[] forground = new int[4];
            int[] blendedForground = new int[4];
            int[] background = new int[4];

            result.getPixelComponents(background, x, y);
            clipFrameResult.getPixelComponents(forground, x, y);

            forground[3] = (int) (forground[3] * globalAlpha);

            blendMode.computeColor(forground, background, blendedForground);

            if (blendedForground[3] != 255) {
                double backgroundAlpha = background[3] / 255.0;
                double foregroundAlpha = blendedForground[3] / 255.0;
                blendedForground[3] = (int) ((foregroundAlpha + backgroundAlpha * (1.0 - foregroundAlpha)) * 255.0);
            }
            result.setPixel(blendedForground, x, y);
        });

    }

    // TODO: avoid duplication
    @Override
    public void alphaBlitImageIntoResult(ClipImage result, ReadOnlyClipImage toBlit, int blitToX, int blitToY, BlendModeStrategy blendMode, double globalAlpha) {
        int[] forground = new int[4];
        int[] blendedForground = new int[4];
        int[] background = new int[4];
        int[] resultPixel = new int[4];
        for (int y = 0; y < toBlit.getHeight(); ++y) {
            for (int x = 0; x < toBlit.getWidth(); ++x) {
                int resultX = blitToX + x;
                int resultY = blitToY + y;

                if (resultX >= result.getWidth() || resultY >= result.getHeight() || resultX < 0 || resultY < 0) {
                    continue;
                }

                result.getPixelComponents(background, resultX, resultY);
                toBlit.getPixelComponents(forground, x, y);

                blendMode.computeColor(forground, background, blendedForground);

                double alpha = (blendedForground[3] * globalAlpha) / 255.0;
                double backgroundAlpha = background[3] / 255.0; // maybe the previous layer's alpha needs to be taken into account?
                resultPixel[0] = (int) ((blendedForground[0] * alpha) + (background[0] * (1.0 - alpha)));
                resultPixel[1] = (int) ((blendedForground[1] * alpha) + (background[1] * (1.0 - alpha)));
                resultPixel[2] = (int) ((blendedForground[2] * alpha) + (background[2] * (1.0 - alpha)));
                resultPixel[3] = (int) (background[3] + (1.0 - backgroundAlpha) * blendedForground[3]);

                result.setPixel(resultPixel, resultX, resultY);
            }
        }
    }

    // TODO: avoid duplication
    @Override
    public void alphaBlitImageIntoResultWithoutPremultiply(ClipImage result, ReadOnlyClipImage toBlit, int blitToX, int blitToY, BlendModeStrategy blendMode, double globalAlpha) {
        int[] forground = new int[4];
        int[] blendedForground = new int[4];
        int[] background = new int[4];
        int[] resultPixel = new int[4];
        for (int y = 0; y < toBlit.getHeight(); ++y) {
            for (int x = 0; x < toBlit.getWidth(); ++x) {
                int resultX = blitToX + x;
                int resultY = blitToY + y;

                if (resultX >= result.getWidth() || resultY >= result.getHeight() || resultX < 0 || resultY < 0) {
                    continue;
                }

                result.getPixelComponents(background, resultX, resultY);
                toBlit.getPixelComponents(forground, x, y);

                blendMode.computeColor(forground, background, blendedForground);

                double backgroundAlpha = background[3] / 255.0; // maybe the previous layer's alpha needs to be taken into account?
                resultPixel[0] = (blendedForground[0]);
                resultPixel[1] = (blendedForground[1]);
                resultPixel[2] = (blendedForground[2]);
                resultPixel[3] = (int) (background[3] + (1.0 - backgroundAlpha) * blendedForground[3]);

                result.setPixel(resultPixel, resultX, resultY);
            }
        }
    }

}
