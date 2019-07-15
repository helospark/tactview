package com.helospark.tactview.core.timeline.effect.colorize;

import static com.helospark.tactview.core.timeline.effect.interpolation.hint.ColorPickerType.CIRCLE;
import static com.helospark.tactview.core.timeline.effect.interpolation.hint.RenderTypeHint.TYPE;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.math.DoubleMath;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.StatelessVideoEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;
import com.helospark.tactview.core.timeline.effect.contractbrightness.BrignessContrastService;
import com.helospark.tactview.core.timeline.effect.contractbrightness.BrignessContrastServiceRequest;
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
public class ColorBalanceEffect extends StatelessVideoEffect {
    private static final double EPSILON = 0.000001;
    private ColorProvider shadowsShiftProvider;
    private ColorProvider midtonesShiftProvider;
    private ColorProvider highlightsShiftProvider;
    private DoubleProvider brightnessShiftProvider;
    private DoubleProvider contrastShiftProvider;
    private DoubleProvider colorTemperatureProvider;
    private DoubleProvider tintProvider;
    private DoubleProvider saturationChangeProvider;

    private boolean preserveLuminosity = true;

    private IndependentPixelOperation independentPixelOperation;
    private BrignessContrastService brignessContrastService;
    private ColorizeService colorizeService;
    private ColorTemperatureService colorTemperatureService;

    public ColorBalanceEffect(TimelineInterval interval, IndependentPixelOperation independentPixelOperation, BrignessContrastService brignessContrastService, ColorizeService colorizeService,
            ColorTemperatureService colorTemperatureService) {
        super(interval);
        this.independentPixelOperation = independentPixelOperation;
        this.brignessContrastService = brignessContrastService;
        this.colorizeService = colorizeService;
        this.colorTemperatureService = colorTemperatureService;
    }

