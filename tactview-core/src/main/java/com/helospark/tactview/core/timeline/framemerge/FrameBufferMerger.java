package com.helospark.tactview.core.timeline.framemerge;

import java.nio.ByteBuffer;
import java.util.List;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.decoder.framecache.GlobalMemoryManagerAccessor;
import com.helospark.tactview.core.timeline.EmptyByteBufferFactory;
import com.helospark.tactview.core.timeline.TimelineManagerFramesRequest;
import com.helospark.tactview.core.timeline.effect.transition.ExternalStatelessVideoTransitionEffectRequest;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;

@Component
public class FrameBufferMerger {
    private EmptyByteBufferFactory emptyByteBufferFactory;
    private AlphaBlitService alphaBlitService;

    public FrameBufferMerger(EmptyByteBufferFactory emptyByteBufferFactory, AlphaBlitService alphaBlitService) {
        this.emptyByteBufferFactory = emptyByteBufferFactory;
        this.alphaBlitService = alphaBlitService;
    }

    public ReadOnlyClipImage alphaMergeFrames(List<RenderFrameData> frames, TimelineManagerFramesRequest request) {
        int width = request.getPreviewWidth();
        int height = request.getPreviewHeight();

        if (frames.size() > 0) {
            ClipImage output = new ClipImage(GlobalMemoryManagerAccessor.memoryManager.requestBuffer(width * height * 4), width, height);

            for (int i = frames.size() - 1; i >= 0; --i) {
                if (frames.get(i).videoTransition.isPresent() && frames.get(i).videoTransition.get().isEnabledAt(request.getPosition())) { // TODO: global position?
                    ExternalStatelessVideoTransitionEffectRequest transitionRequest = ExternalStatelessVideoTransitionEffectRequest.builder()
                            .withGlobalPosition(request.getPosition())
                            .withFirstFrame(frames.get(i).clipFrameResult)
                            .withSecondFrame(output)
                            .withScale(request.getScale())
                            .build();
                    ClipImage transitionedImage = frames.get(i).videoTransition.get().applyTransition(transitionRequest);
                    GlobalMemoryManagerAccessor.memoryManager.returnBuffer(output.getBuffer());
                    output = transitionedImage;
                } else {
                    alphaBlitService.alphaBlitFrame(output, frames.get(i).clipFrameResult, width, height, frames.get(i).blendModeStrategy, frames.get(i).globalAlpha);
                }
            }

            return output;
        } else {
            ByteBuffer emptyBuffer = emptyByteBufferFactory.createEmptyByteImage(width, height);
            return new ClipImage(emptyBuffer, width, height);
        }
    }

}
