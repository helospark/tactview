package com.helospark.tactview.core.util;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.ClipFrameResult;

@Component
public class IndependentPixelOperation {

    public ClipFrameResult createNewImageWithAppliedTransformation(ClipFrameResult currentFrame, SimplePixelTransformer pixelTransformer) {
        return createNewImageWithAppliedTransformation(currentFrame, List.of(), pixelTransformer);
    }

    public ClipFrameResult createNewImageWithAppliedTransformation(ClipFrameResult currentFrame, List<ThreadLocalProvider<?>> threadLocalProviders, SimplePixelTransformer pixelTransformer) {
        ClipFrameResult resultFrame = ClipFrameResult.sameSizeAs(currentFrame);
        int[] pixelComponents = new int[4];
        int[] resultPixelComponents = new int[4];

        Map<ThreadLocalProvider<?>, Object> threadLocals = threadLocalProviders.stream()
                .collect(Collectors.toMap(a -> a, a -> a.get()));

        SimplePixelTransformerRequest request = SimplePixelTransformerRequest.builder()
                .withx(0)
                .withy(0)
                .withInput(pixelComponents)
                .withOutput(resultPixelComponents)
                .withThreadLocals(threadLocals)
                .build();

        // TODO: do it in parallel
        for (int y = 0; y < currentFrame.getHeight(); y++) {
            for (int x = 0; x < currentFrame.getWidth(); x++) {
                currentFrame.getPixelComponents(pixelComponents, x, y);

                request.x = x;
                request.y = y;
                request.output = resultPixelComponents;
                request.input = pixelComponents;

                pixelTransformer.transform(request);

                resultFrame.setPixel(request.output, x, y);
            }
        }
        return resultFrame;
    }

    public void executePixelTransformation(int width, int height, BiConsumer<Integer, Integer> consumer) {
        // TODO: do it in parallel
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                consumer.accept(x, y);
            }
        }
    }

}
