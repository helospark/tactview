package com.helospark.tactview.core.timeline.framemerge;

import com.helospark.tactview.core.timeline.blendmode.BlendModeStrategy;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;

public interface AlphaBlitService {

    void alphaBlitFrame(ClipImage result, ReadOnlyClipImage clipFrameResult, Integer width, Integer height, BlendModeStrategy blendMode, double globalAlpha);

    void alphaBlitImageIntoResult(ClipImage result, ReadOnlyClipImage toBlit, int blitToX, int blitToY, BlendModeStrategy blendMode, double globalAlpha);

    void alphaBlitImageIntoResultWithoutPremultiply(ClipImage result, ReadOnlyClipImage toBlit, int blitToX, int blitToY, BlendModeStrategy blendMode, double globalAlpha);

}