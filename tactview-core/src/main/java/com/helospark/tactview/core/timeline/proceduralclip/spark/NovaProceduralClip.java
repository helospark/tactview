package com.helospark.tactview.core.timeline.proceduralclip.spark;

import java.util.List;
import java.util.Random;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.CloneRequestMetadata;
import com.helospark.tactview.core.LoadMetadata;
import com.helospark.tactview.core.ReflectionUtil;
import com.helospark.tactview.core.decoder.ImageMetadata;
import com.helospark.tactview.core.decoder.VisualMediaMetadata;
import com.helospark.tactview.core.timeline.GetFrameRequest;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Color;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ColorProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.IntegerProvider;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.timeline.proceduralclip.ProceduralVisualClip;
import com.helospark.tactview.core.util.IndependentPixelOperationImpl;
import com.helospark.tactview.core.util.MathUtil;

// Code adapted from GIMP: https://github.com/piksels-and-lines-orchestra/gimp/blob/master/plug-ins/common/nova.c
public class NovaProceduralClip extends ProceduralVisualClip {
    private IndependentPixelOperationImpl independentPixelOperation;

    private IntegerProvider randomHueProvider;
    private IntegerProvider numberOfSpokesProvider;
    private DoubleProvider radiusProvider;
    private IntegerProvider seedProvider;
    private ColorProvider colorProvider;
    private DoubleProvider centerXProvider;
    private DoubleProvider centerYProvider;

    public NovaProceduralClip(VisualMediaMetadata visualMediaMetadata, TimelineInterval interval, IndependentPixelOperationImpl independentPixelOperation) {
        super(visualMediaMetadata, interval);
        this.independentPixelOperation = independentPixelOperation;
    }

