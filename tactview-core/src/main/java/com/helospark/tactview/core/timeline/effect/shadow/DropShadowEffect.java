package com.helospark.tactview.core.timeline.effect.shadow;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.decoder.framecache.GlobalMemoryManagerAccessor;
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
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.util.IndependentPixelOperation;
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

    private BlurService blurService;

    public DropShadowEffect(TimelineInterval interval, IndependentPixelOperation independentPixelOperation, BlurService blurService) {
        super(interval);
        this.independentPixelOperation = independentPixelOperation;
        this.blurService = blurService;
    }

    public DropShadowEffect(DropShadowEffect dropShadowEffect) {
        super(dropShadowEffect);
        ReflectionUtil.copyOrCloneFieldFromTo(dropShadowEffect, this);
    }

    public DropShadowEffect(JsonNode node, IndependentPixelOperation independentPixelOperation2, BlurService blurService2) {
        super(node);
        this.independentPixelOperation = independentPixelOperation2;
        this.blurService = blurService2;
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

        BlurRequest blurRequest = BlurRequest.builder()
                .withImage(shadow)
                .withKernelWidth(blurX)
                .withKernelHeight(blurY)
                .build();

        ClipImage result = blurService.createBlurredImage(blurRequest);
        GlobalMemoryManagerAccessor.memoryManager.returnBuffer(shadow.getBuffer());

        independentPixelOperation.executePixelTransformation(currentFrame.getWidth(), currentFrame.getHeight(), (x, y) -> {
            int newr = result.getRed(x, y);
            int newg = result.getGreen(x, y);
            int newb = result.getBlue(x, y);
            int newa = (int) (result.getAlpha(x, y) * shadowMultiplier);

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

        shiftXProvider = new DoubleProvider(1, 300, new MultiKeyframeBasedDoubleInterpolator(20.0));
        shiftYProvider = new DoubleProvider(1, 300, new MultiKeyframeBasedDoubleInterpolator(20.0));
        maximumAlphaProvider = new DoubleProvider(0, 1, new MultiKeyframeBasedDoubleInterpolator(0.7));
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
        ValueProviderDescriptor maximumAlphaProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(maximumAlphaProvider)
                .withName("Maximum alpha")
                .build();
        ValueProviderDescriptor shadowColorProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(shadowColorProvider)
                .withName("Shadow color")
                .build();

        return List.of(shadowBlurXProviderDescriptor, shadowBlurYProviderDescriptor, shadowMultiplierProviderDescriptor, shiftXProviderDescriptor, shiftYProviderDescriptor,
                maximumAlphaProviderDescriptor, shadowColorProviderDescriptor);
    }

    private ColorProvider createColorProvider(double r, double g, double b) {
        return new ColorProvider(new DoubleProvider(new MultiKeyframeBasedDoubleInterpolator(r)),
                new DoubleProvider(new MultiKeyframeBasedDoubleInterpolator(g)),
                new DoubleProvider(new MultiKeyframeBasedDoubleInterpolator(b)));
    }

    @Override
    public StatelessEffect cloneEffect() {
        return new DropShadowEffect(this);
    }

}
