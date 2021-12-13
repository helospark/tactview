package com.helospark.tactview.core.timeline.proceduralclip.text;

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
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.StepStringInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.StringProvider;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.util.BufferedImageToClipFrameResultConverter;
import com.helospark.tactview.core.util.ReflectionUtil;

public class TextProceduralClip extends AbstractTextProceduralClip {
    private StringProvider textProvider;

    public TextProceduralClip(VisualMediaMetadata visualMediaMetadata, TimelineInterval interval,
            BufferedImageToClipFrameResultConverter bufferedImageToClipFrameResultConverter, FontLoader fontLoader) {
        super(visualMediaMetadata, interval, bufferedImageToClipFrameResultConverter, fontLoader);
    }

    public TextProceduralClip(ImageMetadata metadata, JsonNode node, LoadMetadata loadMetadata, BufferedImageToClipFrameResultConverter bufferedImageToClipFrameResultConverter2,
            FontLoader fontLoader) {
        super(metadata, node, loadMetadata, bufferedImageToClipFrameResultConverter2, fontLoader);
    }

    public TextProceduralClip(TextProceduralClip textProceduralClip, CloneRequestMetadata cloneRequestMetadata) {
        super(textProceduralClip, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(textProceduralClip, this, cloneRequestMetadata);
    }

    @Override
    public ReadOnlyClipImage createProceduralFrame(GetFrameRequest request, TimelinePosition relativePosition) {
        String currentText = textProvider.getValueAt(relativePosition);
        return super.drawText(request, relativePosition, currentText);
    }

    @Override
    protected void initializeValueProvider() {
        super.initializeValueProvider();

        textProvider = new StringProvider(new StepStringInterpolator());
    }

    @Override
    public List<ValueProviderDescriptor> getDescriptorsInternal() {
        List<ValueProviderDescriptor> result = super.getDescriptorsInternal();

        ValueProviderDescriptor textDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(textProvider)
                .withName("Text")
                .build();

        result.add(textDescriptor);

        return result;
    }

    @Override
    public TimelineClip cloneClip(CloneRequestMetadata cloneRequestMetadata) {
        return new TextProceduralClip(this, cloneRequestMetadata);
    }

}
