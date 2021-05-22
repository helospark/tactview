package com.helospark.tactview.core.timeline.effect.greenscreen;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.StatelessVideoEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;
import com.helospark.tactview.core.timeline.effect.greenscreen.opencv.OpenCVGreenScreenImplementation;
import com.helospark.tactview.core.timeline.effect.greenscreen.opencv.OpenCVGreenScreenRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.DoubleRange;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.BooleanProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleRangeProvider;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.util.ReflectionUtil;

public class GreenScreenEffect extends StatelessVideoEffect {
    private OpenCVGreenScreenImplementation implementation;

    private DoubleRangeProvider hueRangeProvider;
    private DoubleRangeProvider saturationRangeProvider;
    private DoubleRangeProvider valueRangeProvider;

    private BooleanProvider spillRemovalEnabled;
    private DoubleProvider spillDeltaHueThresholdProvider;
    private DoubleProvider spillSaturationThresholdProvider;
    private DoubleProvider spillValueThresholdProvider;

    private BooleanProvider edgeToAlpha;
    private DoubleProvider edgeToAlphaRadius;

    public GreenScreenEffect(TimelineInterval interval, OpenCVGreenScreenImplementation implementation) {
        super(interval);
        this.implementation = implementation;
    }

    public GreenScreenEffect(GreenScreenEffect cloneFrom, CloneRequestMetadata cloneRequestMetadata) {
        super(cloneFrom, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(cloneFrom, this);
    }

    public GreenScreenEffect(JsonNode node, LoadMetadata loadMetadata, OpenCVGreenScreenImplementation openCVGreenScreenImplementation) {
        super(node, loadMetadata);
        this.implementation = openCVGreenScreenImplementation;
    }

    @Override
    public ReadOnlyClipImage createFrame(StatelessEffectRequest request) {
        ReadOnlyClipImage currentFrame = request.getCurrentFrame();
        ClipImage result = ClipImage.sameSizeAs(currentFrame);

        OpenCVGreenScreenRequest nativeRequest = new OpenCVGreenScreenRequest();
        nativeRequest.input = currentFrame.getBuffer();
        nativeRequest.output = result.getBuffer();
        nativeRequest.width = currentFrame.getWidth();
        nativeRequest.height = currentFrame.getHeight();

        DoubleRange hueRange = hueRangeProvider.getValueAt(request.getEffectPosition());
        DoubleRange saturationRange = saturationRangeProvider.getValueAt(request.getEffectPosition());
        DoubleRange valueRange = valueRangeProvider.getValueAt(request.getEffectPosition());

        nativeRequest.hueMin = (int) hueRange.lowEnd;
        nativeRequest.hueMax = (int) hueRange.highEnd;

        nativeRequest.saturationMin = (int) saturationRange.lowEnd;
        nativeRequest.saturationMax = (int) saturationRange.highEnd;

        nativeRequest.valueMin = (int) valueRange.lowEnd;
        nativeRequest.valueMax = (int) valueRange.highEnd;

        nativeRequest.spillRemovalEnabled = spillRemovalEnabled.getValueAt(request.getEffectPosition()) ? 1 : 0;
        nativeRequest.spillDeltaHue = spillDeltaHueThresholdProvider.getValueAt(request.getEffectPosition()).intValue();
        nativeRequest.spillSaturationThreshold = spillSaturationThresholdProvider.getValueAt(request.getEffectPosition()).intValue();
        nativeRequest.spillValueThreshold = spillValueThresholdProvider.getValueAt(request.getEffectPosition()).intValue();

        nativeRequest.enableEdgeBlur = edgeToAlpha.getValueAt(request.getEffectPosition()) ? 1 : 0;
        nativeRequest.edgeBlurRadius = (int) (edgeToAlphaRadius.getValueAt(request.getEffectPosition()) * request.getScale());

        implementation.greenScreen(nativeRequest);

        return result;
    }

    @Override
    protected void initializeValueProviderInternal() {
        hueRangeProvider = DoubleRangeProvider.createDefaultDoubleRangeProvider(0, 255, 50, 80);
        saturationRangeProvider = DoubleRangeProvider.createDefaultDoubleRangeProvider(0, 255, 30, 255);
        valueRangeProvider = DoubleRangeProvider.createDefaultDoubleRangeProvider(0, 255, 0, 255);

        spillRemovalEnabled = new BooleanProvider(new MultiKeyframeBasedDoubleInterpolator(1.0));
        spillDeltaHueThresholdProvider = new DoubleProvider(0.0, 30.0, new MultiKeyframeBasedDoubleInterpolator(10.0));
        spillSaturationThresholdProvider = new DoubleProvider(0.0, 255.0, new MultiKeyframeBasedDoubleInterpolator(30.0));
        spillValueThresholdProvider = new DoubleProvider(0.0, 255.0, new MultiKeyframeBasedDoubleInterpolator(30.0));

        edgeToAlpha = new BooleanProvider(new MultiKeyframeBasedDoubleInterpolator(1.0));
        edgeToAlphaRadius = new DoubleProvider(1, 30, new MultiKeyframeBasedDoubleInterpolator(5.0));
    }

    @Override
    protected List<ValueProviderDescriptor> getValueProvidersInternal() {
        ValueProviderDescriptor hueProviderDescriptor = ValueProviderDescriptor.builder()
                .withName("Hue range")
                .withKeyframeableEffect(hueRangeProvider)
                .withGroup("color")
                .build();
        ValueProviderDescriptor saturationProviderDescriptor = ValueProviderDescriptor.builder()
                .withName("Saturation range")
                .withKeyframeableEffect(saturationRangeProvider)
                .withGroup("color")
                .build();
        ValueProviderDescriptor valueProviderDescriptor = ValueProviderDescriptor.builder()
                .withName("Value range")
                .withKeyframeableEffect(valueRangeProvider)
                .withGroup("color")
                .build();

        ValueProviderDescriptor spillRemovalEnabledDescriptor = ValueProviderDescriptor.builder()
                .withName("Spill removal enabled")
                .withKeyframeableEffect(spillRemovalEnabled)
                .withGroup("spill")
                .build();
        ValueProviderDescriptor spillDeltaHueThresholdDescriptor = ValueProviderDescriptor.builder()
                .withName("Spill delta hue")
                .withKeyframeableEffect(spillDeltaHueThresholdProvider)
                .withGroup("spill")
                .build();
        ValueProviderDescriptor spillSaturationThresholdDescriptor = ValueProviderDescriptor.builder()
                .withName("Spill saturation threshold")
                .withKeyframeableEffect(spillSaturationThresholdProvider)
                .withGroup("spill")
                .build();
        ValueProviderDescriptor spillValueThresholdDescriptor = ValueProviderDescriptor.builder()
                .withName("Spill value threshold")
                .withKeyframeableEffect(spillValueThresholdProvider)
                .withGroup("spill")
                .build();

        ValueProviderDescriptor edgeToAlphaDescriptor = ValueProviderDescriptor.builder()
                .withName("Edge blur enabled")
                .withKeyframeableEffect(edgeToAlpha)
                .withGroup("edge blur")
                .build();
        ValueProviderDescriptor edgeBlurRadiusDescriptor = ValueProviderDescriptor.builder()
                .withName("Edge blur radius")
                .withEnabledIf(position -> edgeToAlpha.getValueAt(position))
                .withKeyframeableEffect(edgeToAlphaRadius)
                .withGroup("edge blur")
                .build();

        return List.of(hueProviderDescriptor, saturationProviderDescriptor, valueProviderDescriptor,
                spillRemovalEnabledDescriptor, spillDeltaHueThresholdDescriptor, spillSaturationThresholdDescriptor, spillValueThresholdDescriptor,
                edgeToAlphaDescriptor, edgeBlurRadiusDescriptor);
    }

    @Override
    public StatelessEffect cloneEffect(CloneRequestMetadata cloneRequestMetadata) {
        return new GreenScreenEffect(this, cloneRequestMetadata);
    }

}
