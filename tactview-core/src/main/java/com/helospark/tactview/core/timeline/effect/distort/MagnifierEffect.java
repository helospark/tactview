package com.helospark.tactview.core.timeline.effect.distort;

import java.util.List;

import org.apache.commons.math3.util.FastMath;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.CloneRequestMetadata;
import com.helospark.tactview.core.LoadMetadata;
import com.helospark.tactview.core.ReflectionUtil;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.StatelessVideoEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.PointProvider;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.util.IndependentPixelOperation;
import com.helospark.tactview.core.util.MathUtil;

// Logic adapted from http://www.jhlabs.com/ie/index.html
public class MagnifierEffect extends StatelessVideoEffect {
    private static final double HALF_PI = Math.PI / 2.0;

    private IndependentPixelOperation independentPixelOperation;

    private PointProvider centerProvider;
    private DoubleProvider refractionProvider;

    private DoubleProvider widthProvider;
    private DoubleProvider heightProvider;

    public MagnifierEffect(TimelineInterval interval, IndependentPixelOperation independentPixelOperation) {
        super(interval);
        this.independentPixelOperation = independentPixelOperation;
    }

    public MagnifierEffect(MagnifierEffect lensDistortEffect, CloneRequestMetadata cloneRequestMetadata) {
        super(lensDistortEffect, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(lensDistortEffect, this);
    }

    public MagnifierEffect(JsonNode node, LoadMetadata loadMetadata, IndependentPixelOperation independentPixelOperation) {
        super(node, loadMetadata);
        this.independentPixelOperation = independentPixelOperation;
    }

    @Override
    public ReadOnlyClipImage createFrame(StatelessEffectRequest request) {
        ReadOnlyClipImage frame = request.getCurrentFrame();
        ClipImage result = ClipImage.sameSizeAs(frame);

        Point center = centerProvider.getValueAt(request.getEffectPosition()).multiply(request.getCurrentFrame().getWidth(), request.getCurrentFrame().getHeight());
        double refractionIndex = refractionProvider.getValueAt(request.getEffectPosition());

        double width = widthProvider.getValueAt(request.getEffectPosition()) * request.getCurrentFrame().getWidth();
        double height = heightProvider.getValueAt(request.getEffectPosition()) * request.getCurrentFrame().getHeight();

        double a2 = width * width;
        double b2 = height * height;

        independentPixelOperation.executePixelTransformation(frame.getWidth(), frame.getHeight(), (x, y) -> {
            double dx = x - center.x;
            double dy = y - center.y;
            double x2 = dx * dx;
            double y2 = dy * dy;

            double ox, oy;
            if (y2 >= (b2 - (b2 * x2) / a2)) {
                ox = x;
                oy = y;
            } else {
                double rRefraction = 1.0 / refractionIndex;

                double z = Math.sqrt((1.0 - x2 / a2 - y2 / b2) * (center.x * center.y));
                double z2 = z * z;

                double xAngle = FastMath.acos(dx / Math.sqrt(x2 + z2));
                double angle1 = HALF_PI - xAngle;
                double angle2 = FastMath.asin(FastMath.sin(angle1) * rRefraction);
                angle2 = HALF_PI - xAngle - angle2;
                ox = x - FastMath.tan(angle2) * z;

                double yAngle = FastMath.acos(dy / Math.sqrt(y2 + z2));
                angle1 = HALF_PI - yAngle;
                angle2 = FastMath.asin(FastMath.sin(angle1) * rRefraction);
                angle2 = HALF_PI - yAngle - angle2;
                oy = y - FastMath.tan(angle2) * z;
            }

            ox = MathUtil.clamp(ox, 0, frame.getWidth() - 1);
            oy = MathUtil.clamp(oy, 0, frame.getHeight() - 1);

            for (int i = 0; i < 4; ++i) {
                int color = frame.getColorComponentWithOffset((int) ox, (int) oy, i);
                result.setColorComponentByOffset(color, x, y, i);
            }
        });

        return result;
    }

    @Override
    public void initializeValueProvider() {
        centerProvider = PointProvider.ofNormalizedImagePosition(0.5, 0.5);
        refractionProvider = new DoubleProvider(1.0, 20.0, new MultiKeyframeBasedDoubleInterpolator(2.0));
        widthProvider = new DoubleProvider(0.0, 5.0, new MultiKeyframeBasedDoubleInterpolator(0.5));
        heightProvider = new DoubleProvider(0.0, 5.0, new MultiKeyframeBasedDoubleInterpolator(0.5));
    }

    @Override
    public List<ValueProviderDescriptor> getValueProviders() {
        ValueProviderDescriptor centerProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(centerProvider)
                .withName("Center")
                .build();
        ValueProviderDescriptor refractionProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(refractionProvider)
                .withName("Refraction")
                .build();
        ValueProviderDescriptor widthProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(widthProvider)
                .withName("Width")
                .build();
        ValueProviderDescriptor heightProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(heightProvider)
                .withName("Height")
                .build();

        return List.of(centerProviderDescriptor, refractionProviderDescriptor, widthProviderDescriptor, heightProviderDescriptor);
    }

    @Override
    public StatelessEffect cloneEffect(CloneRequestMetadata cloneRequestMetadata) {
        return new MagnifierEffect(this, cloneRequestMetadata);
    }

}
