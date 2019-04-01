package com.helospark.tactview.core.timeline.proceduralclip;

import com.helospark.lightdi.annotation.Bean;
import com.helospark.lightdi.annotation.Configuration;
import com.helospark.tactview.core.decoder.ImageMetadata;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelineLength;
import com.helospark.tactview.core.timeline.proceduralclip.channelcopy.ChannelCopyProceduralClip;
import com.helospark.tactview.core.timeline.proceduralclip.gradient.LinearGradientProceduralClip;
import com.helospark.tactview.core.timeline.proceduralclip.gradient.RadialGradientProceduralEffect;
import com.helospark.tactview.core.timeline.proceduralclip.gradient.service.LinearGradientService;
import com.helospark.tactview.core.timeline.proceduralclip.gradient.service.RadialGradientService;
import com.helospark.tactview.core.timeline.proceduralclip.highlight.DrawnEllipseHighlightProceduralEffect;
import com.helospark.tactview.core.timeline.proceduralclip.highlight.DrawnRectangleHighlightProceduralEffect;
import com.helospark.tactview.core.timeline.proceduralclip.highlight.HighlightPenProceduralEffect;
import com.helospark.tactview.core.timeline.proceduralclip.lines.GridProceduralClip;
import com.helospark.tactview.core.timeline.proceduralclip.lines.LineProceduralClip;
import com.helospark.tactview.core.timeline.proceduralclip.lines.LinesProceduralClip;
import com.helospark.tactview.core.timeline.proceduralclip.lines.impl.DrawLineService;
import com.helospark.tactview.core.timeline.proceduralclip.noise.GaussianNoiseProceduralClip;
import com.helospark.tactview.core.timeline.proceduralclip.noise.GradientPerturbationProceduralClip;
import com.helospark.tactview.core.timeline.proceduralclip.noise.NoiseProceduralClip;
import com.helospark.tactview.core.timeline.proceduralclip.noise.service.PerturbationNoiseService;
import com.helospark.tactview.core.timeline.proceduralclip.particlesystem.ParticleSystemProceduralClip;
import com.helospark.tactview.core.timeline.proceduralclip.pattern.CheckerBoardProceduralClip;
import com.helospark.tactview.core.timeline.proceduralclip.polygon.DrawnNurbsProceduralClip;
import com.helospark.tactview.core.timeline.proceduralclip.polygon.DrawnPolygonProceduralClip;
import com.helospark.tactview.core.timeline.proceduralclip.polygon.PolygonProceduralClip;
import com.helospark.tactview.core.timeline.proceduralclip.polygon.RectangleProceduralClip;
import com.helospark.tactview.core.timeline.proceduralclip.polygon.impl.PolygonRenderService;
import com.helospark.tactview.core.timeline.proceduralclip.singlecolor.SingleColorProceduralClip;
import com.helospark.tactview.core.timeline.proceduralclip.spark.NovaProceduralClip;
import com.helospark.tactview.core.timeline.proceduralclip.text.TextProceduralClip;
import com.helospark.tactview.core.util.BresenhemPixelProvider;
import com.helospark.tactview.core.util.BufferedImageToClipFrameResultConverter;
import com.helospark.tactview.core.util.IndependentPixelOperation;

@Configuration
public class CoreClipFactoryChainItemConfiguration {
    TimelineLength defaultLength = TimelineLength.ofMillis(30000);
    ImageMetadata metadata = ImageMetadata.builder()
            .withWidth(1920)
            .withHeight(1080)
            .withLength(defaultLength)
            .build();

    @Bean
    public StandardProceduralClipFactoryChainItem singleColorProceduralClip(IndependentPixelOperation independentPixelOperation) {
        return new StandardProceduralClipFactoryChainItem("singlecolor", "Single color",
                request -> {
                    return new SingleColorProceduralClip(metadata, new TimelineInterval(request.getPosition(), defaultLength), independentPixelOperation);
                },
                (node, loadMetadata) -> {
                    return new SingleColorProceduralClip(metadata, node, loadMetadata, independentPixelOperation);
                });
    }

    @Bean
    public StandardProceduralClipFactoryChainItem textProceduralClip(BufferedImageToClipFrameResultConverter bufferedImageToClipFrameResultConverter) {
        return new StandardProceduralClipFactoryChainItem("text", "Text",
                request -> {
                    return new TextProceduralClip(metadata, new TimelineInterval(request.getPosition(), defaultLength), bufferedImageToClipFrameResultConverter);
                },
                (node, loadMetadata) -> {
                    return new TextProceduralClip(metadata, node, loadMetadata, bufferedImageToClipFrameResultConverter);
                });
    }

    @Bean
    public StandardProceduralClipFactoryChainItem gradientProceduralClip(RadialGradientService radialGradientService) {
        return new StandardProceduralClipFactoryChainItem("radialgradient", "Radial gradient",
                request -> {
                    return new RadialGradientProceduralEffect(metadata, new TimelineInterval(request.getPosition(), defaultLength), radialGradientService);
                },
                (node, loadMetadata) -> {
                    return new RadialGradientProceduralEffect(metadata, node, loadMetadata, radialGradientService);
                });
    }

