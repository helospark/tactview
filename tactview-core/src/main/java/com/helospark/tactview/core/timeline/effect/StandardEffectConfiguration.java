package com.helospark.tactview.core.timeline.effect;

import static com.helospark.tactview.core.util.conditional.TactviewPlatform.LINUX;

import java.util.List;

import com.helospark.lightdi.annotation.Bean;
import com.helospark.lightdi.annotation.Configuration;
import com.helospark.lightdi.annotation.Value;
import com.helospark.tactview.core.repository.ProjectRepository;
import com.helospark.tactview.core.timeline.TimelineClipType;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelineLength;
import com.helospark.tactview.core.timeline.blendmode.BlendModeStrategy;
import com.helospark.tactview.core.timeline.effect.blend.BlendEffect;
import com.helospark.tactview.core.timeline.effect.blur.BlurEffect;
import com.helospark.tactview.core.timeline.effect.blur.BlurService;
import com.helospark.tactview.core.timeline.effect.blur.LinearBlurEffect;
import com.helospark.tactview.core.timeline.effect.blur.RadialBlurEffect;
import com.helospark.tactview.core.timeline.effect.blur.opencv.OpenCVBasedGaussianBlur;
import com.helospark.tactview.core.timeline.effect.blur.service.LinearBlurService;
import com.helospark.tactview.core.timeline.effect.blur.service.RadialBlurService;
import com.helospark.tactview.core.timeline.effect.cartoon.CartoonEffect;
import com.helospark.tactview.core.timeline.effect.cartoon.opencv.OpenCVCartoonEffectImplementation;
import com.helospark.tactview.core.timeline.effect.colorchannelchange.ColorChannelChangeEffect;
import com.helospark.tactview.core.timeline.effect.colorchannelchange.FloodFillEffect;
import com.helospark.tactview.core.timeline.effect.colorize.AutoWhiteBalanceEffect;
import com.helospark.tactview.core.timeline.effect.colorize.ColorBalanceEffect;
import com.helospark.tactview.core.timeline.effect.colorize.ColorTemperatureService;
import com.helospark.tactview.core.timeline.effect.colorize.ColorizeEffect;
import com.helospark.tactview.core.timeline.effect.colorize.ColorizeService;
import com.helospark.tactview.core.timeline.effect.colorize.CurvesEffect;
import com.helospark.tactview.core.timeline.effect.colorize.MaximumRgbEffect;
import com.helospark.tactview.core.timeline.effect.contractbrightness.BrightnessContrassEffect;
import com.helospark.tactview.core.timeline.effect.contractbrightness.BrignessContrastService;
import com.helospark.tactview.core.timeline.effect.crop.CropEffect;
import com.helospark.tactview.core.timeline.effect.denoise.DenoiseEffect;
import com.helospark.tactview.core.timeline.effect.denoise.opencv.OpenCVBasedDenoiseEffect;
import com.helospark.tactview.core.timeline.effect.desaturize.DesaturizeEffect;
import com.helospark.tactview.core.timeline.effect.desaturize.ExclusiveDesaturizeEffect;
import com.helospark.tactview.core.timeline.effect.displacementmap.DisplacementMapEffect;
import com.helospark.tactview.core.timeline.effect.displacementmap.service.DisplacementMapService;
import com.helospark.tactview.core.timeline.effect.distort.GlassTilesEffect;
import com.helospark.tactview.core.timeline.effect.distort.LensDistortEffect;
import com.helospark.tactview.core.timeline.effect.distort.MagnifierEffect;
import com.helospark.tactview.core.timeline.effect.distort.PerturbationDistortEffect;
import com.helospark.tactview.core.timeline.effect.distort.PolarCoordinateEffect;
import com.helospark.tactview.core.timeline.effect.distort.ShearEffect;
import com.helospark.tactview.core.timeline.effect.distort.impl.OpenCVBasedLensDistort;
import com.helospark.tactview.core.timeline.effect.distort.service.PolarService;
import com.helospark.tactview.core.timeline.effect.edgedetect.EdgeDetectEffect;
import com.helospark.tactview.core.timeline.effect.edgedetect.opencv.OpenCVEdgeDetectImplementation;
import com.helospark.tactview.core.timeline.effect.erodedilate.ErodeDilateEffect;
import com.helospark.tactview.core.timeline.effect.erodedilate.opencv.OpenCVErodeDilateImplementation;
import com.helospark.tactview.core.timeline.effect.extend.ExtendClipWithBlurredImage;
import com.helospark.tactview.core.timeline.effect.extend.FrameExtendEffect;
import com.helospark.tactview.core.timeline.effect.framehold.FrameHoldEffect;
import com.helospark.tactview.core.timeline.effect.fun.AsciiArtEffect;
import com.helospark.tactview.core.timeline.effect.gamma.GammaEffect;
import com.helospark.tactview.core.timeline.effect.glow.LightGlowEffect;
import com.helospark.tactview.core.timeline.effect.graphing.DefaultGraphArrangementFactory;
import com.helospark.tactview.core.timeline.effect.graphing.GraphEffect;
import com.helospark.tactview.core.timeline.effect.greenscreen.GreenScreenEffect;
import com.helospark.tactview.core.timeline.effect.greenscreen.opencv.OpenCVGreenScreenImplementation;
import com.helospark.tactview.core.timeline.effect.histogramequization.HistogramEquizationEffect;
import com.helospark.tactview.core.timeline.effect.histogramequization.opencv.OpenCVHistogramEquizerImplementation;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.EffectGraphAccessorMessageSender;
import com.helospark.tactview.core.timeline.effect.invert.InvertEffect;
import com.helospark.tactview.core.timeline.effect.layermask.BezierMaskEffect;
import com.helospark.tactview.core.timeline.effect.layermask.LayerMaskAlphaCalculator;
import com.helospark.tactview.core.timeline.effect.layermask.LayerMaskApplier;
import com.helospark.tactview.core.timeline.effect.layermask.LayerMaskEffect;
import com.helospark.tactview.core.timeline.effect.layermask.PolygonMaskEffect;
import com.helospark.tactview.core.timeline.effect.layermask.impl.calculator.LayerMaskAlphaToAlpha;
import com.helospark.tactview.core.timeline.effect.levels.LevelsEffect;
import com.helospark.tactview.core.timeline.effect.lut.LutEffect;
import com.helospark.tactview.core.timeline.effect.lut.LutProviderService;
import com.helospark.tactview.core.timeline.effect.median.MedianEffect;
import com.helospark.tactview.core.timeline.effect.mirror.MirrorEffect;
import com.helospark.tactview.core.timeline.effect.mirror.MirrorLineEffect;
import com.helospark.tactview.core.timeline.effect.motionblur.GhostingEffect;
import com.helospark.tactview.core.timeline.effect.mozaic.MozaicEffect;
import com.helospark.tactview.core.timeline.effect.orthogonal.OrthogonalTransformationEffect;
import com.helospark.tactview.core.timeline.effect.pencil.PencilSketchEffect;
import com.helospark.tactview.core.timeline.effect.pencil.opencv.OpenCVPencilSketchImplementation;
import com.helospark.tactview.core.timeline.effect.pixelize.PixelizeEffect;
import com.helospark.tactview.core.timeline.effect.rotate.RotateEffect;
import com.helospark.tactview.core.timeline.effect.rotate.RotateService;
import com.helospark.tactview.core.timeline.effect.scale.ScaleEffect;
import com.helospark.tactview.core.timeline.effect.scale.ZoomEffect;
import com.helospark.tactview.core.timeline.effect.scale.service.ScaleService;
import com.helospark.tactview.core.timeline.effect.shadow.DirectionalShadowEffect;
import com.helospark.tactview.core.timeline.effect.shadow.DropShadowEffect;
import com.helospark.tactview.core.timeline.effect.sharpen.SharpenEffect;
import com.helospark.tactview.core.timeline.effect.sharpen.implementation.OpenCVSharpenImplementation;
import com.helospark.tactview.core.timeline.effect.stabilize.StabilizeVideoEffect;
import com.helospark.tactview.core.timeline.effect.stabilize.impl.OpenCVStabilizeVideoService;
import com.helospark.tactview.core.timeline.effect.television.TelevisionRgbLinesEffect;
import com.helospark.tactview.core.timeline.effect.threshold.AdaptiveThresholdEffect;
import com.helospark.tactview.core.timeline.effect.threshold.SimpleThresholdEffect;
import com.helospark.tactview.core.timeline.effect.threshold.opencv.OpenCVThresholdImplementation;
import com.helospark.tactview.core.timeline.effect.transform.SepiaEffect;
import com.helospark.tactview.core.timeline.effect.transform.service.GenericMatrixTransformationService;
import com.helospark.tactview.core.timeline.effect.vignette.VignetteEffect;
import com.helospark.tactview.core.timeline.effect.warp.RectangleWarpEffect;
import com.helospark.tactview.core.timeline.effect.warp.TrigonometricWrapEffect;
import com.helospark.tactview.core.timeline.effect.warp.rasterizer.Simple2DRasterizer;
import com.helospark.tactview.core.timeline.proceduralclip.noise.service.PerturbationNoiseService;
import com.helospark.tactview.core.timeline.proceduralclip.polygon.impl.PolygonRenderService;
import com.helospark.tactview.core.timeline.proceduralclip.polygon.impl.bezier.BezierPolygonRenderService;
import com.helospark.tactview.core.timeline.render.FrameExtender;
import com.helospark.tactview.core.util.BufferedImageToClipFrameResultConverter;
import com.helospark.tactview.core.util.ByteBufferToImageConverter;
import com.helospark.tactview.core.util.IndependentPixelOperation;
import com.helospark.tactview.core.util.conditional.ConditionalOnPlatform;
import com.helospark.tactview.core.util.messaging.MessagingService;

