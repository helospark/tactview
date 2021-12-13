package com.helospark.tactview.core.timeline.proceduralclip.text;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.decoder.ImageMetadata;
import com.helospark.tactview.core.decoder.VisualMediaMetadata;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.GetFrameRequest;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.BooleanProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.IntegerProvider;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.util.BufferedImageToClipFrameResultConverter;
import com.helospark.tactview.core.util.ReflectionUtil;

public class StopWatchTextProceduralClip extends AbstractTextProceduralClip {
    private IntegerProvider offsetMillisecondsProvider;
    private BooleanProvider includeMillisecondsProvider;

    public StopWatchTextProceduralClip(VisualMediaMetadata visualMediaMetadata, TimelineInterval interval,
            BufferedImageToClipFrameResultConverter bufferedImageToClipFrameResultConverter, FontLoader fontLoader) {
        super(visualMediaMetadata, interval, bufferedImageToClipFrameResultConverter, fontLoader);
    }

    public StopWatchTextProceduralClip(ImageMetadata metadata, JsonNode node, LoadMetadata loadMetadata, BufferedImageToClipFrameResultConverter bufferedImageToClipFrameResultConverter2,
            FontLoader fontLoader) {
        super(metadata, node, loadMetadata, bufferedImageToClipFrameResultConverter2, fontLoader);
    }

    public StopWatchTextProceduralClip(StopWatchTextProceduralClip textProceduralClip, CloneRequestMetadata cloneRequestMetadata) {
        super(textProceduralClip, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(textProceduralClip, this, cloneRequestMetadata);
    }

    @Override
    public ReadOnlyClipImage createProceduralFrame(GetFrameRequest request, TimelinePosition relativePosition) {
        int offsetMillis = offsetMillisecondsProvider.getValueAt(relativePosition);

        BigDecimal secondsToMilliseconds = new BigDecimal(1000);

        BigDecimal position = new BigDecimal(offsetMillis).add(relativePosition.getSeconds().multiply(secondsToMilliseconds));

        Duration duration = Duration.ofMillis(position.longValue());

        int hours = duration.toHoursPart();
        int minutes = duration.toMinutesPart();
        int seconds = duration.toSecondsPart();
        int millis = duration.toMillisPart();

        String template = "%d:%02d:%02d";
        if (includeMillisecondsProvider.getValueAt(relativePosition)) {
            template += ".%03d";
        }

        String currentText = String.format(template, hours, minutes, seconds, millis);

        return super.drawText(request, relativePosition, currentText);
    }

    @Override
    protected void initializeValueProvider() {
        super.initializeValueProvider();

        offsetMillisecondsProvider = new IntegerProvider(new MultiKeyframeBasedDoubleInterpolator(0.0));
        includeMillisecondsProvider = new BooleanProvider(new MultiKeyframeBasedDoubleInterpolator(1.0));
    }

    @Override
    public List<ValueProviderDescriptor> getDescriptorsInternal() {
        List<ValueProviderDescriptor> result = super.getDescriptorsInternal();

        ValueProviderDescriptor offsetMillisecondsProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(offsetMillisecondsProvider)
                .withName("Offset milliseconds")
                .build();
        ValueProviderDescriptor includeMillisecondsProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(includeMillisecondsProvider)
                .withName("Include milliseconds")
                .build();

        result.add(offsetMillisecondsProviderDescriptor);
        result.add(includeMillisecondsProviderDescriptor);

        return result;
    }

    @Override
    public TimelineClip cloneClip(CloneRequestMetadata cloneRequestMetadata) {
        return new StopWatchTextProceduralClip(this, cloneRequestMetadata);
    }

}