    @Bean
    public StandardProceduralClipFactoryChainItem linearProceduralClip(LinearGradientService linearGradientService) {
        return new StandardProceduralClipFactoryChainItem("lineargradient", "Linear gradient",
                request -> {
                    return new LinearGradientProceduralClip(metadata, new TimelineInterval(request.getPosition(), defaultLength), linearGradientService);
                },
                (node, loadMetadata) -> {
                    return new LinearGradientProceduralClip(metadata, node, loadMetadata, linearGradientService);
                });
    }

    @Bean
    public StandardProceduralClipFactoryChainItem gaussianNoiseProceduralClip(IndependentPixelOperation independentPixelOperation) {
        return new StandardProceduralClipFactoryChainItem("gaussianNoise", "Gaussian noise",
                request -> {
                    return new GaussianNoiseProceduralClip(metadata, new TimelineInterval(request.getPosition(), defaultLength), independentPixelOperation);
                },
                (node, loadMetadata) -> {
                    return new GaussianNoiseProceduralClip(metadata, node, loadMetadata, independentPixelOperation);
                });
    }

    @Bean
    public StandardProceduralClipFactoryChainItem drawnHighlightProceduralEffect(DrawLineService drawLineService, BresenhemPixelProvider bresenhemPixelProvider) {
        return new StandardProceduralClipFactoryChainItem("drawnhighlight", "Drawn ellipse highlight",
                request -> {
                    return new DrawnEllipseHighlightProceduralEffect(metadata, new TimelineInterval(request.getPosition(), defaultLength), drawLineService, bresenhemPixelProvider);
                },
                (node, loadMetadata) -> {
                    return new DrawnEllipseHighlightProceduralEffect(metadata, node, loadMetadata, drawLineService, bresenhemPixelProvider);
                });
    }

    @Bean
    public StandardProceduralClipFactoryChainItem drawnRectangleHighlightProceduralEffect(DrawLineService drawLineService, BresenhemPixelProvider bresenhemPixelProvider) {
        return new StandardProceduralClipFactoryChainItem("drawnrectanglehighlight", "Drawn rectangle highlight",
                request -> {
                    return new DrawnRectangleHighlightProceduralEffect(metadata, new TimelineInterval(request.getPosition(), defaultLength), drawLineService, bresenhemPixelProvider);
                },
                (node, loadMetadata) -> {
                    return new DrawnRectangleHighlightProceduralEffect(metadata, node, loadMetadata, drawLineService, bresenhemPixelProvider);
                });
    }

    @Bean
    public StandardProceduralClipFactoryChainItem highlighterPenProceduralEffect(DrawLineService drawLineService, BresenhemPixelProvider bresenhemPixelProvider) {
        return new StandardProceduralClipFactoryChainItem("highlighterpen", "Highlighter pen",
                request -> {
                    return new HighlightPenProceduralEffect(metadata, new TimelineInterval(request.getPosition(), defaultLength), drawLineService, bresenhemPixelProvider);
                },
                (node, loadMetadata) -> {
                    return new HighlightPenProceduralEffect(metadata, node, loadMetadata, drawLineService, bresenhemPixelProvider);
                });
    }

    @Bean
    public StandardProceduralClipFactoryChainItem fillPolygonProceduralClip(PolygonRenderService polygonRenderService) {
        return new StandardProceduralClipFactoryChainItem("fillpolygon", "Fill polygon",
                request -> {
                    return new PolygonProceduralClip(metadata, new TimelineInterval(request.getPosition(), defaultLength), polygonRenderService);
                },
                (node, loadMetadata) -> {
                    return new PolygonProceduralClip(metadata, node, loadMetadata, polygonRenderService);
                });
    }

    @Bean
    public StandardProceduralClipFactoryChainItem noiseProceduralClip(IndependentPixelOperation independentPixelOperation) {
        return new StandardProceduralClipFactoryChainItem("noise", "Noise",
                request -> {
                    return new NoiseProceduralClip(metadata, new TimelineInterval(request.getPosition(), defaultLength), independentPixelOperation);
                },
                (node, loadMetadata) -> {
                    return new NoiseProceduralClip(metadata, node, loadMetadata, independentPixelOperation);
                });
    }

    @Bean
    public StandardProceduralClipFactoryChainItem novaProceduralClip(IndependentPixelOperation independentPixelOperation) {
        return new StandardProceduralClipFactoryChainItem("nova", "Nova",
                request -> {
                    return new NovaProceduralClip(metadata, new TimelineInterval(request.getPosition(), defaultLength), independentPixelOperation);
                },
                (node, loadMetadata) -> {
                    return new NovaProceduralClip(metadata, node, loadMetadata, independentPixelOperation);
                });
    }

    @Bean
    public StandardProceduralClipFactoryChainItem checkerboardProceduralClip(IndependentPixelOperation independentPixelOperation) {
        return new StandardProceduralClipFactoryChainItem("checkerboard", "Checkerboard",
                request -> {
                    return new CheckerBoardProceduralClip(metadata, new TimelineInterval(request.getPosition(), defaultLength), independentPixelOperation);
                },
                (node, loadMetadata) -> {
                    return new CheckerBoardProceduralClip(metadata, node, loadMetadata, independentPixelOperation);
                });
    }

