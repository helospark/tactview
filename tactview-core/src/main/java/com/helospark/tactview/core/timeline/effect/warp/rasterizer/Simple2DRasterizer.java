package com.helospark.tactview.core.timeline.effect.warp.rasterizer;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import com.helospark.lightdi.annotation.Component;

// https://www.scratchapixel.com/lessons/3d-basic-rendering/rasterization-practical-implementation/rasterization-stage
// GNU license
@Component
public class Simple2DRasterizer {

    public void rasterizeTriangle(TriangleRasterizationRequest request) {
        SimpleTriangle triangle = request.triangle;

        SimpleVertex v0 = triangle.a;
        SimpleVertex v1 = triangle.b;
        SimpleVertex v2 = triangle.c;

        double area = edgeFunction(v0.position, v1.position, v2.position);

        double[] colorComponentMultiplier = new double[4];

        for (int j = 0; j < request.result.getHeight(); ++j) {
            for (int i = 0; i < request.result.getWidth(); ++i) {
                Vector2D p = new Vector2D(i + 0.5, j + 0.5);
                double w0 = edgeFunction(v1.position, v2.position, p);
                double w1 = edgeFunction(v2.position, v0.position, p);
                double w2 = edgeFunction(v0.position, v1.position, p);
                if (w0 >= 0 && w1 >= 0 && w2 >= 0) {
                    w0 /= area;
                    w1 /= area;
                    w2 /= area;
                    double r = w0 * v0.color.red + w1 * v1.color.red + w2 * v2.color.red;
                    double g = w0 * v0.color.green + w1 * v1.color.green + w2 * v2.color.green;
                    double b = w0 * v0.color.blue + w1 * v1.color.blue + w2 * v2.color.blue;

                    colorComponentMultiplier[0] = r;
                    colorComponentMultiplier[1] = g;
                    colorComponentMultiplier[2] = b;
                    colorComponentMultiplier[3] = 1.0;

                    double s = (w0 * v0.textureCoordinates.getX() + w1 * v1.textureCoordinates.getX() + w2 * v2.textureCoordinates.getX()) * triangle.texture.getWidth();
                    double t = (w0 * v0.textureCoordinates.getY() + w1 * v1.textureCoordinates.getY() + w2 * v2.textureCoordinates.getY()) * triangle.texture.getHeight();

                    for (int c = 0; c < 4; ++c) {
                        int color = (int) (triangle.texture.getColorComponentWithOffsetUsingInterpolation(s, t, c) * colorComponentMultiplier[c]);
                        request.result.setColorComponentByOffset(color, i, j, c);
                    }
                }
            }
        }
    }

    double edgeFunction(Vector2D a, Vector2D b, Vector2D c) {
        return (c.getX() - a.getX()) * (b.getY() - a.getY()) - (c.getY() - a.getY()) * (b.getX() - a.getX());
    }
}
