package com.helospark.tactview.core.timeline;

import java.nio.ByteBuffer;
import java.util.List;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.decoder.framecache.GlobalMemoryManagerAccessor;
import com.helospark.tactview.core.timeline.TimelineManager.RenderFrameData;
import com.helospark.tactview.core.timeline.blendmode.BlendModeStrategy;
import com.helospark.tactview.core.timeline.effect.transition.ExternalStatelessVideoTransitionEffectRequest;

@Component
public class FrameBufferMerger {
    private EmptyByteBufferFactory emptyByteBufferFactory;

    public FrameBufferMerger(EmptyByteBufferFactory emptyByteBufferFactory) {
        this.emptyByteBufferFactory = emptyByteBufferFactory;
    }

    public ClipFrameResult alphaMergeFrames(List<RenderFrameData> frames, TimelineManagerFramesRequest request) {
        int width = request.getPreviewWidth();
        int height = request.getPreviewHeight();
        if (frames.size() > 0) {
            ClipFrameResult output = new ClipFrameResult(GlobalMemoryManagerAccessor.memoryManager.requestBuffer(width * height * 4), width, height);

            for (int i = frames.size() - 1; i >= 0; --i) {
                if (frames.get(i).videoTransition.isPresent()) {
                    ExternalStatelessVideoTransitionEffectRequest transitionRequest = ExternalStatelessVideoTransitionEffectRequest.builder()
                            .withGlobalPosition(request.getPosition())
                            .withFirstFrame(frames.get(i).clipFrameResult)
                            .withSecondFrame(output)
                            .withScale(request.getScale())
                            .build();
                    ClipFrameResult transitionedImage = frames.get(i).videoTransition.get().applyTransition(transitionRequest);
                    GlobalMemoryManagerAccessor.memoryManager.returnBuffer(output.getBuffer());
                    output = transitionedImage;
                } else {
                    alphaBlitFrame(output, frames.get(i).clipFrameResult, width, height, frames.get(i).blendModeStrategy, frames.get(i).globalAlpha);
                }
            }

            return output;
        } else {
            ByteBuffer emptyBuffer = emptyByteBufferFactory.createEmptyByteImage(width, height);
            return new ClipFrameResult(emptyBuffer, width, height);
        }
    }

    private void alphaBlitFrame(ClipFrameResult result, ClipFrameResult clipFrameResult, Integer width, Integer height, BlendModeStrategy blendMode, double globalAlpha) {
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

}
