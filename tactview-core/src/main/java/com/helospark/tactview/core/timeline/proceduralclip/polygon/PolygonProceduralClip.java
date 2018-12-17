package com.helospark.tactview.core.timeline.proceduralclip.polygon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.decoder.ImageMetadata;
import com.helospark.tactview.core.decoder.VisualMediaMetadata;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.GetFrameRequest;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Polygon;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.PolygonProvider;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.timeline.proceduralclip.ProceduralVisualClip;

public class PolygonProceduralClip extends ProceduralVisualClip {
    private PolygonProvider polygonProvider;

    public PolygonProceduralClip(VisualMediaMetadata visualMediaMetadata, TimelineInterval interval) {
        super(visualMediaMetadata, interval);
    }

    public PolygonProceduralClip(ImageMetadata metadata, JsonNode node, LoadMetadata loadMetadata) {
        super(metadata, node, loadMetadata);
    }

    @Override
    public ReadOnlyClipImage createProceduralFrame(GetFrameRequest request, TimelinePosition relativePosition) {
        ClipImage result = ClipImage.fromSize(request.getExpectedWidth(), request.getExpectedHeight());

        Polygon polygon = polygonProvider.getValueAt(relativePosition);

        //  Loop through the rows of the image.
        Point expectedSizePoint = new Point(request.getExpectedWidth(), request.getExpectedHeight());
        for (int pixelY = 0; pixelY < request.getExpectedHeight(); pixelY++) {

            //  Build a list of nodes.
            List<Integer> nodeX = new ArrayList<>();
            int polyCorners = polygon.getPoints().size();
            int j = polyCorners - 1;
            for (int i = 0; i < polyCorners; i++) {
                Point polyI = polygon.getPoints().get(i).multiply(expectedSizePoint);
                Point polyJ = polygon.getPoints().get(j).multiply(expectedSizePoint);

                if (polyI.y < pixelY && polyJ.y >= pixelY || polyJ.y < pixelY && polyI.y >= pixelY) {
                    nodeX.add((int) (polyI.x + (pixelY - polyI.y) / (polyJ.y - polyI.y) * (polyJ.x - polyI.x)));
                }
                j = i;
            }

            Collections.sort(nodeX);

            //  Fill the pixels between node pairs.
            int IMAGE_RIGHT = request.getExpectedWidth();

            for (int i = 0; i < nodeX.size(); i += 2) {
                int currentValue = nodeX.get(i);
                if (currentValue >= IMAGE_RIGHT)
                    break;
                int nextValue;
                if (i + 1 < nodeX.size()) {
                    nextValue = nodeX.get(i + 1);
                } else {
                    // maybe this cannot happen?
                    nextValue = IMAGE_RIGHT;
                }
                for (int pixelX = currentValue; pixelX < nextValue; pixelX++) {
                    result.setRed(255, pixelX, pixelY);
                    result.setAlpha(255, pixelX, pixelY);
                }
            }
        }

        return result;
    }

    @Override
    protected void initializeValueProvider() {
        super.initializeValueProvider();
        this.polygonProvider = new PolygonProvider(List.of());
    }

    @Override
    public List<ValueProviderDescriptor> getDescriptorsInternal() {
        List<ValueProviderDescriptor> result = super.getDescriptorsInternal();

        ValueProviderDescriptor polygonProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(polygonProvider)
                .withName("polygon")
                .build();

        result.add(polygonProviderDescriptor);

        return result;
    }

    @Override
    public TimelineClip cloneClip() {
        // TODO Auto-generated method stub
        return null;
    }

}
