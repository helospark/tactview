package com.helospark.tactview.core.timeline.proceduralclip.polygon.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Color;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Polygon;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;

@Component
public class PolygonRenderService {

    public ReadOnlyClipImage drawPolygon(PolygonRenderServiceRequest request) {
        Polygon polygon = request.getPolygon();
        Color color = request.getColor();
        int fuzzyEdge = request.getFuzzyEdge();
        ClipImage result = ClipImage.fromSize(request.getExpectedWidth(), request.getExpectedHeight());
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
                for (int pixelX = currentValue; pixelX <= nextValue; pixelX++) {
                    result.setRed((int) (color.red * 255.0), pixelX, pixelY);
                    result.setGreen((int) (color.green * 255.0), pixelX, pixelY);
                    result.setBlue((int) (color.blue * 255.0), pixelX, pixelY);

                    int minDistance = Math.min(pixelX - currentValue, nextValue - pixelX);
                    int alpha = (int) ((double) minDistance / fuzzyEdge * 255);
                    if (alpha > 255) {
                        alpha = 255;
                    }

                    result.setAlpha(alpha, pixelX, pixelY);
                }
            }
        }

        if (fuzzyEdge > 0) {
            // below code is almost the same as the above one, except all x and y are reversed and only the alpha is modifed
            for (int pixelX = 0; pixelX < request.getExpectedWidth(); pixelX++) {
                List<Integer> nodeY = new ArrayList<>();
                int polyCorners = polygon.getPoints().size();
                int j = polyCorners - 1;
                for (int i = 0; i < polyCorners; i++) {
                    Point polyI = polygon.getPoints().get(i).multiply(expectedSizePoint);
                    Point polyJ = polygon.getPoints().get(j).multiply(expectedSizePoint);

                    if (polyI.x < pixelX && polyJ.x >= pixelX || polyJ.x < pixelX && polyI.x >= pixelX) {
                        nodeY.add((int) (polyI.y + (pixelX - polyI.x) / (polyJ.x - polyI.x) * (polyJ.y - polyI.y)));
                    }
                    j = i;
                }
                Collections.sort(nodeY);

                int IMAGE_BOTTOM = request.getExpectedHeight();
                for (int i = 0; i < nodeY.size(); i += 2) {
                    int currentValue = nodeY.get(i);
                    if (currentValue >= IMAGE_BOTTOM)
                        break;
                    int nextValue;
                    if (i + 1 < nodeY.size()) {
                        nextValue = nodeY.get(i + 1);
                    } else {
                        // maybe this cannot happen?
                        nextValue = IMAGE_BOTTOM;
                    }
                    for (int pixelY = currentValue; pixelY <= nextValue; pixelY++) {
                        int minDistance = Math.min(pixelY - currentValue, nextValue - pixelY);
                        double verticalAlpha = ((double) minDistance / fuzzyEdge);
                        if (verticalAlpha > 1.0) {
                            verticalAlpha = 1.0;
                        }
                        double horizontalAlpha = result.getAlpha(pixelX, pixelY) / 255.0;

                        double newAlpha = Math.min(horizontalAlpha, verticalAlpha);

                        result.setAlpha((int) (newAlpha * 255.0), pixelX, pixelY);
                    }
                }
            }
        }
        return result;
    }

}
