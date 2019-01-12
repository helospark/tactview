package com.helospark.tactview.core.timeline.proceduralclip.lines.impl;

import java.util.List;
import java.util.Optional;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.blendmode.impl.NormalBlendModeStrategy;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Color;
import com.helospark.tactview.core.timeline.framemerge.AlphaBlitService;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.util.brush.GetBrushRequest;
import com.helospark.tactview.core.util.brush.ScaledBrushProvider;

@Component
public class DrawLineService {
    private AlphaBlitService alphaBlitService;
    private NormalBlendModeStrategy normalBlendModeStrategy;
    private ScaledBrushProvider scaledBrushProvider;

    public DrawLineService(AlphaBlitService alphaBlitService, NormalBlendModeStrategy normalBlendModeStrategy, ScaledBrushProvider scaledBrushProvider) {
        this.alphaBlitService = alphaBlitService;
        this.normalBlendModeStrategy = normalBlendModeStrategy;
        this.scaledBrushProvider = scaledBrushProvider;
    }

    public void drawLine(DrawLineRequest drawLineRequest) {
        double progress = drawLineRequest.getProgress();
        List<Vector2D> pixels = drawLineRequest.getPixels();

        Optional<ClipImage> brushImage = getBrush(drawLineRequest.getBrushFilePath(), drawLineRequest.getBrushSize(), drawLineRequest.getColor());

        ClipImage result = drawLineRequest.getResult();

        if (brushImage.isPresent()) {
            int spacing = 1;
            for (int i = 0; i < pixels.size() * progress; i += spacing) {
                int x = (int) pixels.get(i).getX();
                int y = (int) pixels.get(i).getY();
                alphaBlitService.alphaBlitImageIntoResult(result, brushImage.get(), x, y, normalBlendModeStrategy, 1.0);
            }
        }
    }

    private Optional<ClipImage> getBrush(String brushFilePath, int brushSize, Color color) {
        try {
            GetBrushRequest brushRequest = GetBrushRequest.builder()
                    .withFilename(brushFilePath)
                    .withWidth(brushSize)
                    .withHeight(brushSize)
                    .withColor(color)
                    .build();

            return Optional.of(scaledBrushProvider.getBrushImage(brushRequest));
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }
}
