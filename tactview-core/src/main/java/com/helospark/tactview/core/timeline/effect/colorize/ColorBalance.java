package com.helospark.tactview.core.timeline.effect.colorize;

import java.util.List;

import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.StatelessVideoEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Color;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ColorProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.util.IndependentPixelOperation;
import com.helospark.tactview.core.util.ReflectionUtil;

/**
 * 3 way color balance
 * 
 * Code based on GIMP:
 * https://github.com/GNOME/gimp/blob/5f700549e7bd1881678152bc1bd20e116a8ea444/app/operations/gimpoperationcolorbalance.c#L86
 * and GPUImage library:
 * https://github.com/liovch/GPUImage/commit/fcc85db4fdafae1d4e41313c96bb1cac54dc93b4
 * @author helospark
 */
public class ColorBalance extends StatelessVideoEffect {
    private ColorProvider shadowsShiftProvider;
    private ColorProvider midtonesShiftProvider;
    private ColorProvider highlightsShiftProvider;
    private boolean preserveLuminosity = true;

    private IndependentPixelOperation independentPixelOperation;

    public ColorBalance(TimelineInterval interval, IndependentPixelOperation independentPixelOperation) {
        super(interval);
        this.independentPixelOperation = independentPixelOperation;
    }

    public ColorBalance(ColorBalance colorBalance) {
        super(colorBalance);
        ReflectionUtil.copyOrCloneFieldFromTo(colorBalance, this);
    }

    @Override
    public ReadOnlyClipImage createFrame(StatelessEffectRequest request) {
        ReadOnlyClipImage currentFrame = request.getCurrentFrame();
        ClipImage clipImage = ClipImage.sameSizeAs(currentFrame);

        Color shadowsShift = shadowsShiftProvider.getValueAt(request.getEffectPosition());
        Color midtonesShift = midtonesShiftProvider.getValueAt(request.getEffectPosition());
        Color highlightsShift = highlightsShiftProvider.getValueAt(request.getEffectPosition());

        independentPixelOperation.executePixelTransformation(clipImage.getWidth(), clipImage.getHeight(), (x, y) -> {
            int red = currentFrame.getRed(x, y);
            int green = currentFrame.getGreen(x, y);
            int blue = currentFrame.getBlue(x, y);
            int alpha = currentFrame.getAlpha(x, y);

            Color inputColor = Color.of(red / 255.0, green / 255.0, blue / 255.0);

            double a = 0.25;
            double b = 0.333;
            double scale = 0.7;
            Color shadows = inputColor.subtractFromComponents(b).divideComponents(-a).addComponents(0.5).multiplyComponents(1.3).clamp(0.0, 1.0).multiplyComponents(scale).multiply(shadowsShift);
            Color midtones = inputColor.subtractFromComponents(b).divideComponents(a).addComponents(0.5).clamp(0.0, 1.0)
                    .multiply(inputColor.addComponents(b).subtractFromComponents(-1.0).clamp(0.0, 1.0).multiplyComponents(scale)).multiply(midtonesShift);
            Color highlights = inputColor.addComponents(b).subtractFromComponents(1.0).divideComponents(a).addComponents(0.5).clamp(0.0, 1.0).multiplyComponents(scale).multiply(highlightsShift);

            Color result = inputColor.add(shadows).add(midtones).add(highlights).clamp(0.0, 1.0);

            Color finalImage;
            if (preserveLuminosity) {
                Color newHSL = result.rgbToHsl();
                double oldLum = inputColor.getLuminance();
                finalImage = new Color(newHSL.red, newHSL.green, oldLum).hslToRgbColor();
            } else {
                finalImage = inputColor;
            }

            clipImage.setRed((int) (finalImage.red * 255.0), x, y);
            clipImage.setGreen((int) (finalImage.green * 255.0), x, y);
            clipImage.setBlue((int) (finalImage.blue * 255.0), x, y);
            clipImage.setAlpha(alpha, x, y);
        });

        return clipImage;
    }

    @Override
    public List<ValueProviderDescriptor> getValueProviders() {
        shadowsShiftProvider = createColorProvider(0, 0, 0);
        midtonesShiftProvider = createColorProvider(0, 0, 0);
        highlightsShiftProvider = createColorProvider(0, 0, 0);

        ValueProviderDescriptor shadowsShiftProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(shadowsShiftProvider)
                .withName("Shadow")
                .build();

        ValueProviderDescriptor midtonesShiftProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(midtonesShiftProvider)
                .withName("Midtone")
                .build();

        ValueProviderDescriptor highlightsShiftProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(highlightsShiftProvider)
                .withName("Highlight")
                .build();

        return List.of(shadowsShiftProviderDescriptor, midtonesShiftProviderDescriptor, highlightsShiftProviderDescriptor);
    }

    private ColorProvider createColorProvider(double r, double g, double b) {
        return new ColorProvider(new DoubleProvider(new MultiKeyframeBasedDoubleInterpolator(r)),
                new DoubleProvider(new MultiKeyframeBasedDoubleInterpolator(g)),
                new DoubleProvider(new MultiKeyframeBasedDoubleInterpolator(b)));
    }

    @Override
    public StatelessEffect cloneEffect() {
        return new ColorBalance(this);
    };

}
