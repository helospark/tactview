package com.helospark.tactview.core.timeline.proceduralclip.polygon.impl.bezier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.helospark.lightdi.annotation.Service;
import com.helospark.tactview.core.decoder.framecache.GlobalMemoryManagerAccessor;
import com.helospark.tactview.core.timeline.effect.blur.BlurRequest;
import com.helospark.tactview.core.timeline.effect.blur.BlurService;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Color;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;

// http://alienryderflex.com/polyspline/
@Service
public class BezierPolygonRenderServiceImpl implements BezierPolygonRenderService {
    private BlurService blurService;

    public BezierPolygonRenderServiceImpl(BlurService blurService) {
        this.blurService = blurService;
    }

    public ReadOnlyClipImage drawBezierPolygon(BezierPolygonRenderServiceRequest request) {
        BezierPolygon polygon = request.getPolygon();
        Color color = request.getColor();
        int fuzzyEdge = request.getFuzzyEdge();
        ClipImage result = ClipImage.fromSize(request.getExpectedWidth(), request.getExpectedHeight());
        //  Loop through the rows of the image.
        Point expectedSizePoint = new Point(request.getExpectedWidth(), request.getExpectedHeight());
        List<BezierPolygonPoint> poly = polygon.points;
        //                //150, 100, 150, 50, 50, 50, 50, 100, SPLINE, 100, 180, END
        //                200, 100, SPLINE, 220, 75, 200, 50, SPLINE, 125, 20, 50, 50, SPLINE, 50, 75, 50, 100, 100, 200, END
        //                //                    150, 100, 150, 50, 50, 50, 50, 100, 100, 150, END
        //        };

        for (double y = 0.00001; y < result.getHeight(); ++y) {

            //  Build a list of nodes.
            List<Double> nodeX = new ArrayList<>();

            double Sx, Sy, Ex, Ey, a, b, sRoot, F, plusOrMinus, topPart, bottomPart, xPart;
            int i = 0, j, k, start = 0;

            //            int END = -10000;
            //            int SPLINE = -10001;
            //            int NEW_LOOP = -10002;

            while (i < poly.size()) {

                j = i + 1;
                if (j >= poly.size()) {
                    j = start;
                }
                //                if (poly[j] == END || poly[j] == NEW_LOOP)
                //                    j = start;

                if (poly.get(i).type != SplinePolygonType.SPLINE && poly.get(j).type != SplinePolygonType.SPLINE) { //  STRAIGHT LINE
                    if (poly.get(i).y < y && poly.get(j).y >= y || poly.get(j).y < y && poly.get(i).y >= y) {
                        nodeX.add(poly.get(i).x + (y - poly.get(i).y) / (poly.get(j).y - poly.get(i).y) * (poly.get(j).x - poly.get(i).x));
                    }
                }

                else if (poly.get(j).type == SplinePolygonType.SPLINE) { //  SPLINE CURVE
                    a = poly.get(j).x;
                    b = poly.get(j).y;
                    k = j + 1;
                    if (k >= poly.size())
                        k = start;
                    if (poly.get(i).type != SplinePolygonType.SPLINE) {
                        Sx = poly.get(i).x;
                        Sy = poly.get(i).y;
                    } else { //  interpolate hard corner
                        Sx = (poly.get(i).x + poly.get(j).x) / 2.;
                        Sy = (poly.get(i).y + poly.get(j).y) / 2.;
                    }
                    if (poly.get(k).type != SplinePolygonType.SPLINE) {
                        Ex = poly.get(k).x;
                        Ey = poly.get(k).y;
                    } else { //  interpolate hard corner
                        Ex = (poly.get(j).x + poly.get(k).x) / 2.;
                        Ey = (poly.get(j).y + poly.get(k).y) / 2.;
                    }
                    bottomPart = 2. * (Sy + Ey - b - b);
                    if (bottomPart == 0.) { //  prevent division-by-zero
                        b += .0001;
                        bottomPart = -.0004;
                    }
                    sRoot = 2. * (b - Sy);
                    sRoot *= sRoot;
                    sRoot -= 2. * bottomPart * (Sy - y);
                    if (sRoot >= 0.) {
                        sRoot = Math.sqrt(sRoot);
                        topPart = 2. * (Sy - b);
                        for (plusOrMinus = -1.; plusOrMinus < 1.1; plusOrMinus += 2.) {
                            F = (topPart + plusOrMinus * sRoot) / bottomPart;
                            if (F >= 0. && F <= 1.) {
                                xPart = Sx + F * (a - Sx);
                                nodeX.add(xPart + F * (a + F * (Ex - a) - xPart));
                            }
                        }
                    }
                }
                i++;
                //                if (poly[i] == NEW_LOOP) {
                //                    i++;
                //                    start = i;
                //                }

            }
            Collections.sort(nodeX);

            //  Fill the pixels between node pairs.
            int IMAGE_RIGHT = request.getExpectedWidth();

            for (int in = 0; in < nodeX.size(); in += 2) {
                int currentValue = (int) Math.round(nodeX.get(in).doubleValue());
                if (currentValue >= IMAGE_RIGHT)
                    break;
                int nextValue;
                if (in + 1 < nodeX.size()) {
                    nextValue = (int) Math.round(nodeX.get(in + 1).doubleValue());
                } else {
                    // maybe this cannot happen?
                    nextValue = IMAGE_RIGHT;
                }
                for (int pixelX = currentValue; pixelX <= nextValue; pixelX++) {
                    if (result.inBounds(pixelX, (int) y)) {
                        result.setRed((int) (color.red * 255.0), pixelX, (int) y);
                        result.setGreen((int) (color.green * 255.0), pixelX, (int) y);
                        result.setBlue((int) (color.blue * 255.0), pixelX, (int) y);
                        result.setAlpha(255, pixelX, (int) y);
                    }
                }
            }
        }

        if (fuzzyEdge > 0) {
            BlurRequest blurRequest = BlurRequest.builder()
                    .withImage(result)
                    .withKernelWidth(fuzzyEdge)
                    .withKernelHeight(fuzzyEdge)
                    .build();

            ClipImage blurredResult = blurService.createBlurredImage(blurRequest);
            GlobalMemoryManagerAccessor.memoryManager.returnBuffer(result.getBuffer());
            return blurredResult;
        } else {
            return result;
        }
    }

}
