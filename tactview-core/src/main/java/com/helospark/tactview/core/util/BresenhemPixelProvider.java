package com.helospark.tactview.core.util;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.util.cacheable.Cacheable;

@Component
public class BresenhemPixelProvider {

    @Cacheable
    public List<Vector2D> ellipsePixels(int xc, int yc, int width, int height) {
        List<Vector2D> points = new ArrayList<>();
        List<Vector2D> bottomFirstQuarter = new LinkedList<>();
        List<Vector2D> bottomSecondQuarter = new LinkedList<>();
        List<Vector2D> topFirstQuarter = new LinkedList<>();
        List<Vector2D> topSecondQuarter = new LinkedList<>();

        int a2 = width * width;
        int b2 = height * height;
        int fa2 = 4 * a2, fb2 = 4 * b2;
        int x, y, sigma;

        /* first half */
        for (x = 0, y = height, sigma = 2 * b2 + a2 * (1 - 2 * height); b2 * x <= a2 * y; x++) {
            bottomFirstQuarter.add(new Vector2D(xc + x, yc + y));
            bottomSecondQuarter.add(0, new Vector2D(xc - x, yc + y));
            topFirstQuarter.add(0, new Vector2D(xc + x, yc - y));
            topSecondQuarter.add(new Vector2D(xc - x, yc - y));
            if (sigma >= 0) {
                sigma += fa2 * (1 - y);
                y--;
            }
            sigma += b2 * ((4 * x) + 6);
        }

        int addedPoints = bottomFirstQuarter.size();

        /* second half */
        int i = 0;
        for (i = 0, x = width, y = 0, sigma = 2 * a2 + b2 * (1 - 2 * width); a2 * y <= b2 * x; y++, ++i) {
            bottomFirstQuarter.add(addedPoints, new Vector2D(xc + x, yc + y));
            bottomSecondQuarter.add(i, new Vector2D(xc - x, yc + y));
            topFirstQuarter.add(i, new Vector2D(xc + x, yc - y));
            topSecondQuarter.add(addedPoints, new Vector2D(xc - x, yc - y));
            if (sigma >= 0) {
                sigma += fb2 * (1 - x);
                x--;
            }
            sigma += a2 * ((4 * y) + 6);
        }

        points.addAll(bottomFirstQuarter);
        points.addAll(topFirstQuarter);
        points.addAll(topSecondQuarter);
        points.addAll(bottomSecondQuarter);

        return points;
    }

}
