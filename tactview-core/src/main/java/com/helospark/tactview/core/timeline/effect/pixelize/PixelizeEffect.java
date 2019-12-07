package com.helospark.tactview.core.timeline.effect.pixelize;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.decoder.framecache.GlobalMemoryManagerAccessor;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.StatelessVideoEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.util.IndependentPixelOperation;
import com.helospark.tactview.core.util.ReflectionUtil;

public class PixelizeEffect extends StatelessVideoEffect {
    private DoubleProvider pixelWidthProvider;
    private DoubleProvider pixelHeightProvider;

    private IndependentPixelOperation independentPixelOperation;

    public PixelizeEffect(TimelineInterval interval, IndependentPixelOperation independentPixelOperation) {
        super(interval);
        this.independentPixelOperation = independentPixelOperation;
    }

    public PixelizeEffect(PixelizeEffect cloneFrom, CloneRequestMetadata cloneRequestMetadata) {
        super(cloneFrom, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(cloneFrom, this);
    }

    public PixelizeEffect(JsonNode node, LoadMetadata loadMetadata, IndependentPixelOperation independentPixelOperation2) {
        super(node, loadMetadata);
        this.independentPixelOperation = independentPixelOperation2;
    }

    @Override
    public ReadOnlyClipImage createFrame(StatelessEffectRequest request) {
        ReadOnlyClipImage currentFrame = request.getCurrentFrame();
        int pixelWidth = (int) (pixelWidthProvider.getValueAt(request.getEffectPosition()) * currentFrame.getWidth());
        int pixelHeight = (int) (pixelHeightProvider.getValueAt(request.getEffectPosition()) * currentFrame.getHeight());

        if (pixelHeight < 0 || pixelWidth < 0) {
            return independentPixelOperation.createNewImageWithAppliedTransformation(currentFrame, pixelRequest -> {
                pixelRequest.output = pixelRequest.input;
            });
        }

        int tempImageWidth = (int) Math.ceil((double) currentFrame.getWidth() / pixelWidth);
        int tempImageHeight = (int) Math.ceil((double) currentFrame.getHeight() / pixelHeight);

        ClipImage tempBuffer = ClipImage.fromSize(tempImageWidth, tempImageHeight);

        independentPixelOperation.executePixelTransformation(tempImageWidth, tempImageHeight, (x, y) -> {
            int originalPixelX = x * pixelWidth;
            int originalPixelY = y * pixelHeight;

            int sumR = 0;
            int sumG = 0;
            int sumB = 0;
            int sumA = 0;
            int pixelCount = 0;

            for (int yIndex = originalPixelY - pixelHeight; yIndex < originalPixelY + pixelHeight; ++yIndex) {
                for (int xIndex = originalPixelX - pixelWidth; xIndex < originalPixelX + pixelWidth; ++xIndex) {

                    if (xIndex >= 0 && xIndex < currentFrame.getWidth() && yIndex >= 0 && yIndex < currentFrame.getHeight()) {
                        sumR += currentFrame.getRed(xIndex, yIndex);
                        sumG += currentFrame.getGreen(xIndex, yIndex);
                        sumB += currentFrame.getBlue(xIndex, yIndex);
                        sumA += currentFrame.getAlpha(xIndex, yIndex);
                        ++pixelCount;
                    }
                }
            }

            tempBuffer.setRed(sumR / pixelCount, x, y);
            tempBuffer.setGreen(sumG / pixelCount, x, y);
            tempBuffer.setBlue(sumB / pixelCount, x, y);
            tempBuffer.setAlpha(sumA / pixelCount, x, y);
        });

        ClipImage result = ClipImage.sameSizeAs(currentFrame);

        independentPixelOperation.executePixelTransformation(currentFrame.getWidth(), currentFrame.getHeight(), (x, y) -> {
            int xInTmpImage = x / pixelWidth;
            int yInTmpImage = y / pixelHeight;
            int red = tempBuffer.getRed(xInTmpImage, yInTmpImage);
            int green = tempBuffer.getGreen(xInTmpImage, yInTmpImage);
            int blue = tempBuffer.getBlue(xInTmpImage, yInTmpImage);
            int alpha = tempBuffer.getAlpha(xInTmpImage, yInTmpImage);

            result.setRed(red, x, y);
            result.setGreen(green, x, y);
            result.setBlue(blue, x, y);
            result.setAlpha(alpha, x, y);
        });

        GlobalMemoryManagerAccessor.memoryManager.returnBuffer(tempBuffer.getBuffer());

        return result;
    }

    @Override
    protected void initializeValueProviderInternal() {
        pixelWidthProvider = new DoubleProvider(1.0 / 4000, 0.2, new MultiKeyframeBasedDoubleInterpolator(0.1));
        pixelHeightProvider = new DoubleProvider(1.0 / 4000, 0.2, new MultiKeyframeBasedDoubleInterpolator(0.1));
    }

    @Override
    protected List<ValueProviderDescriptor> getValueProvidersInternal() {

        ValueProviderDescriptor pixelWidthDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(pixelWidthProvider)
                .withName("Pixel width")
                .build();
        ValueProviderDescriptor pixelHeightDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(pixelHeightProvider)
                .withName("Pixel height")
                .build();

        return List.of(pixelWidthDescriptor, pixelHeightDescriptor);
    }

    @Override
    public StatelessEffect cloneEffect(CloneRequestMetadata cloneRequestMetadata) {
        return new PixelizeEffect(this, cloneRequestMetadata);
    }

}
