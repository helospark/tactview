package com.helospark.tactview.core.timeline.framemerge;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.ClipFrameResult;
import com.helospark.tactview.core.timeline.blendmode.BlendModeStrategy;

@Component
public class AlphaBlitService {

    public void alphaBlitFrame(ClipFrameResult result, ClipFrameResult clipFrameResult, Integer width, Integer height, BlendModeStrategy blendMode, double globalAlpha) {
        int[] forground = new int[4];
        int[] blendedForground = new int[4];
        int[] background = new int[4];
        int[] resultPixel = new int[4];
        for (int i = 0; i < height; ++i) {
            for (int j = 0; j < width; ++j) {
                result.getPixelComponents(background, j, i);
                clipFrameResult.getPixelComponents(forground, j, i);

                blendMode.computeColor(forground, background, blendedForground);

                double alpha = (blendedForground[3] * globalAlpha) / 255.0;
                double backgroundAlpha = background[3] / 255.0; // maybe the previous layer's alpha needs to be taken into account?
                resultPixel[0] = (int) ((blendedForground[0] * alpha) + (background[0] * (1.0 - alpha)));
                resultPixel[1] = (int) ((blendedForground[1] * alpha) + (background[1] * (1.0 - alpha)));
                resultPixel[2] = (int) ((blendedForground[2] * alpha) + (background[2] * (1.0 - alpha)));
                resultPixel[3] = (int) (background[3] + (1.0 - backgroundAlpha) * blendedForground[3]);

                result.setPixel(resultPixel, j, i);
            }
        }
    }

    // TODO: avoid duplication
    public void alphaBlitImageIntoResult(ClipFrameResult result, ClipFrameResult toBlit, int blitToX, int blitToY, BlendModeStrategy blendMode, double globalAlpha) {
        int[] forground = new int[4];
        int[] blendedForground = new int[4];
        int[] background = new int[4];
        int[] resultPixel = new int[4];
        for (int y = 0; y < toBlit.getHeight(); ++y) {
            for (int x = 0; x < toBlit.getWidth(); ++x) {
                int resultX = blitToX + x;
                int resultY = blitToY + y;

                if (resultX >= result.getWidth() || resultY >= result.getHeight()) {
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

}
