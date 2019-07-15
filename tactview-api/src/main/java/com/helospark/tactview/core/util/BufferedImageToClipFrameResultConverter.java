package com.helospark.tactview.core.util;

import java.awt.image.BufferedImage;

import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;

public interface BufferedImageToClipFrameResultConverter {

    ClipImage convertFromAbgr(BufferedImage bufferedImage);

    ReadOnlyClipImage convertFromIntArgb(BufferedImage bufferedImage);

    ReadOnlyClipImage convert(BufferedImage bufferedImage);

}