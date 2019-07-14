package com.helospark.tactview.core.util;

import java.util.List;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Polygon;

public interface BresenhemPixelProviderInterface {

    List<Vector2D> ellipsePixels(int xc, int yc, int width, int height);

    List<Vector2D> linePixels(Point startPoint, Point endPoint);

    List<Vector2D> polygonPixels(Polygon polygon);

    List<Vector2D> nurbsPixels(Polygon polygon, boolean connect);

}