package com.helospark.tactview.core.timeline.effect.blur.service;

import com.helospark.lightdi.annotation.Service;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.util.IndependentPixelOperationImpl;

@Service
public class RadialBlurService {
    final int img_bpp = 4;

    private IndependentPixelOperationImpl independentPixelOperation;

    public RadialBlurService(IndependentPixelOperationImpl independentPixelOperation) {
        this.independentPixelOperation = independentPixelOperation;
    }

    public ClipImage radialBlur(RadialBlurRequest request) {
        ClipImage resultImage = ClipImage.sameSizeAs(request.inputImage);

        independentPixelOperation.executePixelTransformation(resultImage.getWidth(), resultImage.getHeight(), (x, y) -> {
            calculateValueForPixel(request, resultImage, x, y);
        });

        return resultImage;

    }

    private void calculateValueForPixel(RadialBlurRequest request, ClipImage resultImage, int x, int y) {
        int[] pixel = new int[4];
        int[] p1 = new int[4];
        double[] sum = new double[4];

        int center_x = request.centerX;
        int center_y = request.centerY;
        int width = request.inputImage.getWidth();
        int height = request.inputImage.getHeight();
        double angle = request.angle;

        int count = 0;
        double xx = 0.0;
        double yy = 0.0;

        double xr = x - center_x;
        double yr = y - center_y;

        double r = Math.sqrt(square(xr) + square(yr));
        double n = r * angle;

        if (angle == 0.0) {
            getPixelInto(request.inputImage, x, y, p1);
            copyPixel(resultImage, p1, x, y);
            return;
        }

        double phi = Math.atan2(yr, xr);

        for (int c = 0; c < img_bpp; c++) {
            sum[c] = 0;
        }

        double phi_start;
        if (n == 1)
            phi_start = phi;
        else
            phi_start = phi + angle / 2.0;

        double theta = angle / n;
        count = 0;

        for (int i = 0; i < n; i++) {
            double s_val = Math.sin(phi_start - i * theta);
            double c_val = Math.cos(phi_start - i * theta);

            xx = center_x + r * c_val;
            yy = center_y - r * s_val;

            if ((yy < 0) || (yy >= 0 + height) ||
                    (xx < 0) || (xx >= 0 + width))
                continue;

            ++count;
            getPixelInto(request.inputImage, xx, yy, pixel);

            for (int c = 0; c < img_bpp - 1; c++)
                sum[c] += (pixel[c] * (pixel[3] / 255.0));
        }

        if (count == 0) {
            getPixelInto(request.inputImage, xx, yy, p1);
            copyPixel(resultImage, p1, x, y);
        } else {
            for (int j = 0; j < 4; ++j) {
                p1[j] = (int) (sum[j] / count);
            }

            copyPixel(resultImage, p1, x, y);
        }

    }

    private void copyPixel(ClipImage inputImage, int[] sum, int x, int y) {
        inputImage.setRed(sum[0], x, y);
        inputImage.setGreen(sum[1], x, y);
        inputImage.setBlue(sum[2], x, y);
        inputImage.setAlpha(255, x, y);
    }

    private void getPixelInto(ReadOnlyClipImage inputImage, double x, double y, int[] p2) {
        for (int i = 0; i < 4; ++i) {
            p2[i] = inputImage.getColorComponentWithOffsetUsingInterpolation(x, y, i);
        }
    }

    private double square(double value) {
        return value * value;
    }

}
