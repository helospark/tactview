package com.helospark.tactview.core.util;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;

public interface IndependentPixelOperation {

    ClipImage createNewImageWithAppliedTransformation(ReadOnlyClipImage currentFrame, SimplePixelTransformer pixelTransformer);

    ClipImage createNewImageWithAppliedTransformation(ReadOnlyClipImage currentFrame, List<ThreadLocalProvider<?>> threadLocalProviders, SimplePixelTransformer pixelTransformer);

    void executePixelTransformation(int width, int height, BiConsumer<Integer, Integer> consumer);

    void executePixelTransformation(int width, int height, List<ThreadLocalProvider<?>> threadLocalProviders, Consumer<PixelUpdateRequest> consumer);

}