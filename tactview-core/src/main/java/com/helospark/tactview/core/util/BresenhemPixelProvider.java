package com.helospark.tactview.core.util;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;
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

    @Cacheable
    public List<Vector2D> linePixels(Point startPoint, Point endPoint) {
        List<Vector2D> result = new ArrayList<>();

        int x1 = (int) startPoint.x;
        int x2 = (int) endPoint.x;

        int y1 = (int) startPoint.y;
        int y2 = (int) endPoint.y;
        boolean steep = (Math.abs(y2 - y1) > Math.abs(x2 - x1));
        if (steep) {
            int tmp = x1;
            x1 = y1;
            y1 = tmp;

            tmp = x2;
            x2 = y2;
            y2 = tmp;
        }

        if (x1 > x2) {
            int tmp = x1;
            x1 = x2;
            x2 = tmp;

            tmp = y1;
            y1 = y2;
            y2 = tmp;
        }

        double dx = x2 - x1;
        double dy = Math.abs(y2 - y1);

        double error = dx / 2.0f;
        int ystep = (y1 < y2) ? 1 : -1;
        int y = y1;

        int maxX = x2;

        for (int x = x1; x < maxX; x++) {
            if (steep) {
                result.add(new Vector2D(y, x));
            } else {
                result.add(new Vector2D(x, y));
            }

            error -= dy;
            if (error < 0) {
                y += ystep;
                error += dx;
            }
        }

        return result;
    }

}
