package com.helospark.tactview.core.timeline.effect.distort;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.StatelessVideoEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.BooleanProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.util.IndependentPixelOperation;
import com.helospark.tactview.core.util.ReflectionUtil;

// Part of the code is adapted from Gimp: https://github.com/piksels-and-lines-orchestra/gimp/blob/master/plug-ins/common/polar-coords.c
public class PolarCoordinateEffect extends StatelessVideoEffect {
    private IndependentPixelOperation independentPixelOperation;

    private DoubleProvider circleDepthProvider;
    private DoubleProvider offsetAngleProvider;
    private BooleanProvider mapBackwardProvider;
    private BooleanProvider toPolarProvider;
    private BooleanProvider mapFromTopProvider;

    public PolarCoordinateEffect(TimelineInterval interval, IndependentPixelOperation independentPixelOperation) {
        super(interval);
        this.independentPixelOperation = independentPixelOperation;
    }

    public PolarCoordinateEffect(PolarCoordinateEffect polarCoordinateEffect, CloneRequestMetadata cloneRequestMetadata) {
        super(polarCoordinateEffect, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(polarCoordinateEffect, this);
    }

    public PolarCoordinateEffect(JsonNode node, LoadMetadata loadMetadata, IndependentPixelOperation independentPixelOperation) {
        super(node, loadMetadata);
        this.independentPixelOperation = independentPixelOperation;
    }

    @Override
    public ReadOnlyClipImage createFrame(StatelessEffectRequest request) {
        ReadOnlyClipImage currentFrame = request.getCurrentFrame();
        ClipImage result = ClipImage.sameSizeAs(currentFrame);

        double circleDepth = circleDepthProvider.getValueAt(request.getEffectPosition());
        double offsetAngleDepth = offsetAngleProvider.getValueAt(request.getEffectPosition());
        boolean mapBackward = mapBackwardProvider.getValueAt(request.getEffectPosition());
        boolean toPolar = toPolarProvider.getValueAt(request.getEffectPosition());
        boolean mapFromTop = mapFromTopProvider.getValueAt(request.getEffectPosition());

        independentPixelOperation.executePixelTransformation(currentFrame.getWidth(), currentFrame.getHeight(), (x, y) -> {
            fillPixelWithDistortedPixelColor(currentFrame, result, x, y, offsetAngleDepth, circleDepth, toPolar, mapFromTop, mapBackward);
        });

        return result;
    }

    private void fillPixelWithDistortedPixelColor(ReadOnlyClipImage sourceImage,
            ClipImage result,
            int x, int y,
            double offsetAngleRadian,
            double circlePercentage,
            boolean toPolar,
            boolean mapFromTop,
            boolean mapBackward) {

        double phi2;
        double xx, yy;
        double r;
        double m;
        double xmax, ymax, rmax;
        double x_calc, y_calc;
        double t;

        int cen_x = sourceImage.getWidth() / 2;
        int cen_y = sourceImage.getHeight() / 2;

        double phi = 0.0;
        double x1 = 0;
        double y1 = 0;
        double x2 = sourceImage.getWidth();
        double y2 = sourceImage.getHeight();
        double xdiff = x2 - x1;
        double ydiff = y2 - y1;
        double xm = xdiff / 2.0;
        double ym = ydiff / 2.0;

        if (toPolar) {
            if (x >= cen_x) {
                if (y > cen_y) {
                    phi = Math.PI - Math.atan(((double) (x - cen_x)) /
                            ((double) (y - cen_y)));
                } else if (y < cen_y) {
                    phi = Math.atan(((double) (x - cen_x)) / ((double) (cen_y - y)));
                } else {
                    phi = Math.PI / 2;
                }
            } else if (x < cen_x) {
                if (y < cen_y) {
                    phi = 2 * Math.PI - Math.atan(((double) (cen_x - x)) /
                            ((double) (cen_y - y)));
                } else if (y > cen_y) {
                    phi = Math.PI + Math.atan(((double) (cen_x - x)) /
                            ((double) (y - cen_y)));
                } else {
                    phi = 1.5 * Math.PI;
                }
            }
            int i = x - cen_x;
            int i1 = y - cen_y;

            r = Math.sqrt(i * i + i1 * i1);

            if (x != cen_x) {
                m = Math.abs(((double) (y - cen_y)) / ((double) (x - cen_x)));
            } else {
                m = 0;
            }

            if (m <= ((y2 - y1) / (x2 - x1))) {
                if (x == cen_x) {
                    xmax = 0;
                    ymax = cen_y - y1;
                } else {
                    xmax = cen_x - x1;
                    ymax = m * xmax;
                }
            } else {
                ymax = cen_y - y1;
                xmax = ymax / m;
            }

            rmax = Math.sqrt(xmax * xmax + ymax * ymax);

            t = ((cen_y - y1) < (cen_x - x1)) ? (cen_y - y1) : (cen_x - x1);
            rmax = (rmax - t) / 100 * (100 - circlePercentage) + t;

            phi = (phi + offsetAngleRadian) % (2 * Math.PI);

            if (mapBackward)
                x_calc = x2 - 1 - (x2 - x1 - 1) / (2 * Math.PI) * phi;
            else
                x_calc = (x2 - x1 - 1) / (2 * Math.PI) * phi + x1;

            if (mapFromTop)
                y_calc = (y2 - y1) / rmax * r + y1;
            else
                y_calc = y2 - (y2 - y1) / rmax * r;
        } else {
            if (mapBackward)
                phi = (2 * Math.PI) * (x2 - x) / xdiff;
            else
                phi = (2 * Math.PI) * (x - x1) / xdiff;

            phi = (phi + offsetAngleRadian) % (2 * Math.PI);

            if (phi >= 1.5 * Math.PI)
                phi2 = 2 * Math.PI - phi;
            else if (phi >= Math.PI)
                phi2 = phi - Math.PI;
            else if (phi >= 0.5 * Math.PI)
                phi2 = Math.PI - phi;
            else
                phi2 = phi;

            xx = Math.tan(phi2);
            if (xx != 0)
                m = 1.0 / xx;
            else
                m = 0;

            if (m <= ((ydiff) / (xdiff))) {
                if (phi2 == 0) {
                    xmax = 0;
                    ymax = ym - y1;
                } else {
                    xmax = xm - x1;
                    ymax = m * xmax;
                }
            } else {
                ymax = ym - y1;
                xmax = ymax / m;
            }

            rmax = Math.sqrt(xmax * xmax + ymax * ymax);

            t = ((ym - y1) < (xm - x1)) ? (ym - y1) : (xm - x1);

            rmax = (rmax - t) / 100.0 * (100 - circlePercentage) + t;

            if (mapFromTop)
                r = rmax * ((y - y1) / (ydiff));
            else
                r = rmax * ((y2 - y) / (ydiff));

            xx = r * Math.sin(phi2);
            yy = r * Math.cos(phi2);

            if (phi >= 1.5 * Math.PI) {
                x_calc = xm - xx;
                y_calc = ym - yy;
            } else if (phi >= Math.PI) {
                x_calc = xm - xx;
                y_calc = ym + yy;
            } else if (phi >= 0.5 * Math.PI) {
                x_calc = xm + xx;
                y_calc = ym + yy;
            } else {
                x_calc = xm + xx;
                y_calc = ym - yy;
            }
        }

        for (int i = 0; i < 4; ++i) {
            int color = sourceImage.getColorComponentWithOffsetUsingInterpolation(x_calc, y_calc, i);
            result.setColorComponentByOffset(color, x, y, i);
        }

    }

    @Override
    public void initializeValueProvider() {
        circleDepthProvider = new DoubleProvider(0.0, 100.0, new MultiKeyframeBasedDoubleInterpolator(100.0));
        offsetAngleProvider = new DoubleProvider(0.0, 2.0 * Math.PI, new MultiKeyframeBasedDoubleInterpolator(0.0));
        mapBackwardProvider = new BooleanProvider(new MultiKeyframeBasedDoubleInterpolator(0.0));
        toPolarProvider = new BooleanProvider(new MultiKeyframeBasedDoubleInterpolator(1.0));
        mapFromTopProvider = new BooleanProvider(new MultiKeyframeBasedDoubleInterpolator(0.0));
    }

    @Override
    public List<ValueProviderDescriptor> getValueProviders() {
        ValueProviderDescriptor circleDepthProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(circleDepthProvider)
                .withName("Circle deapth")
                .build();
        ValueProviderDescriptor offsetAngleProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(offsetAngleProvider)
                .withName("Offset angle")
                .build();
        ValueProviderDescriptor mapBackwardProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(mapBackwardProvider)
                .withName("Map backward")
                .build();
        ValueProviderDescriptor mapFromTopProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(mapFromTopProvider)
                .withName("Map from top")
                .build();
        ValueProviderDescriptor toPolarProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(toPolarProvider)
                .withName("To polar")
                .build();

        return List.of(circleDepthProviderDescriptor, offsetAngleProviderDescriptor, mapBackwardProviderDescriptor, mapFromTopProviderDescriptor, toPolarProviderDescriptor);
    }

    @Override
    public StatelessEffect cloneEffect(CloneRequestMetadata cloneRequestMetadata) {
        return new PolarCoordinateEffect(this, cloneRequestMetadata);
    }

}
