package com.helospark.tactview.core.timeline.effect.erodedilate;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.StatelessVideoEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;
import com.helospark.tactview.core.timeline.effect.erodedilate.opencv.OpenCVErodeDilateImplementation;
import com.helospark.tactview.core.timeline.effect.erodedilate.opencv.OpenCVErodeDilateRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.StepStringInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ValueListElement;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ValueListProvider;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.util.ReflectionUtil;

public class ErodeDilateEffect extends StatelessVideoEffect {
    private DoubleProvider kernelWidthProvider;
    private DoubleProvider kernelHeightProvider;
    private ValueListProvider<ValueListElement> erodeOrDelodeProvider;
    private ValueListProvider<IdIdableValueListElement> shapeProvider;

    private OpenCVErodeDilateImplementation implementation;

    public ErodeDilateEffect(TimelineInterval interval, OpenCVErodeDilateImplementation implementation) {
        super(interval);
        this.implementation = implementation;
    }

    public ErodeDilateEffect(ErodeDilateEffect cloneFrom, CloneRequestMetadata cloneRequestMetadata) {
        super(cloneFrom, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(cloneFrom, this, cloneRequestMetadata);
    }

    public ErodeDilateEffect(JsonNode node, LoadMetadata loadMetadata, OpenCVErodeDilateImplementation openCVErodeDilateImplementation) {
        super(node, loadMetadata);
        this.implementation = openCVErodeDilateImplementation;
    }

    @Override
    public ReadOnlyClipImage createFrame(StatelessEffectRequest request) {
        ReadOnlyClipImage currentFrame = request.getCurrentFrame();
        ClipImage result = ClipImage.sameSizeAs(currentFrame);
        int kernelWidth = (int) (kernelWidthProvider.getValueAt(request.getEffectPosition(), request.getEvaluationContext()) * currentFrame.getWidth());
        int kernelHeight = (int) (kernelHeightProvider.getValueAt(request.getEffectPosition(), request.getEvaluationContext()) * currentFrame.getHeight());
        boolean erode = erodeOrDelodeProvider.getValueAt(request.getEffectPosition(), request.getEvaluationContext()).getId().equals("erode");
        int shape = shapeProvider.getValueAt(request.getEffectPosition(), request.getEvaluationContext()).intId;

        OpenCVErodeDilateRequest nativeRequest = new OpenCVErodeDilateRequest();
        nativeRequest.width = currentFrame.getWidth();
        nativeRequest.height = currentFrame.getHeight();
        nativeRequest.input = currentFrame.getBuffer();
        nativeRequest.output = result.getBuffer();
        nativeRequest.kernelWidth = kernelWidth;
        nativeRequest.kernelHeight = kernelHeight;
        nativeRequest.erode = erode;
        nativeRequest.shape = shape;

        implementation.erodeDilate(nativeRequest);

        return result;
    }

    @Override
    protected void initializeValueProviderInternal() {
        kernelWidthProvider = new DoubleProvider(1 / 4000.0, 0.1, new MultiKeyframeBasedDoubleInterpolator(10.0 / 1920));
        kernelHeightProvider = new DoubleProvider(1 / 4000.0, 0.1, new MultiKeyframeBasedDoubleInterpolator(10.0 / 1080));
        erodeOrDelodeProvider = new ValueListProvider<>(createErodeOrDilate(), new StepStringInterpolator("dilate"));
        shapeProvider = new ValueListProvider<>(createShape(), new StepStringInterpolator("ellipse"));
    }

    @Override
    protected List<ValueProviderDescriptor> getValueProvidersInternal() {

        ValueProviderDescriptor kernelWidthDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(kernelWidthProvider)
                .withName("Kernel width")
                .build();
        ValueProviderDescriptor kernelHeightDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(kernelHeightProvider)
                .withName("Kernel height")
                .build();
        ValueProviderDescriptor typeDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(erodeOrDelodeProvider)
                .withName("Operation")
                .build();
        ValueProviderDescriptor shapeDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(shapeProvider)
                .withName("Shape")
                .build();

        return List.of(kernelWidthDescriptor, kernelHeightDescriptor, typeDescriptor, shapeDescriptor);
    }

    private List<IdIdableValueListElement> createShape() {
        IdIdableValueListElement cross = new IdIdableValueListElement("cross", "cross", 0);
        IdIdableValueListElement rectangle = new IdIdableValueListElement("rectangle", "rectangle", 1);
        IdIdableValueListElement ellipse = new IdIdableValueListElement("ellipse", "ellipse", 2);

        return List.of(cross, rectangle, ellipse);
    }

    private List<ValueListElement> createErodeOrDilate() {
        ValueListElement erode = new ValueListElement("erode", "erode");
        ValueListElement dilate = new ValueListElement("dilate", "dilate");
        return List.of(erode, dilate);
    }

    static class IdIdableValueListElement extends ValueListElement {
        private Integer intId;

        public IdIdableValueListElement(String id, String text, Integer intId) {
            super(id, text);
            this.intId = intId;
        }

    }

    @Override
    public StatelessEffect cloneEffect(CloneRequestMetadata cloneRequestMetadata) {
        return new ErodeDilateEffect(this, cloneRequestMetadata);
    }

}