    public NovaProceduralClip(NovaProceduralClip novaProceduralClip, CloneRequestMetadata cloneRequestMetadata) {
        super(novaProceduralClip, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(novaProceduralClip, this);
    }

    public NovaProceduralClip(ImageMetadata metadata, JsonNode node, LoadMetadata loadMetadata, IndependentPixelOperationImpl independentPixelOperation) {
        super(metadata, node, loadMetadata);
        this.independentPixelOperation = independentPixelOperation;
    }

    @Override
    public ReadOnlyClipImage createProceduralFrame(GetFrameRequest request, TimelinePosition relativePosition) {
        int seed = seedProvider.getValueAt(relativePosition);
        int randomHue = randomHueProvider.getValueAt(relativePosition);
        int nspoke = numberOfSpokesProvider.getValueAt(relativePosition);
        double radius = radiusProvider.getValueAt(relativePosition) * request.getExpectedWidth();
        Color color = colorProvider.getValueAt(relativePosition).rgbToHsl();
        double centerX = centerXProvider.getValueAt(relativePosition);
        double centerY = centerXProvider.getValueAt(relativePosition);

        Random random = new Random(seed);
        double[] spoke = new double[nspoke];
        Color[] spokecolor = new Color[nspoke];
        for (int i = 0; i < nspoke; i++) {
            spoke[i] = random.nextDouble();

            Color hsv = new Color(color);

            hsv.red += randomHue / 360.0 * (random.nextDouble() - 0.5);

            if (hsv.red < 0)
                hsv.red += 1.0;
            else if (hsv.red >= 1.0)
                hsv.red -= 1.0;

            spokecolor[i] = hsv.hslToRgbColor();
        }

        int width = request.getExpectedWidth();
        int height = request.getExpectedHeight();

        ClipImage result = ClipImage.fromSize(width, height);

        independentPixelOperation.executePixelTransformation(width, height, (x, y) -> {
            setPixel(x, y, (int) (width * centerX), (int) (height * centerY), (int) radius, nspoke, result, spoke, spokecolor);
        });

        return result;
    }

    public void setPixel(int x, int y, int xc, int yc, int radius, int nspoke, ClipImage result, double[] spoke, Color[] spokecolor) {
        double u = (double) (x - xc) / radius;
        double v = (double) (y - yc) / radius;
        double l = Math.sqrt(u * u + v * v);

        double t = (Math.atan2(u, v) / (2 * Math.PI) + .51) * nspoke;
        int i = (int) Math.floor(t);
        t -= i;
        i %= nspoke;
        double w1 = spoke[i] * (1 - t) + spoke[(i + 1) % nspoke] * t;
        w1 = w1 * w1;

        double w = 1 / (l + 0.001) * 0.9;

        double nova_alpha = MathUtil.clamp(w, 0.0, 1.0);

        double red, green, blue, c;

        /* red */
        double spokecol = spokecolor[i].red * (1.0 - t) +
                spokecolor[(i + 1) % nspoke].red * t;
        if (w > 1.0)
            red = MathUtil.clamp(spokecol * w, 0.0, 1.0);
        else
            red = spokecol;
        c = MathUtil.clamp(w1 * w, 0.0, 1.0);
        red += c;

        /* green */
        spokecol = spokecolor[i].green * (1.0 - t) +
                spokecolor[(i + 1) % nspoke].green * t;
        if (w > 1.0)
            green = MathUtil.clamp(spokecol * w, 0.0, 1.0);
        else
            green = spokecol;
        c = MathUtil.clamp(w1 * w, 0.0, 1.0);
        green += c;

        /* blue */
        spokecol = spokecolor[i].blue * (1.0 - t) +
                spokecolor[(i + 1) % nspoke].blue * t;
        if (w > 1.0)
            blue = MathUtil.clamp(spokecol * w, 0.0, 1.0);
        else
            blue = spokecol;
        c = MathUtil.clamp(w1 * w, 0.0, 1.0);
        blue += c;

        result.setColorComponentByOffset((int) (red * 255.0), x, y, 0);
        result.setColorComponentByOffset((int) (green * 255.0), x, y, 1);
        result.setColorComponentByOffset((int) (blue * 255.0), x, y, 2);
        result.setColorComponentByOffset((int) (nova_alpha * 255.0), x, y, 3);
    }

    @Override
    protected void initializeValueProvider() {
        super.initializeValueProvider();

        randomHueProvider = new IntegerProvider(0, 360, new MultiKeyframeBasedDoubleInterpolator(0.0));
        radiusProvider = new DoubleProvider(0.0, 1.0, new MultiKeyframeBasedDoubleInterpolator(0.2));
        numberOfSpokesProvider = new IntegerProvider(1, 100, new MultiKeyframeBasedDoubleInterpolator(30.0));
        seedProvider = new IntegerProvider(-1000, 1000, new MultiKeyframeBasedDoubleInterpolator(123.0));
        centerXProvider = new DoubleProvider(0.0, 1.0, new MultiKeyframeBasedDoubleInterpolator(0.5));
        centerYProvider = new DoubleProvider(0.0, 1.0, new MultiKeyframeBasedDoubleInterpolator(0.5));
        colorProvider = ColorProvider.fromDefaultValue(0.5, 0.5, 1.0);
    }

    @Override
    public List<ValueProviderDescriptor> getDescriptorsInternal() {
        List<ValueProviderDescriptor> result = super.getDescriptorsInternal();

        ValueProviderDescriptor randomHueDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(randomHueProvider)
                .withName("random hue")
                .build();
        ValueProviderDescriptor radiusDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(radiusProvider)
                .withName("radius")
                .build();
        ValueProviderDescriptor numberOfSpokesProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(numberOfSpokesProvider)
                .withName("number of spokes")
                .build();
        ValueProviderDescriptor colorProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(colorProvider)
                .withName("color")
                .build();
        ValueProviderDescriptor centerXDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(centerXProvider)
                .withName("center X")
                .build();
        ValueProviderDescriptor centerYDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(centerYProvider)
                .withName("center Y")
                .build();

        result.add(randomHueDescriptor);
        result.add(radiusDescriptor);
        result.add(numberOfSpokesProviderDescriptor);
        result.add(colorProviderDescriptor);
        result.add(centerXDescriptor);
        result.add(centerYDescriptor);

        return result;
    }

    @Override
    public TimelineClip cloneClip(CloneRequestMetadata cloneRequestMetadata) {
        return new NovaProceduralClip(this, cloneRequestMetadata);
    }
}
