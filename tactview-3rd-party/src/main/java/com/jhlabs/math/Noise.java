/*
Copyright 2006 Jerry Huxtable

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.jhlabs.math;

import java.util.Random;

/**
 * Perlin Noise functions
 */
public class Noise implements Function1D, Function2D, Function3D {

    private static Random randomGenerator = new Random();

    @Override
    public double evaluate(double x) {
        return noise1(x);
    }

    @Override
    public double evaluate(double x, double y) {
        return noise2(x, y);
    }

    @Override
    public double evaluate(double x, double y, double z) {
        return noise3(x, y, z);
    }

    /**
     * Compute turbulence using Perlin noise.
     * @param x the x value
     * @param y the y value
     * @param octaves number of octaves of turbulence
     * @return turbulence value at (x,y)
     */
    public static double turbulence2(double x, double y, double octaves) {
        double t = 0.0f;

        for (double f = 1.0f; f <= octaves; f *= 2)
            t += Math.abs(noise2(f * x, f * y)) / f;
        return t;
    }

    /**
     * Compute turbulence using Perlin noise.
     * @param x the x value
     * @param y the y value
     * @param octaves number of octaves of turbulence
     * @return turbulence value at (x,y)
     */
    public static double turbulence3(double x, double y, double z, double octaves) {
        double t = 0.0f;

        for (double f = 1.0f; f <= octaves; f *= 2)
            t += Math.abs(noise3(f * x, f * y, f * z)) / f;
        return t;
    }

    private final static int B = 0x100;
    private final static int BM = 0xff;
    private final static int N = 0x1000;

    static int[] p = new int[B + B + 2];
    static double[][] g3 = new double[B + B + 2][3];
    static double[][] g2 = new double[B + B + 2][2];
    static double[] g1 = new double[B + B + 2];
    static boolean start = true;

    private static double sCurve(double t) {
        return t * t * (3.0f - 2.0f * t);
    }

    /**
     * Compute 1-dimensional Perlin noise.
     * @param x the x value
     * @return noise value at x in the range -1..1
     */
    public static double noise1(double x) {
        int bx0, bx1;
        double rx0, rx1, sx, t, u, v;

        if (start) {
            start = false;
            init();
        }

        t = x + N;
        bx0 = ((int) t) & BM;
        bx1 = (bx0 + 1) & BM;
        rx0 = t - (int) t;
        rx1 = rx0 - 1.0f;

        sx = sCurve(rx0);

        u = rx0 * g1[p[bx0]];
        v = rx1 * g1[p[bx1]];
        return 2.3f * lerp(sx, u, v);
    }

    /**
     * Compute 2-dimensional Perlin noise.
     * @param x the x coordinate
     * @param y the y coordinate
     * @return noise value at (x,y)
     */
    public static double noise2(double x, double y) {
        int bx0, bx1, by0, by1, b00, b10, b01, b11;
        double rx0, rx1, ry0, ry1, q[], sx, sy, a, b, t, u, v;
        int i, j;

        if (start) {
            start = false;
            init();
        }

        t = x + N;
        bx0 = ((int) t) & BM;
        bx1 = (bx0 + 1) & BM;
        rx0 = t - (int) t;
        rx1 = rx0 - 1.0f;

        t = y + N;
        by0 = ((int) t) & BM;
        by1 = (by0 + 1) & BM;
        ry0 = t - (int) t;
        ry1 = ry0 - 1.0f;

        i = p[bx0];
        j = p[bx1];

        b00 = p[i + by0];
        b10 = p[j + by0];
        b01 = p[i + by1];
        b11 = p[j + by1];

        sx = sCurve(rx0);
        sy = sCurve(ry0);

        q = g2[b00];
        u = rx0 * q[0] + ry0 * q[1];
        q = g2[b10];
        v = rx1 * q[0] + ry0 * q[1];
        a = lerp(sx, u, v);

        q = g2[b01];
        u = rx0 * q[0] + ry1 * q[1];
        q = g2[b11];
        v = rx1 * q[0] + ry1 * q[1];
        b = lerp(sx, u, v);

        return 1.5f * lerp(sy, a, b);
    }

