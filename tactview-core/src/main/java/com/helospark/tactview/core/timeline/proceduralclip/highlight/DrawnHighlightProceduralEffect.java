package com.helospark.tactview.core.timeline.proceduralclip.highlight;

import com.helospark.tactview.core.decoder.VisualMediaMetadata;
import com.helospark.tactview.core.decoder.framecache.GlobalMemoryManagerAccessor;
import com.helospark.tactview.core.timeline.ClipFrameResult;
import com.helospark.tactview.core.timeline.GetFrameRequest;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.blendmode.impl.NormalBlendModeStrategy;
import com.helospark.tactview.core.timeline.effect.scale.service.ScaleRequest;
import com.helospark.tactview.core.timeline.effect.scale.service.ScaleService;
import com.helospark.tactview.core.timeline.framemerge.AlphaBlitService;
import com.helospark.tactview.core.timeline.proceduralclip.ProceduralVisualClip;
import com.helospark.tactview.core.util.ReflectionUtil;
import com.helospark.tactview.core.util.brush.GimpBrushHeader;
import com.helospark.tactview.core.util.brush.GimpBrushLoader;

public class DrawnHighlightProceduralEffect extends ProceduralVisualClip {
    private AlphaBlitService alphaBlitService;
    private NormalBlendModeStrategy normalBlendModeStrategy;
    private ScaleService scaleService;

    private GimpBrushHeader brush;

    public DrawnHighlightProceduralEffect(VisualMediaMetadata visualMediaMetadata, TimelineInterval interval, GimpBrushLoader gimpBrushLoader, AlphaBlitService alphaBlitService,
            NormalBlendModeStrategy normalBlendModeStrategy, ScaleService scaleService) {
        super(visualMediaMetadata, interval);
        brush = gimpBrushLoader.loadBrush("/usr/share/gimp/2.0/brushes/Media/Oils-02.gbr");
        this.alphaBlitService = alphaBlitService;
        this.normalBlendModeStrategy = normalBlendModeStrategy;
        this.scaleService = scaleService;

    }

    public DrawnHighlightProceduralEffect(DrawnHighlightProceduralEffect cloneFrom) {
        super(cloneFrom);
        ReflectionUtil.copyOrCloneFieldFromTo(cloneFrom, this);
    }

    @Override
    public ClipFrameResult createProceduralFrame(GetFrameRequest request, TimelinePosition relativePosition) {
        ClipFrameResult result = ClipFrameResult.fromSize(request.getExpectedWidth(), request.getExpectedHeight());
        ClipFrameResult scaledBrush = createScaledBrush(request);

        //        int spacing = brush.spacing / (brush.width / 40);
        //        if (spacing <= 0) {
        int spacing = 1;
        //        }
        double progress = relativePosition.getSeconds().doubleValue() / 5.0;
        progress = progress - (int) progress;
        for (int i = 20; i < (request.getExpectedWidth() - 40) * progress; i += spacing) {
            alphaBlitService.alphaBlitImageIntoResult(result, scaledBrush, i, request.getExpectedHeight() / 2, normalBlendModeStrategy, 1.0);
        }

        GlobalMemoryManagerAccessor.memoryManager.returnBuffer(scaledBrush.getBuffer());

        return result;
    }

    private ClipFrameResult createScaledBrush(GetFrameRequest request) {
        ClipFrameResult brushImage = createBrushImage(brush);

        ScaleRequest scaleRequest = ScaleRequest.builder()
                .withImage(brushImage)
                .withNewWidth(20)
                .withNewHeight(20)
                .withPadImage(false)
                .build();

        ClipFrameResult scaledBrush = scaleService.createScaledImage(scaleRequest);

        GlobalMemoryManagerAccessor.memoryManager.returnBuffer(brushImage.getBuffer());

        return scaledBrush;
    }

    private ClipFrameResult createBrushImage(GimpBrushHeader brush) {
        ClipFrameResult resultBrush = ClipFrameResult.fromSize(brush.width, brush.height);

        for (int y = 0; y < brush.height; ++y) {
            for (int x = 0; x < brush.width; ++x) {
                resultBrush.setAlpha(ClipFrameResult.signedToUnsignedByte(brush.data[y * brush.width + x]), x, y);
            }
        }

        return resultBrush;
    }

    @Override
    public TimelineClip cloneClip() {
        return new DrawnHighlightProceduralEffect(this);
    }

}
