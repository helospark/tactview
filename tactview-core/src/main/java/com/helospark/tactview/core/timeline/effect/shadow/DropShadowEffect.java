package com.helospark.tactview.core.timeline.effect.shadow;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.decoder.framecache.GlobalMemoryManagerAccessor;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.StatelessVideoEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;
import com.helospark.tactview.core.timeline.effect.blur.BlurRequest;
import com.helospark.tactview.core.timeline.effect.blur.BlurService;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Color;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ColorProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.effect.scale.service.ScaleRequest;
import com.helospark.tactview.core.timeline.effect.scale.service.ScaleService;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.util.IndependentPixelOperation;
import com.helospark.tactview.core.util.MathUtil;
import com.helospark.tactview.core.util.ReflectionUtil;

public class DropShadowEffect extends StatelessVideoEffect {
    private IndependentPixelOperation independentPixelOperation;
    private DoubleProvider shadowBlurXProvider;
    private DoubleProvider shadowBlurYProvider;
    private DoubleProvider shadowMultiplierProvider;

    private DoubleProvider shiftXProvider;
    private DoubleProvider shiftYProvider;
    private ColorProvider shadowColorProvider;
    private DoubleProvider maximumAlphaProvider;
    private DoubleProvider scaleProvider;

    private BlurService blurService;
    private ScaleService scaleService;

    public DropShadowEffect(TimelineInterval interval, IndependentPixelOperation independentPixelOperation, BlurService blurService, ScaleService scaleService) {
        super(interval);
        this.independentPixelOperation = independentPixelOperation;
        this.blurService = blurService;
        this.scaleService = scaleService;
    }