    /**
     * Compute 3-dimensional Perlin noise.
     * @param x the x coordinate
     * @param y the y coordinate
     * @param y the y coordinate
     * @return noise value at (x,y,z)
     */
    public static double noise3(double x, double y, double z) {
        int bx0, bx1, by0, by1, bz0, bz1, b00, b10, b01, b11;
        double rx0, rx1, ry0, ry1, rz0, rz1, q[], sy, sz, a, b, c, d, t, u, v;
        int i, j;

        if (start) {
            start = false;
            init();
        }

        t = x + N;
        bx0 = ((int) t) & BM;
        bx1 = (bx0 + 1) & BM;
        rx0 = t - (int) t;
        rx1 = rx0 - 1.0f;

        t = y + N;
        by0 = ((int) t) & BM;
        by1 = (by0 + 1) & BM;
        ry0 = t - (int) t;
        ry1 = ry0 - 1.0f;

        t = z + N;
        bz0 = ((int) t) & BM;
        bz1 = (bz0 + 1) & BM;
        rz0 = t - (int) t;
        rz1 = rz0 - 1.0f;

        i = p[bx0];
        j = p[bx1];

        b00 = p[i + by0];
        b10 = p[j + by0];
        b01 = p[i + by1];
        b11 = p[j + by1];

        t = sCurve(rx0);
        sy = sCurve(ry0);
        sz = sCurve(rz0);

        q = g3[b00 + bz0];
        u = rx0 * q[0] + ry0 * q[1] + rz0 * q[2];
        q = g3[b10 + bz0];
        v = rx1 * q[0] + ry0 * q[1] + rz0 * q[2];
        a = lerp(t, u, v);

        q = g3[b01 + bz0];
        u = rx0 * q[0] + ry1 * q[1] + rz0 * q[2];
        q = g3[b11 + bz0];
        v = rx1 * q[0] + ry1 * q[1] + rz0 * q[2];
        b = lerp(t, u, v);

        c = lerp(sy, a, b);

        q = g3[b00 + bz1];
        u = rx0 * q[0] + ry0 * q[1] + rz1 * q[2];
        q = g3[b10 + bz1];
        v = rx1 * q[0] + ry0 * q[1] + rz1 * q[2];
        a = lerp(t, u, v);

        q = g3[b01 + bz1];
        u = rx0 * q[0] + ry1 * q[1] + rz1 * q[2];
        q = g3[b11 + bz1];
        v = rx1 * q[0] + ry1 * q[1] + rz1 * q[2];
        b = lerp(t, u, v);

        d = lerp(sy, a, b);

        return 1.5f * lerp(sz, c, d);
    }

    public static double lerp(double t, double a, double b) {
        return a + t * (b - a);
    }

    private static void normalize2(double v[]) {
        double s = Math.sqrt(v[0] * v[0] + v[1] * v[1]);
        v[0] = v[0] / s;
        v[1] = v[1] / s;
    }

    static void normalize3(double v[]) {
        double s = Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
        v[0] = v[0] / s;
        v[1] = v[1] / s;
        v[2] = v[2] / s;
    }

    private static int random() {
        return randomGenerator.nextInt() & 0x7fffffff;
    }

    private static void init() {
        int i, j, k;

        for (i = 0; i < B; i++) {
            p[i] = i;

            g1[i] = (double) ((random() % (B + B)) - B) / B;

            for (j = 0; j < 2; j++)
                g2[i][j] = (double) ((random() % (B + B)) - B) / B;
            normalize2(g2[i]);

            for (j = 0; j < 3; j++)
                g3[i][j] = (double) ((random() % (B + B)) - B) / B;
            normalize3(g3[i]);
        }

        for (i = B - 1; i >= 0; i--) {
            k = p[i];
            p[i] = p[j = random() % B];
            p[j] = k;
        }

        for (i = 0; i < B + 2; i++) {
            p[B + i] = p[i];
            g1[B + i] = g1[i];
            for (j = 0; j < 2; j++)
                g2[B + i][j] = g2[i][j];
            for (j = 0; j < 3; j++)
                g3[B + i][j] = g3[i][j];
        }
    }

    /**
     * Returns the minimum and maximum of a number of random values
     * of the given function. This is useful for making some stab at
     * normalising the function.
     */
    public static double[] findRange(Function1D f, double[] minmax) {
        if (minmax == null)
            minmax = new double[2];
        double min = 0, max = 0;
        // Some random numbers here...
        for (double x = -100; x < 100; x += 1.27139) {
            double n = f.evaluate(x);
            min = Math.min(min, n);
            max = Math.max(max, n);
        }
        minmax[0] = min;
        minmax[1] = max;
        return minmax;
    }

    /**
     * Returns the minimum and maximum of a number of random values
     * of the given function. This is useful for making some stab at
     * normalising the function.
     */
    public static double[] findRange(Function2D f, double[] minmax) {
        if (minmax == null)
            minmax = new double[2];
        double min = 0, max = 0;
        // Some random numbers here...
        for (double y = -100; y < 100; y += 10.35173) {
            for (double x = -100; x < 100; x += 10.77139) {
                double n = f.evaluate(x, y);
                min = Math.min(min, n);
                max = Math.max(max, n);
            }
        }
        minmax[0] = min;
        minmax[1] = max;
        return minmax;
    }

}