    @Bean
    public StandardProceduralClipFactoryChainItem lineProceduralClip(DrawLineService drawLineService, BresenhemPixelProvider bresenhemPixelProvider) {
        return new StandardProceduralClipFactoryChainItem("line", "Line",
                request -> {
                    return new LineProceduralClip(metadata, new TimelineInterval(request.getPosition(), defaultLength), drawLineService, bresenhemPixelProvider);
                },
                (node, loadMetadata) -> {
                    return new LineProceduralClip(metadata, node, loadMetadata, drawLineService, bresenhemPixelProvider);
                });
    }

    @Bean
    public StandardProceduralClipFactoryChainItem gridProceduralClip(DrawLineService drawLineService, BresenhemPixelProvider bresenhemPixelProvider) {
        return new StandardProceduralClipFactoryChainItem("grid", "Grid",
                request -> {
                    return new GridProceduralClip(metadata, new TimelineInterval(request.getPosition(), defaultLength));
                },
                (node, loadMetadata) -> {
                    return new GridProceduralClip(metadata, node, loadMetadata);
                });
    }

    @Bean
    public StandardProceduralClipFactoryChainItem rectangleProceduralClip(IndependentPixelOperation independentPixelOperation) {
        return new StandardProceduralClipFactoryChainItem("rectangle", "Rectangle",
                request -> {
                    return new RectangleProceduralClip(metadata, new TimelineInterval(request.getPosition(), defaultLength), independentPixelOperation);
                },
                (node, loadMetadata) -> {
                    return new RectangleProceduralClip(metadata, node, loadMetadata, independentPixelOperation);
                });
    }

    @Bean
    public StandardProceduralClipFactoryChainItem particleSystemProceduralClip(IndependentPixelOperation independentPixelOperation) {
        return new StandardProceduralClipFactoryChainItem("particlesystem", "Particle system",
                request -> {
                    return new ParticleSystemProceduralClip(metadata, new TimelineInterval(request.getPosition(), defaultLength));
                },
                (node, loadMetadata) -> {
                    return new ParticleSystemProceduralClip(metadata, node, loadMetadata);
                });
    }

    @Bean
    public StandardProceduralClipFactoryChainItem channelCopyProceduralClip(IndependentPixelOperation independentPixelOperation) {
        return new StandardProceduralClipFactoryChainItem("channelcopy", "Copy channel",
                request -> {
                    return new ChannelCopyProceduralClip(metadata, new TimelineInterval(request.getPosition(), defaultLength));
                },
                (node, loadMetadata) -> {
                    return new ChannelCopyProceduralClip(metadata, node, loadMetadata);
                });
    }

    @Bean
    public StandardProceduralClipFactoryChainItem linesProceduralClip(IndependentPixelOperation independentPixelOperation) {
        return new StandardProceduralClipFactoryChainItem("lines", "vertical/horizontal lines",
                request -> {
                    return new LinesProceduralClip(metadata, new TimelineInterval(request.getPosition(), defaultLength));
                },
                (node, loadMetadata) -> {
                    return new LinesProceduralClip(metadata, node, loadMetadata);
                });
    }

    @Bean
    public StandardProceduralClipFactoryChainItem drawnPolygonProceduralClip(DrawLineService drawLineService, BresenhemPixelProvider bresenhemPixelProvider) {
        return new StandardProceduralClipFactoryChainItem("drawnpolygon", "Drawn polygon",
                request -> {
                    return new DrawnPolygonProceduralClip(metadata, new TimelineInterval(request.getPosition(), defaultLength), drawLineService, bresenhemPixelProvider);
                },
                (node, loadMetadata) -> {
                    return new DrawnPolygonProceduralClip(metadata, node, loadMetadata, drawLineService, bresenhemPixelProvider);
                });
    }

    @Bean
    public StandardProceduralClipFactoryChainItem drawnNurbsProceduralClip(DrawLineService drawLineService, BresenhemPixelProvider bresenhemPixelProvider) {
        return new StandardProceduralClipFactoryChainItem("drawnnurbs", "NURBS",
                request -> {
                    return new DrawnNurbsProceduralClip(metadata, new TimelineInterval(request.getPosition(), defaultLength), drawLineService, bresenhemPixelProvider);
                },
                (node, loadMetadata) -> {
                    return new DrawnNurbsProceduralClip(metadata, node, loadMetadata, drawLineService, bresenhemPixelProvider);
                });
    }

    @Bean
    public StandardProceduralClipFactoryChainItem gradientPerturbationProceduralClip(PerturbationNoiseService perturbationNoiseService) {
        return new StandardProceduralClipFactoryChainItem("gradientperturbation", "Perturbation",
                request -> {
                    return new GradientPerturbationProceduralClip(metadata, new TimelineInterval(request.getPosition(), defaultLength), perturbationNoiseService);
                },
                (node, loadMetadata) -> {
                    return new GradientPerturbationProceduralClip(metadata, node, loadMetadata, perturbationNoiseService);
                });
    }
}