    public DropShadowEffect(DropShadowEffect dropShadowEffect, CloneRequestMetadata cloneRequestMetadata) {
        super(dropShadowEffect, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(dropShadowEffect, this);
    }

    public DropShadowEffect(JsonNode node, LoadMetadata loadMetadata, IndependentPixelOperation independentPixelOperation2, BlurService blurService2, ScaleService scaleService) {
        super(node, loadMetadata);
        this.independentPixelOperation = independentPixelOperation2;
        this.blurService = blurService2;
        this.scaleService = scaleService;
    }

    @Override
    public ReadOnlyClipImage createFrame(StatelessEffectRequest request) {
        ReadOnlyClipImage currentFrame = request.getCurrentFrame();
        ClipImage shadow = ClipImage.sameSizeAs(currentFrame);

        int shiftX = (int) (shiftXProvider.getValueAt(request.getEffectPosition()) * request.getScale());
        int shiftY = (int) (shiftYProvider.getValueAt(request.getEffectPosition()) * request.getScale());
        Color shadowColor = shadowColorProvider.getValueAt(request.getEffectPosition());
        int maximumAlpha = (int) (maximumAlphaProvider.getValueAt(request.getEffectPosition()) * 255.0);

        int blurX = (int) (shadowBlurXProvider.getValueAt(request.getEffectPosition()) * request.getScale());
        int blurY = (int) (shadowBlurYProvider.getValueAt(request.getEffectPosition()) * request.getScale());

        double shadowMultiplier = shadowMultiplierProvider.getValueAt(request.getEffectPosition());

        independentPixelOperation.executePixelTransformation(currentFrame.getWidth(), currentFrame.getHeight(), (x, y) -> {
            int originalX = x - shiftX;
            int originalY = y - shiftY;
            int newr = 0;
            int newg = 0;
            int newb = 0;
            int newa = 0;

            if (originalX >= 0 && originalY >= 0 && originalX < currentFrame.getWidth() && originalY < currentFrame.getHeight()) {
                int alphaToSet = currentFrame.getAlpha(originalX, originalY);

                if (alphaToSet > maximumAlpha) {
                    alphaToSet = maximumAlpha;
                }

                newr = (int) (shadowColor.red * 255.0);
                newg = (int) (shadowColor.green * 255.0);
                newb = (int) (shadowColor.blue * 255.0);
                newa = alphaToSet;
            }

            shadow.setRed(newr, x, y);
            shadow.setGreen(newg, x, y);
            shadow.setBlue(newb, x, y);
            shadow.setAlpha(newa, x, y);
        });

        double scale = scaleProvider.getValueAt(request.getEffectPosition());

        int shadowTranslateX;
        int shadowTranslateY;
        ClipImage shadowImage;
        if (!MathUtil.fuzzyEquals(scale, 1.0)) {

            ScaleRequest scaleRequest = ScaleRequest.builder()
                    .withImage(shadow)
                    .withNewWidth((int) (shadow.getWidth() * scale))
                    .withNewHeight((int) (shadow.getHeight() * scale))
                    .build();

            ClipImage scaledImage = scaleService.createScaledImage(scaleRequest);

            BlurRequest blurRequest = BlurRequest.builder()
                    .withImage(scaledImage)
                    .withKernelWidth(blurX)
                    .withKernelHeight(blurY)
                    .build();

            shadowImage = blurService.createBlurredImage(blurRequest);

            GlobalMemoryManagerAccessor.memoryManager.returnBuffer(shadow.getBuffer());
            GlobalMemoryManagerAccessor.memoryManager.returnBuffer(scaledImage.getBuffer());

            shadowTranslateX = (shadowImage.getWidth() - currentFrame.getWidth()) / 2;
            shadowTranslateY = (shadowImage.getHeight() - currentFrame.getHeight()) / 2;
        } else {
            BlurRequest blurRequest = BlurRequest.builder()
                    .withImage(shadow)
                    .withKernelWidth(blurX)
                    .withKernelHeight(blurY)
                    .build();
            shadowImage = blurService.createBlurredImage(blurRequest);
            GlobalMemoryManagerAccessor.memoryManager.returnBuffer(shadow.getBuffer());
            shadowTranslateX = 0;
            shadowTranslateY = 0;
        }

        ClipImage result = ClipImage.sameSizeAs(currentFrame);

        independentPixelOperation.executePixelTransformation(currentFrame.getWidth(), currentFrame.getHeight(), (x, y) -> {
            int shadowX = x + shadowTranslateX;
            int shadowY = y + shadowTranslateY;

            int newr;
            int newg;
            int newb;
            int newa;
            if (shadowImage.inBounds(shadowX, shadowY)) {
                newr = shadowImage.getRed(shadowX, shadowY);
                newg = shadowImage.getGreen(shadowX, shadowY);
                newb = shadowImage.getBlue(shadowX, shadowY);
                newa = (int) (shadowImage.getAlpha(shadowX, shadowY) * shadowMultiplier);
            } else {
                newr = 0;
                newg = 0;
                newb = 0;
                newa = 0;
            }
            double originalAlpha = currentFrame.getAlpha(x, y) / 255.0;

            newr = (int) ((currentFrame.getRed(x, y) * originalAlpha) + (newr * (1.0 - originalAlpha)));
            newg = (int) ((currentFrame.getGreen(x, y) * originalAlpha) + (newg * (1.0 - originalAlpha)));
            newb = (int) ((currentFrame.getBlue(x, y) * originalAlpha) + (newb * (1.0 - originalAlpha)));
            newa = Math.max(newa, currentFrame.getAlpha(x, y));

            result.setRed(newr, x, y);
            result.setGreen(newg, x, y);
            result.setBlue(newb, x, y);
            result.setAlpha(newa, x, y);
        });

        return result;
    }

    @Override
    public void initializeValueProvider() {
        shadowBlurXProvider = new DoubleProvider(1, 100, new MultiKeyframeBasedDoubleInterpolator(40.0));
        shadowBlurYProvider = new DoubleProvider(1, 100, new MultiKeyframeBasedDoubleInterpolator(40.0));
        shadowMultiplierProvider = new DoubleProvider(0.1, 3, new MultiKeyframeBasedDoubleInterpolator(1.5));

        shiftXProvider = new DoubleProvider(-300, 300, new MultiKeyframeBasedDoubleInterpolator(20.0));
        shiftYProvider = new DoubleProvider(-300, 300, new MultiKeyframeBasedDoubleInterpolator(20.0));
        maximumAlphaProvider = new DoubleProvider(0, 1, new MultiKeyframeBasedDoubleInterpolator(0.7));
        scaleProvider = new DoubleProvider(0, 3, new MultiKeyframeBasedDoubleInterpolator(1.0));
        shadowColorProvider = createColorProvider(0, 0, 0);
    }

    @Override
    public List<ValueProviderDescriptor> getValueProviders() {

        ValueProviderDescriptor shadowBlurXProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(shadowBlurXProvider)
                .withName("horizontal blur")
                .build();
        ValueProviderDescriptor shadowBlurYProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(shadowBlurYProvider)
                .withName("vertical shift")
                .build();
        ValueProviderDescriptor shadowMultiplierProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(shadowMultiplierProvider)
                .withName("shadow multiplier")
                .build();
        ValueProviderDescriptor shiftXProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(shiftXProvider)
                .withName("horizontal shift")
                .build();
        ValueProviderDescriptor shiftYProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(shiftYProvider)
                .withName("vertical shift")
                .build();
        ValueProviderDescriptor scaleProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(scaleProvider)
                .withName("Scale")
                .build();
        ValueProviderDescriptor maximumAlphaProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(maximumAlphaProvider)
                .withName("Maximum alpha")
                .build();
        ValueProviderDescriptor shadowColorProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(shadowColorProvider)
                .withName("Shadow color")
                .build();

        return List.of(shadowBlurXProviderDescriptor, shadowBlurYProviderDescriptor, shadowMultiplierProviderDescriptor, shiftXProviderDescriptor, shiftYProviderDescriptor,
                scaleProviderDescriptor, maximumAlphaProviderDescriptor, shadowColorProviderDescriptor);
    }

    private ColorProvider createColorProvider(double r, double g, double b) {
        return new ColorProvider(new DoubleProvider(new MultiKeyframeBasedDoubleInterpolator(r)),
                new DoubleProvider(new MultiKeyframeBasedDoubleInterpolator(g)),
                new DoubleProvider(new MultiKeyframeBasedDoubleInterpolator(b)));
    }

    @Override
    public StatelessEffect cloneEffect(CloneRequestMetadata cloneRequestMetadata) {
        return new DropShadowEffect(this, cloneRequestMetadata);
    }

}