    public ColorBalanceEffect(ColorBalanceEffect colorBalance, CloneRequestMetadata cloneRequestMetadata) {
        super(colorBalance, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(colorBalance, this);
    }

    public ColorBalanceEffect(JsonNode node, LoadMetadata loadMetadata, IndependentPixelOperation independentPixelOperation2, BrignessContrastService brignessContrastService2,
            ColorizeService colorizeService2, ColorTemperatureService colorTemperatureService) {
        super(node, loadMetadata);
        this.independentPixelOperation = independentPixelOperation2;
        this.brignessContrastService = brignessContrastService2;
        this.colorizeService = colorizeService2;
        this.colorTemperatureService = colorTemperatureService;
    }

    @Override
    public ReadOnlyClipImage createFrame(StatelessEffectRequest request) {
        ReadOnlyClipImage currentFrame = request.getCurrentFrame();
        ClipImage clipImage = ClipImage.sameSizeAs(currentFrame);

        applyThreeWayColorCorrect(request, currentFrame, clipImage);
        applyBrightnessContrast(request, clipImage);
        applyColorTemperature(request, clipImage);
        applySaturationChange(request, clipImage);

        return clipImage;
    }

    private void applySaturationChange(StatelessEffectRequest request, ClipImage clipImage) {
        double saturationChange = saturationChangeProvider.getValueAt(request.getEffectPosition());

        if (!DoubleMath.fuzzyEquals(saturationChange, 0.0, EPSILON)) {
            ColorizeRequest colorizeRequest = ColorizeRequest.builder()
                    .withHueChange(0.0)
                    .withSaturationChange(saturationChange)
                    .withValueChange(0.0)
                    .build();

            colorizeService.applyColorizeToFrame(clipImage, clipImage, colorizeRequest);
        }
    }

    private void applyColorTemperature(StatelessEffectRequest request, ClipImage clipImage) {
        double colorTemperature = colorTemperatureProvider.getValueAt(request.getEffectPosition());
        double tint = tintProvider.getValueAt(request.getEffectPosition());

        if (!DoubleMath.fuzzyEquals(colorTemperature, 0.0, EPSILON) || !DoubleMath.fuzzyEquals(tint, 0.0, EPSILON)) {
            ColorTemperatureChangeRequest temperatureChangeRequest = ColorTemperatureChangeRequest.builder()
                    .withTemperatureChange(colorTemperature)
                    .withTintChange(tint)
                    .build();
            colorTemperatureService.applyColorTemperatureChange(clipImage, temperatureChangeRequest);
        }
    }

    private void applyBrightnessContrast(StatelessEffectRequest request, ClipImage clipImage) {
        double brightness = brightnessShiftProvider.getValueAt(request.getEffectPosition());
        double contrast = contrastShiftProvider.getValueAt(request.getEffectPosition());

        if (!DoubleMath.fuzzyEquals(brightness, 0.0, EPSILON) || !DoubleMath.fuzzyEquals(contrast, 1.0, EPSILON)) {
            BrignessContrastServiceRequest brightnessContrastRequest = BrignessContrastServiceRequest.builder()
                    .withBrightness(brightness)
                    .withContrast(contrast)
                    .build();

            brignessContrastService.applyBrightnessContrastChangeToImage(clipImage, clipImage, brightnessContrastRequest);
        }
    }

    private void applyThreeWayColorCorrect(StatelessEffectRequest request, ReadOnlyClipImage currentFrame, ClipImage clipImage) {
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
            Color shadows = inputColor.subtractFromComponents(b).divideComponents(-a).addComponents(0.5).clamp(0.0, 1.0).multiplyComponents(scale).multiply(shadowsShift);
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
    }

    @Override
    public void initializeValueProvider() {
        shadowsShiftProvider = createColorProvider(0, 0, 0);
        midtonesShiftProvider = createColorProvider(0, 0, 0);
        highlightsShiftProvider = createColorProvider(0, 0, 0);
        brightnessShiftProvider = new DoubleProvider(-2, 2, new MultiKeyframeBasedDoubleInterpolator(0.0));
        contrastShiftProvider = new DoubleProvider(0, 3, new MultiKeyframeBasedDoubleInterpolator(1.0));
        colorTemperatureProvider = new DoubleProvider(-0.2, 0.2, new MultiKeyframeBasedDoubleInterpolator(0.0));
        tintProvider = new DoubleProvider(-0.2, 0.2, new MultiKeyframeBasedDoubleInterpolator(0.0));
        saturationChangeProvider = new DoubleProvider(-1.0, 1.0, new MultiKeyframeBasedDoubleInterpolator(0.0));
    };

    @Override
    public List<ValueProviderDescriptor> getValueProviders() {

        ValueProviderDescriptor shadowsShiftProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(shadowsShiftProvider)
                .withName("Shadow")
                .withRenderHints(Collections.singletonMap(TYPE, CIRCLE))
                .build();

        ValueProviderDescriptor midtonesShiftProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(midtonesShiftProvider)
                .withName("Midtone")
                .withRenderHints(Collections.singletonMap(TYPE, CIRCLE))
                .build();

        ValueProviderDescriptor highlightsShiftProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(highlightsShiftProvider)
                .withName("Highlight")
                .withRenderHints(Collections.singletonMap(TYPE, CIRCLE))
                .build();

        ValueProviderDescriptor brightnessShiftProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(brightnessShiftProvider)
                .withName("Brightness")
                .build();

        ValueProviderDescriptor contrastShiftProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(contrastShiftProvider)
                .withName("Contrast")
                .build();
        ValueProviderDescriptor colorTemperatureProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(colorTemperatureProvider)
                .withName("Color temperature")
                .build();
        ValueProviderDescriptor tintProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(tintProvider)
                .withName("Tint")
                .build();
        ValueProviderDescriptor saturationProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(saturationChangeProvider)
                .withName("Saturation")
                .build();

        return List.of(contrastShiftProviderDescriptor,
                brightnessShiftProviderDescriptor,
                colorTemperatureProviderDescriptor,
                tintProviderDescriptor,
                saturationProviderDescriptor,
                shadowsShiftProviderDescriptor,
                midtonesShiftProviderDescriptor,
                highlightsShiftProviderDescriptor);
    }

    private ColorProvider createColorProvider(double r, double g, double b) {
        return new ColorProvider(new DoubleProvider(new MultiKeyframeBasedDoubleInterpolator(r)),
                new DoubleProvider(new MultiKeyframeBasedDoubleInterpolator(g)),
                new DoubleProvider(new MultiKeyframeBasedDoubleInterpolator(b)));
    }

    @Override
    public StatelessEffect cloneEffect(CloneRequestMetadata cloneRequestMetadata) {
        return new ColorBalanceEffect(this, cloneRequestMetadata);
    }

}