@Configuration
public class StandardEffectConfiguration {

    @Bean
    public StandardEffectFactory blurEffect(BlurService blurService, MessagingService messagingService) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new BlurEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(10000)), blurService))
                .withRestoreFactory((node, loadMetadata) -> new BlurEffect(node, loadMetadata, blurService))
                .withName("Gaussian blur")
                .withSupportedEffectId("gaussianblur")
                .withSupportedClipTypes(List.of(TimelineClipType.VIDEO, TimelineClipType.IMAGE))
                .withEffectType(TimelineEffectType.VIDEO_EFFECT)
                .build();
    }

    @Bean
    public StandardEffectFactory desaturizeEffect(OpenCVBasedGaussianBlur gaussianBlur, IndependentPixelOperation independentPixelOperations) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new DesaturizeEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000)), independentPixelOperations))
                .withRestoreFactory((node, loadMetadata) -> new DesaturizeEffect(node, loadMetadata, independentPixelOperations))
                .withName("Desaturize")
                .withSupportedEffectId("desaturize")
                .withSupportedClipTypes(List.of(TimelineClipType.VIDEO, TimelineClipType.IMAGE))
                .withEffectType(TimelineEffectType.VIDEO_EFFECT)
                .build();
    }

    @Bean
    public StandardEffectFactory scaleEffect(ScaleService scaleService) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new ScaleEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000)), scaleService))
                .withRestoreFactory((node, loadMetadata) -> new ScaleEffect(node, loadMetadata, scaleService))
                .withName("Scale")
                .withSupportedEffectId("scale")
                .withSupportedClipTypes(List.of(TimelineClipType.VIDEO, TimelineClipType.IMAGE))
                .withEffectType(TimelineEffectType.VIDEO_EFFECT)
                .build();
    }

    @Bean
    public StandardEffectFactory rotateEffect(RotateService rotateService) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new RotateEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000)), rotateService))
                .withRestoreFactory((node, loadMetadata) -> new RotateEffect(node, loadMetadata, rotateService))
                .withName("Rotate")
                .withSupportedEffectId("rotate")
                .withSupportedClipTypes(List.of(TimelineClipType.VIDEO, TimelineClipType.IMAGE))
                .withEffectType(TimelineEffectType.VIDEO_EFFECT)
                .build();
    }

    @Bean
    public StandardEffectFactory brightnessContrastEffect(BrignessContrastService brignessContrastService) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new BrightnessContrassEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000)), brignessContrastService))
                .withRestoreFactory((node, loadMetadata) -> new BrightnessContrassEffect(node, loadMetadata, brignessContrastService))
                .withName("Brightness")
                .withSupportedEffectId("brightesscontrast")
                .withSupportedClipTypes(List.of(TimelineClipType.VIDEO, TimelineClipType.IMAGE))
                .withEffectType(TimelineEffectType.VIDEO_EFFECT)
                .build();
    }

    @Bean
    public StandardEffectFactory gammaEffect(IndependentPixelOperation independentPixelOperations) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new GammaEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000)), independentPixelOperations))
                .withRestoreFactory((node, loadMetadata) -> new GammaEffect(node, loadMetadata, independentPixelOperations))
                .withName("Gamma")
                .withSupportedEffectId("gamma")
                .withSupportedClipTypes(List.of(TimelineClipType.VIDEO, TimelineClipType.IMAGE))
                .withEffectType(TimelineEffectType.VIDEO_EFFECT)
                .build();
    }

    @Bean
    public StandardEffectFactory invertEffect(IndependentPixelOperation independentPixelOperations) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new InvertEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000)), independentPixelOperations))
                .withRestoreFactory((node, loadMetadata) -> new InvertEffect(node, loadMetadata, independentPixelOperations))
                .withName("Invert")
                .withSupportedEffectId("invert")
                .withSupportedClipTypes(List.of(TimelineClipType.VIDEO, TimelineClipType.IMAGE))
                .withEffectType(TimelineEffectType.VIDEO_EFFECT)
                .build();
    }

    @Bean
    public StandardEffectFactory denoiseEffect(OpenCVBasedDenoiseEffect openCVBasedDenoiseEffect) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new DenoiseEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000)), openCVBasedDenoiseEffect))
                .withRestoreFactory((node, loadMetadata) -> new DenoiseEffect(node, loadMetadata, openCVBasedDenoiseEffect))
                .withName("Denoise")
                .withSupportedEffectId("denoise")
                .withSupportedClipTypes(List.of(TimelineClipType.VIDEO, TimelineClipType.IMAGE))
                .withEffectType(TimelineEffectType.VIDEO_EFFECT)
                .build();
    }

    @Bean
    public StandardEffectFactory adaptiveThresholdEffect(OpenCVThresholdImplementation openCVThresholdImplementation) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new AdaptiveThresholdEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000)), openCVThresholdImplementation))
                .withRestoreFactory((node, loadMetadata) -> new AdaptiveThresholdEffect(node, loadMetadata, openCVThresholdImplementation))
                .withName("Adaptive threshold")
                .withSupportedEffectId("adaptivethreshold")
                .withSupportedClipTypes(List.of(TimelineClipType.VIDEO, TimelineClipType.IMAGE))
                .withEffectType(TimelineEffectType.VIDEO_EFFECT)
                .build();
    }

    @Bean
    public StandardEffectFactory thresholdEffect(IndependentPixelOperation independentPixelOperation) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new SimpleThresholdEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000)), independentPixelOperation))
                .withRestoreFactory((node, loadMetadata) -> new SimpleThresholdEffect(node, loadMetadata, independentPixelOperation))
                .withName("Threshold")
                .withSupportedEffectId("threshold")
                .withSupportedClipTypes(List.of(TimelineClipType.VIDEO, TimelineClipType.IMAGE))
                .withEffectType(TimelineEffectType.VIDEO_EFFECT)
                .build();
    }

    @Bean
    public StandardEffectFactory mirrorEffect(IndependentPixelOperation independentPixelOperation) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new MirrorEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000)), independentPixelOperation))
                .withRestoreFactory((node, loadMetadata) -> new MirrorEffect(node, loadMetadata, independentPixelOperation))
                .withName("Mirror")
                .withSupportedEffectId("mirror")
                .withSupportedClipTypes(List.of(TimelineClipType.VIDEO, TimelineClipType.IMAGE))
                .withEffectType(TimelineEffectType.VIDEO_EFFECT)
                .build();
    }

    @Bean
    public StandardEffectFactory colorize(ColorizeService colorizeService) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new ColorizeEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000)), colorizeService))
                .withRestoreFactory((node, loadMetadata) -> new ColorizeEffect(node, loadMetadata, colorizeService))
                .withName("Colorize")
                .withSupportedEffectId("colorize")
                .withSupportedClipTypes(List.of(TimelineClipType.VIDEO, TimelineClipType.IMAGE))
                .withEffectType(TimelineEffectType.VIDEO_EFFECT)
                .build();
    }

    @Bean
    public StandardEffectFactory pixelize(IndependentPixelOperation independentPixelOperation) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new PixelizeEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000)), independentPixelOperation))
                .withRestoreFactory((node, loadMetadata) -> new PixelizeEffect(node, loadMetadata, independentPixelOperation))
                .withName("Pixelize")
                .withSupportedEffectId("pixelize")
                .withSupportedClipTypes(List.of(TimelineClipType.VIDEO, TimelineClipType.IMAGE))
                .withEffectType(TimelineEffectType.VIDEO_EFFECT)
                .build();
    }

    @Bean
    public StandardEffectFactory erodeDilate(OpenCVErodeDilateImplementation openCVErodeDilateImplementation) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new ErodeDilateEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000)), openCVErodeDilateImplementation))
                .withRestoreFactory((node, loadMetadata) -> new ErodeDilateEffect(node, loadMetadata, openCVErodeDilateImplementation))
                .withName("Erode/Dilate")
                .withSupportedEffectId("erodedilate")
                .withSupportedClipTypes(List.of(TimelineClipType.VIDEO, TimelineClipType.IMAGE))
                .withEffectType(TimelineEffectType.VIDEO_EFFECT)
                .build();
    }

    @Bean
    public StandardEffectFactory edgeDetect(OpenCVEdgeDetectImplementation openCVEdgeDetectImplementation) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new EdgeDetectEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000)), openCVEdgeDetectImplementation))
                .withRestoreFactory((node, loadMetadata) -> new EdgeDetectEffect(node, loadMetadata, openCVEdgeDetectImplementation))
                .withName("Edge detect")
                .withSupportedEffectId("edgedetect")
                .withSupportedClipTypes(List.of(TimelineClipType.VIDEO, TimelineClipType.IMAGE))
                .withEffectType(TimelineEffectType.VIDEO_EFFECT)
                .build();
    }

    @Bean
    public StandardEffectFactory greenScreen(OpenCVGreenScreenImplementation openCVGreenScreenImplementation) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new GreenScreenEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000)), openCVGreenScreenImplementation))
                .withRestoreFactory((node, loadMetadata) -> new GreenScreenEffect(node, loadMetadata, openCVGreenScreenImplementation))
                .withName("Green screen")
                .withSupportedEffectId("greenscreen")
                .withSupportedClipTypes(List.of(TimelineClipType.VIDEO, TimelineClipType.IMAGE))
                .withEffectType(TimelineEffectType.VIDEO_EFFECT)
                .build();
    }

    @Bean
    public StandardEffectFactory equizeHistogram(OpenCVHistogramEquizerImplementation implementation) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new HistogramEquizationEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000)), implementation))
                .withRestoreFactory((node, loadMetadata) -> new HistogramEquizationEffect(node, loadMetadata, implementation))
                .withName("Equize histogram")
                .withSupportedEffectId("equize histogram")
                .withSupportedClipTypes(List.of(TimelineClipType.VIDEO, TimelineClipType.IMAGE))
                .withEffectType(TimelineEffectType.VIDEO_EFFECT)
                .build();
    }

    @Bean
    public StandardEffectFactory cartoonEffect(OpenCVCartoonEffectImplementation implementation) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new CartoonEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000)), implementation))
                .withRestoreFactory((node, loadMetadata) -> new CartoonEffect(node, loadMetadata, implementation))
                .withName("Cartoon")
                .withSupportedEffectId("cartoon")
                .withSupportedClipTypes(List.of(TimelineClipType.VIDEO, TimelineClipType.IMAGE))
                .withEffectType(TimelineEffectType.VIDEO_EFFECT)
                .build();
    }

    @Bean
    public StandardEffectFactory pencilSketch(OpenCVPencilSketchImplementation implementation) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new PencilSketchEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000)), implementation))
                .withRestoreFactory((node, loadMetadata) -> new PencilSketchEffect(node, loadMetadata, implementation))
                .withName("Pencil")
                .withSupportedEffectId("pencil")
                .withSupportedClipTypes(List.of(TimelineClipType.VIDEO, TimelineClipType.IMAGE))
                .withEffectType(TimelineEffectType.VIDEO_EFFECT)
                .build();
    }

    @Bean
    public StandardEffectFactory warpEffect(IndependentPixelOperation independentPixelOperation) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new TrigonometricWrapEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000)), independentPixelOperation))
                .withRestoreFactory((node, loadMetadata) -> new TrigonometricWrapEffect(node, loadMetadata, independentPixelOperation))
                .withName("Warp")
                .withSupportedEffectId("warp")
                .withSupportedClipTypes(List.of(TimelineClipType.VIDEO, TimelineClipType.IMAGE))
                .withEffectType(TimelineEffectType.VIDEO_EFFECT)
                .build();
    }

    @Bean
    public StandardEffectFactory televisionRgbLinesEffect(IndependentPixelOperation independentPixelOperation) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new TelevisionRgbLinesEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000)), independentPixelOperation))
                .withRestoreFactory((node, loadMetadata) -> new TelevisionRgbLinesEffect(node, loadMetadata, independentPixelOperation))
                .withName("Television RGB")
                .withSupportedEffectId("televisionrgb")
                .withSupportedClipTypes(List.of(TimelineClipType.VIDEO, TimelineClipType.IMAGE))
                .withEffectType(TimelineEffectType.VIDEO_EFFECT)
                .build();
    }

    @Bean
    public StandardEffectFactory lightGlowEffect(IndependentPixelOperation independentPixelOperation, OpenCVBasedGaussianBlur blueImplementation) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new LightGlowEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000)), blueImplementation, independentPixelOperation))
                .withRestoreFactory((node, loadMetadata) -> new LightGlowEffect(node, loadMetadata, blueImplementation, independentPixelOperation))
                .withName("Light glow")
                .withSupportedEffectId("lightglow")
                .withSupportedClipTypes(List.of(TimelineClipType.VIDEO, TimelineClipType.IMAGE))
                .withEffectType(TimelineEffectType.VIDEO_EFFECT)
                .build();
    }

    @Bean
    public StandardEffectFactory vignetteEffect(IndependentPixelOperation independentPixelOperation) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new VignetteEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000)), independentPixelOperation))
                .withRestoreFactory((node, loadMetadata) -> new VignetteEffect(node, loadMetadata, independentPixelOperation))
                .withName("Vignette")
                .withSupportedEffectId("vignette")
                .withSupportedClipTypes(List.of(TimelineClipType.VIDEO, TimelineClipType.IMAGE))
                .withEffectType(TimelineEffectType.VIDEO_EFFECT)
                .build();
    }

    @Bean
    public StandardEffectFactory colorChannelChangeEffect(IndependentPixelOperation independentPixelOperation) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new ColorChannelChangeEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000)), independentPixelOperation))
                .withRestoreFactory((node, loadMetadata) -> new ColorChannelChangeEffect(node, loadMetadata, independentPixelOperation))
                .withName("Colorchannel change")
                .withSupportedEffectId("colorchannelchange")
                .withSupportedClipTypes(List.of(TimelineClipType.VIDEO, TimelineClipType.IMAGE))
                .withEffectType(TimelineEffectType.VIDEO_EFFECT)
                .build();
    }

    @Bean
    public StandardEffectFactory displacementMapEffect(DisplacementMapService displacementMapService) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new DisplacementMapEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000)), displacementMapService))
                .withRestoreFactory((node, loadMetadata) -> new DisplacementMapEffect(node, loadMetadata, displacementMapService))
                .withName("Displacement map")
                .withSupportedEffectId("displacementmap")
                .withSupportedClipTypes(List.of(TimelineClipType.VIDEO, TimelineClipType.IMAGE))
                .withEffectType(TimelineEffectType.VIDEO_EFFECT)
                .build();
    }

    @Bean
    public StandardEffectFactory layerMaskEffect(LayerMaskApplier layerMaskApplier, List<LayerMaskAlphaCalculator> calculators) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new LayerMaskEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000)), layerMaskApplier, calculators))
                .withRestoreFactory((node, loadMetadata) -> new LayerMaskEffect(node, loadMetadata, layerMaskApplier, calculators))
                .withName("Layer mask")
                .withSupportedEffectId("layermaskeffect")
                .withSupportedClipTypes(List.of(TimelineClipType.VIDEO, TimelineClipType.IMAGE))
                .withEffectType(TimelineEffectType.VIDEO_EFFECT)
                .build();
    }

    @Bean
    public StandardEffectFactory cropEffect(IndependentPixelOperation independentPixelOperation) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new CropEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000)), independentPixelOperation))
                .withRestoreFactory((node, loadMetadata) -> new CropEffect(node, loadMetadata, independentPixelOperation))
                .withName("Crop")
                .withSupportedEffectId("cropeffect")
                .withSupportedClipTypes(List.of(TimelineClipType.VIDEO, TimelineClipType.IMAGE))
                .withEffectType(TimelineEffectType.VIDEO_EFFECT)
                .build();
    }

    @Bean
    public StandardEffectFactory colorBalanceEffect(IndependentPixelOperation independentPixelOperation, BrignessContrastService brignessContrastService, ColorizeService colorizeService,
            ColorTemperatureService colorTemperatureService) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new ColorBalanceEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000)), independentPixelOperation, brignessContrastService,
                        colorizeService, colorTemperatureService))
                .withRestoreFactory((node, loadMetadata) -> new ColorBalanceEffect(node, loadMetadata, independentPixelOperation, brignessContrastService, colorizeService, colorTemperatureService))
                .withName("Color balance")
                .withSupportedEffectId("colorbalance")
                .withSupportedClipTypes(List.of(TimelineClipType.VIDEO, TimelineClipType.IMAGE))
                .withEffectType(TimelineEffectType.VIDEO_EFFECT)
                .build();
    }

    @Bean
    public StandardEffectFactory lutEffect(IndependentPixelOperation independentPixelOperation, LutProviderService lutProviderService,
            @Value("${tactview.lut.dropinFolders}") List<String> lutLocations) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new LutEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000)), independentPixelOperation, lutProviderService, lutLocations))
                .withRestoreFactory((node, loadMetadata) -> new LutEffect(node, loadMetadata, independentPixelOperation, lutProviderService, lutLocations))
                .withName("LUT")
                .withSupportedEffectId("lut")
                .withSupportedClipTypes(List.of(TimelineClipType.VIDEO, TimelineClipType.IMAGE))
                .withEffectType(TimelineEffectType.VIDEO_EFFECT)
                .build();
    }

    @Bean
    public StandardEffectFactory ghostingEffect(IndependentPixelOperation independentPixelOperation) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new GhostingEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000)), independentPixelOperation))
                .withRestoreFactory((node, loadMetadata) -> new GhostingEffect(node, loadMetadata, independentPixelOperation))
                .withName("ghosting")
                .withSupportedEffectId("ghosting")
                .withSupportedClipTypes(List.of(TimelineClipType.VIDEO, TimelineClipType.IMAGE))
                .withEffectType(TimelineEffectType.VIDEO_EFFECT)
                .build();
    }

    @Bean
    public StandardEffectFactory dropShadowEffect(IndependentPixelOperation independentPixelOperation, BlurService blurService, ScaleService scaleService) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new DropShadowEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000)), independentPixelOperation, blurService, scaleService))
                .withRestoreFactory((node, loadMetadata) -> new DropShadowEffect(node, loadMetadata, independentPixelOperation, blurService, scaleService))
                .withName("Drop Shadow")
                .withSupportedEffectId("dropshadow")
                .withSupportedClipTypes(List.of(TimelineClipType.VIDEO, TimelineClipType.IMAGE))
                .withEffectType(TimelineEffectType.VIDEO_EFFECT)
                .build();
    }

    @Bean
    public StandardEffectFactory sepiaEffect(GenericMatrixTransformationService genericMatrixTransformationService) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new SepiaEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000)), genericMatrixTransformationService))
                .withRestoreFactory((node, loadMetadata) -> new SepiaEffect(node, loadMetadata, genericMatrixTransformationService))
                .withName("Sepia")
                .withSupportedEffectId("sepia")
                .withSupportedClipTypes(List.of(TimelineClipType.VIDEO, TimelineClipType.IMAGE))
                .withEffectType(TimelineEffectType.VIDEO_EFFECT)
                .build();
    }

    @Bean
    public StandardEffectFactory sharpenEffect(OpenCVSharpenImplementation implementation) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new SharpenEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000)), implementation))
                .withRestoreFactory((node, loadMetadata) -> new SharpenEffect(node, loadMetadata, implementation))
                .withName("Sharpen")
                .withSupportedEffectId("sharpen")
                .withSupportedClipTypes(List.of(TimelineClipType.VIDEO, TimelineClipType.IMAGE))
                .withEffectType(TimelineEffectType.VIDEO_EFFECT)
                .build();
    }

    @Bean
    public StandardEffectFactory mirrorLine(IndependentPixelOperation independentPixelOperation) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new MirrorLineEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000)), independentPixelOperation))
                .withRestoreFactory((node, loadMetadata) -> new MirrorLineEffect(node, loadMetadata, independentPixelOperation))
                .withName("Mirror line")
                .withSupportedEffectId("mirrorline")
                .withSupportedClipTypes(List.of(TimelineClipType.VIDEO, TimelineClipType.IMAGE))
                .withEffectType(TimelineEffectType.VIDEO_EFFECT)
                .build();
    }

    @Bean
    public StandardEffectFactory medianEffect(IndependentPixelOperation independentPixelOperation) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new MedianEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000)), independentPixelOperation))
                .withRestoreFactory((node, loadMetadata) -> new MedianEffect(node, loadMetadata, independentPixelOperation))
                .withName("Median effect")
                .withSupportedEffectId("medianeffect")
                .withSupportedClipTypes(List.of(TimelineClipType.VIDEO, TimelineClipType.IMAGE))
                .withEffectType(TimelineEffectType.VIDEO_EFFECT)
                .build();
    }

    @Bean
    public StandardEffectFactory exclusiveDesaturizeEffect(IndependentPixelOperation independentPixelOperation) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new ExclusiveDesaturizeEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000)), independentPixelOperation))
                .withRestoreFactory((node, loadMetadata) -> new ExclusiveDesaturizeEffect(node, loadMetadata, independentPixelOperation))
                .withName("Exclusive desaturize")
                .withSupportedEffectId("exclusivedesaturize")
                .withSupportedClipTypes(List.of(TimelineClipType.VIDEO, TimelineClipType.IMAGE))
                .withEffectType(TimelineEffectType.VIDEO_EFFECT)
                .build();
    }

    @Bean
    public StandardEffectFactory maximumRgbEffect(IndependentPixelOperation independentPixelOperation) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new MaximumRgbEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000)), independentPixelOperation))
                .withRestoreFactory((node, loadMetadata) -> new MaximumRgbEffect(node, loadMetadata, independentPixelOperation))
                .withName("Max rgb")
                .withSupportedEffectId("maximumrgbeffect")
                .withSupportedClipTypes(List.of(TimelineClipType.VIDEO, TimelineClipType.IMAGE))
                .withEffectType(TimelineEffectType.VIDEO_EFFECT)
                .build();
    }

    @Bean
    public StandardEffectFactory levelsEffect(IndependentPixelOperation independentPixelOperation) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new LevelsEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000)), independentPixelOperation))
                .withRestoreFactory((node, loadMetadata) -> new LevelsEffect(node, loadMetadata, independentPixelOperation))
                .withName("Levels")
                .withSupportedEffectId("levelseffect")
                .withSupportedClipTypes(List.of(TimelineClipType.VIDEO, TimelineClipType.IMAGE))
                .withEffectType(TimelineEffectType.VIDEO_EFFECT)
                .build();
    }

    @Bean
    public StandardEffectFactory mozaicEffect(IndependentPixelOperation independentPixelOperation) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new MozaicEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000)), independentPixelOperation))
                .withRestoreFactory((node, loadMetadata) -> new MozaicEffect(node, loadMetadata, independentPixelOperation))
                .withName("Mozaic")
                .withSupportedEffectId("mozaiceffect")
                .withSupportedClipTypes(List.of(TimelineClipType.VIDEO, TimelineClipType.IMAGE))
                .withEffectType(TimelineEffectType.VIDEO_EFFECT)
                .build();
    }

    @Bean
    public StandardEffectFactory lensDistortEffect(OpenCVBasedLensDistort lensDistortImplementation) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new LensDistortEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000)), lensDistortImplementation))
                .withRestoreFactory((node, loadMetadata) -> new LensDistortEffect(node, loadMetadata, lensDistortImplementation))
                .withName("Lens distort")
                .withSupportedEffectId("lensdistort")
                .withSupportedClipTypes(List.of(TimelineClipType.VIDEO, TimelineClipType.IMAGE))
                .withEffectType(TimelineEffectType.VIDEO_EFFECT)
                .build();
    }

    @Bean
    public StandardEffectFactory polarCoordinateEffect(PolarService polarService) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new PolarCoordinateEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000)), polarService))
                .withRestoreFactory((node, loadMetadata) -> new PolarCoordinateEffect(node, loadMetadata, polarService))
                .withName("Polar coordinate")
                .withSupportedEffectId("polarcoordinate")
                .withSupportedClipTypes(List.of(TimelineClipType.VIDEO, TimelineClipType.IMAGE))
                .withEffectType(TimelineEffectType.VIDEO_EFFECT)
                .build();
    }

    @Bean
    public StandardEffectFactory blurExtendEffect(IndependentPixelOperation independentPixelOperation, BlurService blurService, ScaleService scaleService) {
        return StandardEffectFactory.builder()
                .withFactory(
                        request -> new ExtendClipWithBlurredImage(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000)), independentPixelOperation, blurService, scaleService))
                .withRestoreFactory((node, loadMetadata) -> new ExtendClipWithBlurredImage(node, loadMetadata, independentPixelOperation, blurService, scaleService))
                .withName("Blurzoom extend")
                .withSupportedEffectId("blurzoomextend")
                .withSupportedClipTypes(List.of(TimelineClipType.VIDEO, TimelineClipType.IMAGE))
                .withEffectType(TimelineEffectType.VIDEO_EFFECT)
                .build();
    }

    @Bean
    public StandardEffectFactory blendEffect(List<BlendModeStrategy> strategies, ScaleService scaleService, IndependentPixelOperation independentPixelOperation) {
        return StandardEffectFactory.builder()
                .withFactory(
                        request -> new BlendEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000)), strategies, scaleService, independentPixelOperation))
                .withRestoreFactory((node, loadMetadata) -> new BlendEffect(node, loadMetadata, strategies, scaleService, independentPixelOperation))
                .withName("Blend")
                .withSupportedEffectId("blendeffect")
                .withSupportedClipTypes(List.of(TimelineClipType.VIDEO, TimelineClipType.IMAGE))
                .withEffectType(TimelineEffectType.VIDEO_EFFECT)
                .build();
    }

    @Bean
    public StandardEffectFactory frameExtendEffect(FrameExtender frameExtender) {
        return StandardEffectFactory.builder()
                .withFactory(
                        request -> new FrameExtendEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000)), frameExtender))
                .withRestoreFactory((node, loadMetadata) -> new FrameExtendEffect(node, loadMetadata, frameExtender))
                .withName("Extend frame")
                .withSupportedEffectId("extendframe")
                .withSupportedClipTypes(List.of(TimelineClipType.VIDEO, TimelineClipType.IMAGE))
                .withEffectType(TimelineEffectType.VIDEO_EFFECT)
                .build();
    }

    @Bean
    public StandardEffectFactory orthogonalTransformEffect(ScaleService scaleService, RotateService rotateService, FrameExtender frameExtender) {
        return StandardEffectFactory.builder()
                .withFactory(
                        request -> new OrthogonalTransformationEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000)), scaleService, rotateService, frameExtender))
                .withRestoreFactory((node, loadMetadata) -> new OrthogonalTransformationEffect(node, loadMetadata, scaleService, rotateService, frameExtender))
                .withName("Orthogonal transform")
                .withSupportedEffectId("orthogonaltransform")
                .withSupportedClipTypes(List.of(TimelineClipType.VIDEO, TimelineClipType.IMAGE))
                .withEffectType(TimelineEffectType.VIDEO_EFFECT)
                .build();
    }

    @Bean
    public StandardEffectFactory polygonMaskEffect(PolygonRenderService polygonRenderService, LayerMaskApplier layerMaskApplier, LayerMaskAlphaToAlpha layerMaskAlphaToAlpha) {
        return StandardEffectFactory.builder()
                .withFactory(
                        request -> new PolygonMaskEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000)), polygonRenderService, layerMaskApplier, layerMaskAlphaToAlpha))
                .withRestoreFactory((node, loadMetadata) -> new PolygonMaskEffect(node, loadMetadata, polygonRenderService, layerMaskApplier, layerMaskAlphaToAlpha))
                .withName("Polygon mask")
                .withSupportedEffectId("polygonMask")
                .withSupportedClipTypes(List.of(TimelineClipType.VIDEO, TimelineClipType.IMAGE))
                .withEffectType(TimelineEffectType.VIDEO_EFFECT)
                .build();
    }

    @Bean
    public StandardEffectFactory rectangleWarpEffect(PolygonRenderService polygonRenderService, LayerMaskApplier layerMaskApplier, LayerMaskAlphaToAlpha layerMaskAlphaToAlpha,
            Simple2DRasterizer simple2DRasterizer) {
        return StandardEffectFactory.builder()
                .withFactory(
                        request -> new RectangleWarpEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000)), simple2DRasterizer))
                .withRestoreFactory((node, loadMetadata) -> new RectangleWarpEffect(node, loadMetadata, simple2DRasterizer))
                .withName("Rectangle warp")
                .withSupportedEffectId("rectangleWarp")
                .withSupportedClipTypes(List.of(TimelineClipType.VIDEO, TimelineClipType.IMAGE))
                .withEffectType(TimelineEffectType.VIDEO_EFFECT)
                .build();
    }

    @Bean
    public StandardEffectFactory floodFillEffect() {
        return StandardEffectFactory.builder()
                .withFactory(request -> new FloodFillEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000))))
                .withRestoreFactory((node, loadMetadata) -> new FloodFillEffect(node, loadMetadata))
                .withName("Flood fill")
                .withSupportedEffectId("floodfill")
                .withSupportedClipTypes(List.of(TimelineClipType.VIDEO, TimelineClipType.IMAGE))
                .withEffectType(TimelineEffectType.VIDEO_EFFECT)
                .build();
    }

    @Bean
    public StandardEffectFactory radialBlurEffect(RadialBlurService radialBlurService) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new RadialBlurEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000)), radialBlurService))
                .withRestoreFactory((node, loadMetadata) -> new RadialBlurEffect(node, loadMetadata, radialBlurService))
                .withName("Radial blur")
                .withSupportedEffectId("radialblur")
                .withSupportedClipTypes(List.of(TimelineClipType.VIDEO, TimelineClipType.IMAGE))
                .withEffectType(TimelineEffectType.VIDEO_EFFECT)
                .build();
    }

    @Bean
    public StandardEffectFactory linearBlurEffect(LinearBlurService linearBlurService) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new LinearBlurEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000)), linearBlurService))
                .withRestoreFactory((node, loadMetadata) -> new LinearBlurEffect(node, loadMetadata, linearBlurService))
                .withName("Linear blur")
                .withSupportedEffectId("linearblur")
                .withSupportedClipTypes(List.of(TimelineClipType.VIDEO, TimelineClipType.IMAGE))
                .withEffectType(TimelineEffectType.VIDEO_EFFECT)
                .build();
    }

    @Bean
    public StandardEffectFactory perturbationDistortEffect(DisplacementMapService displacementMapService, PerturbationNoiseService perturbationNoiseService) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new PerturbationDistortEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000)), perturbationNoiseService, displacementMapService))
                .withRestoreFactory((node, loadMetadata) -> new PerturbationDistortEffect(node, loadMetadata, perturbationNoiseService, displacementMapService))
                .withName("Perturbation distort")
                .withSupportedEffectId("perturbationdistorteffect")
                .withSupportedClipTypes(List.of(TimelineClipType.VIDEO, TimelineClipType.IMAGE))
                .withEffectType(TimelineEffectType.VIDEO_EFFECT)
                .build();
    }

    @Bean
    public StandardEffectFactory shearEffect(IndependentPixelOperation independentPixelOperation) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new ShearEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000)), independentPixelOperation))
                .withRestoreFactory((node, loadMetadata) -> new ShearEffect(node, loadMetadata, independentPixelOperation))
                .withName("Shear")
                .withSupportedEffectId("shear")
                .withSupportedClipTypes(List.of(TimelineClipType.VIDEO, TimelineClipType.IMAGE))
                .withEffectType(TimelineEffectType.VIDEO_EFFECT)
                .build();
    }

    @Bean
    public StandardEffectFactory frameHoldEffect() {
        return StandardEffectFactory.builder()
                .withFactory(request -> new FrameHoldEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000))))
                .withRestoreFactory((node, loadMetadata) -> new FrameHoldEffect(node, loadMetadata))
                .withName("Frame hold")
                .withSupportedEffectId("framehold")
                .withSupportedClipTypes(List.of(TimelineClipType.VIDEO, TimelineClipType.IMAGE))
                .withEffectType(TimelineEffectType.VIDEO_EFFECT)
                .build();
    }

    @Bean
    public StandardEffectFactory curvesEffect(IndependentPixelOperation independentPixelOperation) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new CurvesEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000)), independentPixelOperation))
                .withRestoreFactory((node, loadMetadata) -> new CurvesEffect(node, loadMetadata, independentPixelOperation))
                .withName("Curves")
                .withSupportedEffectId("curves")
                .withSupportedClipTypes(List.of(TimelineClipType.VIDEO, TimelineClipType.IMAGE))
                .withEffectType(TimelineEffectType.VIDEO_EFFECT)
                .build();
    }

    @Bean
    public StandardEffectFactory bezierPolygonEffect(BezierPolygonRenderService bezierPolygonRenderService, LayerMaskApplier layerMaskApplier, LayerMaskAlphaToAlpha layerMaskAlphaToAlpha) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new BezierMaskEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000)), bezierPolygonRenderService, layerMaskApplier,
                        layerMaskAlphaToAlpha))
                .withRestoreFactory((node, loadMetadata) -> new BezierMaskEffect(node, loadMetadata, bezierPolygonRenderService, layerMaskApplier,
                        layerMaskAlphaToAlpha))
                .withName("Bezier mask")
                .withSupportedEffectId("beziermask")
                .withSupportedClipTypes(List.of(TimelineClipType.VIDEO, TimelineClipType.IMAGE))
                .withEffectType(TimelineEffectType.VIDEO_EFFECT)
                .build();
    }

    @Bean
    public StandardEffectFactory autoWhiteBalanceEffect(ColorTemperatureService colorTemperatureService) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new AutoWhiteBalanceEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000)), colorTemperatureService))
                .withRestoreFactory((node, loadMetadata) -> new AutoWhiteBalanceEffect(node, loadMetadata, colorTemperatureService))
                .withName("Auto white balance")
                .withSupportedEffectId("autowhitebalance")
                .withSupportedClipTypes(List.of(TimelineClipType.VIDEO, TimelineClipType.IMAGE))
                .withEffectType(TimelineEffectType.VIDEO_EFFECT)
                .build();
    }

    @Bean
    public StandardEffectFactory zoomEffect(ScaleService scaleService, FrameExtender frameExtender, IndependentPixelOperation independentPixelOperation) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new ZoomEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000)), scaleService, frameExtender, independentPixelOperation))
                .withRestoreFactory((node, loadMetadata) -> new ZoomEffect(node, loadMetadata, scaleService, frameExtender, independentPixelOperation))
                .withName("Zoom to area")
                .withSupportedEffectId("zoom")
                .withSupportedClipTypes(List.of(TimelineClipType.VIDEO, TimelineClipType.IMAGE))
                .withEffectType(TimelineEffectType.VIDEO_EFFECT)
                .build();
    }

    @Bean
    public StandardEffectFactory directionalShadowEffect(ScaleService scaleService, FrameExtender frameExtender, IndependentPixelOperation independentPixelOperation) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new DirectionalShadowEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000)), independentPixelOperation))
                .withRestoreFactory((node, loadMetadata) -> new DirectionalShadowEffect(node, loadMetadata, independentPixelOperation))
                .withName("Directional shadow")
                .withSupportedEffectId("directional shadow")
                .withSupportedClipTypes(List.of(TimelineClipType.VIDEO, TimelineClipType.IMAGE))
                .withEffectType(TimelineEffectType.VIDEO_EFFECT)
                .build();
    }

    @Bean
    public StandardEffectFactory asciiArtEffect(ByteBufferToImageConverter byteBufferToImageConverter, BufferedImageToClipFrameResultConverter bufferedImageToClipFrameResultConverter) {
        return StandardEffectFactory.builder()
                .withFactory(
                        request -> new AsciiArtEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000)), byteBufferToImageConverter, bufferedImageToClipFrameResultConverter))
                .withRestoreFactory((node, loadMetadata) -> new AsciiArtEffect(node, loadMetadata, byteBufferToImageConverter, bufferedImageToClipFrameResultConverter))
                .withName("Ascii art")
                .withSupportedEffectId("asciiart")
                .withSupportedClipTypes(List.of(TimelineClipType.VIDEO, TimelineClipType.IMAGE))
                .withEffectType(TimelineEffectType.VIDEO_EFFECT)
                .build();
    }

    @Bean
    public StandardEffectFactory glassTilesEffect(IndependentPixelOperation independentPixelOperation) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new GlassTilesEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000)), independentPixelOperation))
                .withRestoreFactory((node, loadMetadata) -> new GlassTilesEffect(node, loadMetadata, independentPixelOperation))
                .withName("Glass tiles")
                .withSupportedEffectId("glass tiles")
                .withSupportedClipTypes(List.of(TimelineClipType.VIDEO, TimelineClipType.IMAGE))
                .withEffectType(TimelineEffectType.VIDEO_EFFECT)
                .build();
    }

    @Bean
    public StandardEffectFactory magnifierEffect(IndependentPixelOperation independentPixelOperation) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new MagnifierEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000)), independentPixelOperation))
                .withRestoreFactory((node, loadMetadata) -> new MagnifierEffect(node, loadMetadata, independentPixelOperation))
                .withName("Magnifier")
                .withSupportedEffectId("magnifier")
                .withSupportedClipTypes(List.of(TimelineClipType.VIDEO, TimelineClipType.IMAGE))
                .withEffectType(TimelineEffectType.VIDEO_EFFECT)
                .build();
    }

    @Bean
    @ConditionalOnPlatform(LINUX) // until we get compilation working on other platforms
    public StandardEffectFactory stabilizerVideoEffect(OpenCVStabilizeVideoService openCVStabilizeVideoService, ProjectRepository projectRepository) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new StabilizeVideoEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000)), openCVStabilizeVideoService, projectRepository))
                .withRestoreFactory((node, loadMetadata) -> new StabilizeVideoEffect(node, loadMetadata, openCVStabilizeVideoService, projectRepository))
                .withName("Stabilize")
                .withSupportedEffectId("stabilize")
                .withSupportedClipTypes(List.of(TimelineClipType.VIDEO, TimelineClipType.IMAGE))
                .withEffectType(TimelineEffectType.VIDEO_EFFECT)
                .withIsFullWidth(true)
                .build();
    }

    @Bean
    public StandardEffectFactory graphingVideoEffect(DefaultGraphArrangementFactory defaultGraphArrangementFactory, EffectGraphAccessorMessageSender effectGraphAccessor) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new GraphEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000)), defaultGraphArrangementFactory, effectGraphAccessor))
                .withRestoreFactory((node, loadMetadata) -> new GraphEffect(node, loadMetadata, defaultGraphArrangementFactory, effectGraphAccessor))
                .withName("Graph")
                .withSupportedEffectId("graphing")
                .withSupportedClipTypes(List.of(TimelineClipType.VIDEO, TimelineClipType.IMAGE))
                .withEffectType(TimelineEffectType.VIDEO_EFFECT)
                .withIsFullWidth(true)
                .build();
    }
}
