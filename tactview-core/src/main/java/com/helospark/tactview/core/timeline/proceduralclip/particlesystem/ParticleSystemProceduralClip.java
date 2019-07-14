package com.helospark.tactview.core.timeline.proceduralclip.particlesystem;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.CloneRequestMetadata;
import com.helospark.tactview.core.LoadMetadata;
import com.helospark.tactview.core.ReflectionUtil;
import com.helospark.tactview.core.RepeatableRandom;
import com.helospark.tactview.core.decoder.ImageMetadata;
import com.helospark.tactview.core.decoder.VisualMediaMetadata;
import com.helospark.tactview.core.timeline.GetFrameRequest;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Color;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.DoubleRange;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ColorProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleRangeProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.IntegerProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.PointProvider;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.timeline.proceduralclip.ProceduralVisualClip;

public class ParticleSystemProceduralClip extends ProceduralVisualClip {
    private static final BigDecimal SIMULATION_TIME = BigDecimal.valueOf(1).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_DOWN);
    private static final BigDecimal CACHE_TIME = new BigDecimal("0.600");
    private TreeMap<BigDecimal, List<Particle>> particlesCache = new TreeMap<>();

    private ColorProvider startColorProvider;
    private ColorProvider endColorProvider;
    private DoubleProvider fuzzyProvider;

    private PointProvider emitterCenterProvider;
    private DoubleProvider emitterRandomizationProvider;

    private PointProvider startDirectionProvider;
    private DoubleProvider startDirectionXRandomSpeedProvider;
    private DoubleProvider startDirectionYRandomSpeedProvider;

    private DoubleProvider numberOfParticlesCreatedInStep;
    private DoubleProvider gravityProvider;

    private DoubleRangeProvider ageRangeProvider;

    private IntegerProvider sizeProvider;

    private RepeatableRandom repeatableRandom;

    public ParticleSystemProceduralClip(VisualMediaMetadata visualMediaMetadata, TimelineInterval interval) {
        super(visualMediaMetadata, interval);
        repeatableRandom = new RepeatableRandom();
    }

    public ParticleSystemProceduralClip(CloneRequestMetadata cloneRequestMetadata, ParticleSystemProceduralClip particleSystemProceduralClip) {
        super(particleSystemProceduralClip, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(particleSystemProceduralClip, this);
    }

    public ParticleSystemProceduralClip(ImageMetadata metadata, JsonNode node, LoadMetadata loadMetadata) {
        super(metadata, node, loadMetadata);
    }

    @Override
    public ReadOnlyClipImage createProceduralFrame(GetFrameRequest request, TimelinePosition relativePosition) {
        ClipImage result = ClipImage.fromSize(request.getExpectedWidth(), request.getExpectedHeight());
        List<Particle> particles = getParticlesAtPosition(relativePosition);

        double currentSeconds = relativePosition.getSeconds().doubleValue();

        Color startColor = startColorProvider.getValueAt(relativePosition).multiply(Color.of(255, 255, 255));
        Color endColor = endColorProvider.getValueAt(relativePosition).multiply(Color.of(255, 255, 255));
        int size = (int) (sizeProvider.getValueAt(relativePosition) * request.getScale());
        double fuzzy = 1.0 - fuzzyProvider.getValueAt(relativePosition);

        for (Particle particle : particles) {
            double normalizedAge = (currentSeconds - particle.bornTime) / particle.maxAge;
            // TODO: More display types
            for (int relativeX = -size; relativeX < size; ++relativeX) {
                for (int relativeY = -size; relativeY < size; ++relativeY) {
                    int x = (int) (particle.x * request.getExpectedWidth() + relativeX);
                    int y = (int) (particle.y * request.getExpectedHeight() + relativeY);
                    if (result.inBounds(x, y)) {
                        double normalizedDistance = ((double) (relativeX * relativeX + relativeY * relativeY)) / (size * size);
                        if (normalizedDistance < 1) {

                            double fuzzyAlpha = 0;
                            if (normalizedDistance < fuzzy) {
                                fuzzyAlpha = 1.0;
                            } else {
                                fuzzyAlpha = 1.0 - (normalizedDistance - fuzzy) / (1.0 - fuzzy);
                            }

                            double alpha = fuzzyAlpha * (1.0 - normalizedAge);
                            Color color = startColor.interpolate(endColor, normalizedAge);

                            int backgroundAlpha = result.getAlpha(x, y);

                            result.setRed((int) (color.red * alpha + (1.0 - alpha) * result.getRed(x, y)), x, y);
                            result.setGreen((int) (color.green * alpha + (1.0 - alpha) * result.getGreen(x, y)), x, y);
                            result.setBlue((int) (color.blue * alpha + (1.0 - alpha) * result.getBlue(x, y)), x, y);
                            result.setAlpha((int) (backgroundAlpha + (1.0 - backgroundAlpha / 255.0) * (alpha * 255.0)), x, y);
                        }
                    }
                }
            }
        }

        return result;
    }

    private List<Particle> getParticlesAtPosition(TimelinePosition requestedPosition) {
        Entry<BigDecimal, List<Particle>> cachedEntry = particlesCache.floorEntry(requestedPosition.getSeconds());
        if (cachedEntry != null) {
            List<Particle> particles = cloneParticles(cachedEntry.getValue());
            BigDecimal startSecond = cachedEntry.getKey();
            return simulateParticles(requestedPosition, particles, startSecond);
        } else {
            return simulateParticles(requestedPosition, new ArrayList<>(), BigDecimal.ZERO);
        }
    }

    private List<Particle> simulateParticles(TimelinePosition requestedPosition, List<Particle> particles, BigDecimal startSecond) {
        BigDecimal timeSinceLastCache = BigDecimal.ZERO;

        BigDecimal roundedEndPosition = requestedPosition.getSeconds().setScale(2, RoundingMode.CEILING);
        BigDecimal roundedStartPosition = startSecond.setScale(2, RoundingMode.CEILING).add(SIMULATION_TIME);

        while (roundedStartPosition.compareTo(roundedEndPosition) < 0) {
            TimelinePosition currentSimlutatedTimelinePosition = new TimelinePosition(roundedStartPosition);
            Random random = repeatableRandom.createRandomForPosition(roundedStartPosition);

            double gravity = gravityProvider.getValueAt(currentSimlutatedTimelinePosition);

            for (Particle particle : particles) {
                particle.x += particle.xVel;
                particle.y += particle.yVel;

                particle.yVel += gravity;
            }

            int numberOfParticlesToCreate = (int) numberOfParticlesCreatedInStep.getValueAt(currentSimlutatedTimelinePosition).doubleValue();
            Point center = emitterCenterProvider.getValueAt(currentSimlutatedTimelinePosition);
            Point startDirection = startDirectionProvider.getValueAt(currentSimlutatedTimelinePosition);
            double randomXSpeed = startDirectionXRandomSpeedProvider.getValueAt(currentSimlutatedTimelinePosition);
            double randomYSpeed = startDirectionYRandomSpeedProvider.getValueAt(currentSimlutatedTimelinePosition);

            double emitterRandomization = emitterRandomizationProvider.getValueAt(currentSimlutatedTimelinePosition);

            DoubleRange lifeRange = ageRangeProvider.getValueAt(currentSimlutatedTimelinePosition);

            for (int i = 0; i < numberOfParticlesToCreate; ++i) {
                Particle particle = new Particle();
                particle.x = center.x + ((random.nextDouble() * 2.0 - 1.0) * emitterRandomization);
                particle.y = center.y + ((random.nextDouble() * 2.0 - 1.0) * emitterRandomization);
                particle.xVel = startDirection.x + (random.nextDouble() * 2.0 - 1.0) * randomXSpeed;
                particle.yVel = startDirection.y + (random.nextDouble() * 2.0 - 1.0) * randomYSpeed;
                particle.maxAge = lifeRange.lowEnd + random.nextDouble() * (lifeRange.highEnd - lifeRange.lowEnd);
                particle.bornTime = roundedStartPosition.doubleValue();
                particles.add(particle);
            }

            double currentSecondDouble = roundedStartPosition.doubleValue();
            particles.removeIf(particle -> particle.bornTime + particle.maxAge <= currentSecondDouble);

            timeSinceLastCache.add(SIMULATION_TIME);
            if (timeSinceLastCache.compareTo(CACHE_TIME) > 0) {
                timeSinceLastCache = timeSinceLastCache.subtract(CACHE_TIME);
                particlesCache.put(roundedStartPosition, cloneParticles(particles));
            }
            roundedStartPosition = roundedStartPosition.add(SIMULATION_TIME);
        }
        return particles;
    }

    private List<Particle> cloneParticles(List<Particle> value) {
        return value.stream()
                .map(p -> p.cloneParticle())
                .collect(Collectors.toList());
    }

    @Override
    protected void initializeValueProvider() {
        super.initializeValueProvider();

        startColorProvider = ColorProvider.fromDefaultValue(1.0, 0.0, 0.0);
        endColorProvider = ColorProvider.fromDefaultValue(0.0, 0.0, 0.0);
        fuzzyProvider = new DoubleProvider(0.0, 1.0, new MultiKeyframeBasedDoubleInterpolator(0.4));

        startDirectionProvider = new PointProvider(new DoubleProvider(new MultiKeyframeBasedDoubleInterpolator(0.0)), new DoubleProvider(new MultiKeyframeBasedDoubleInterpolator(0.0)));
        startDirectionXRandomSpeedProvider = new DoubleProvider(0, 2, new MultiKeyframeBasedDoubleInterpolator(0.01));
        startDirectionYRandomSpeedProvider = new DoubleProvider(0, 2, new MultiKeyframeBasedDoubleInterpolator(0.01));
        numberOfParticlesCreatedInStep = new DoubleProvider(0.0, 100.0, new MultiKeyframeBasedDoubleInterpolator(6.0));
        gravityProvider = new DoubleProvider(-2.0, 2.0, new MultiKeyframeBasedDoubleInterpolator(0.001));
        emitterCenterProvider = PointProvider.ofNormalizedImagePosition(0.5, 0.5);
        ageRangeProvider = DoubleRangeProvider.createDefaultDoubleRangeProvider(0.0, 30.0, 0.5, 3.0);
        sizeProvider = new IntegerProvider(0, 200, new MultiKeyframeBasedDoubleInterpolator(60.0));
        emitterRandomizationProvider = new DoubleProvider(0.0, 1.0, new MultiKeyframeBasedDoubleInterpolator(0.01));
    }

    @Override
    public List<ValueProviderDescriptor> getDescriptorsInternal() {
        List<ValueProviderDescriptor> descriptors = super.getDescriptorsInternal();

        ValueProviderDescriptor numberOfParticlesCreatedInStepDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(numberOfParticlesCreatedInStep)
                .withName("particles created")
                .build();
        ValueProviderDescriptor gravityProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(gravityProvider)
                .withName("gravity")
                .build();
        ValueProviderDescriptor ageRangeProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(ageRangeProvider)
                .withName("age range")
                .build();
        ValueProviderDescriptor sizeProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(sizeProvider)
                .withName("size")
                .build();
        ValueProviderDescriptor emitterCenterProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(emitterCenterProvider)
                .withName("center")
                .build();
        ValueProviderDescriptor emitterRandomizationProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(emitterRandomizationProvider)
                .withName("center randomization")
                .build();
        ValueProviderDescriptor startColorDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(startColorProvider)
                .withName("start color")
                .build();
        ValueProviderDescriptor endColorDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(endColorProvider)
                .withName("end color")
                .build();
        ValueProviderDescriptor fuzzyProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(fuzzyProvider)
                .withName("fuzzy")
                .build();

        ValueProviderDescriptor startDirectionDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(startDirectionProvider)
                .withName("start direction")
                .build();
        ValueProviderDescriptor startDirectionXRandomSpeedProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(startDirectionXRandomSpeedProvider)
                .withName("random x speed")
                .build();
        ValueProviderDescriptor startDirectionYRandomSpeedProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(startDirectionYRandomSpeedProvider)
                .withName("random y speed")
                .build();

        descriptors.add(numberOfParticlesCreatedInStepDescriptor);
        descriptors.add(gravityProviderDescriptor);
        descriptors.add(ageRangeProviderDescriptor);
        descriptors.add(sizeProviderDescriptor);

        descriptors.add(emitterCenterProviderDescriptor);
        descriptors.add(emitterRandomizationProviderDescriptor);

        descriptors.add(startColorDescriptor);
        descriptors.add(endColorDescriptor);
        descriptors.add(fuzzyProviderDescriptor);

        descriptors.add(startDirectionDescriptor);
        descriptors.add(startDirectionXRandomSpeedProviderDescriptor);
        descriptors.add(startDirectionYRandomSpeedProviderDescriptor);

        return descriptors;
    }

    @Override
    public TimelineClip cloneClip(CloneRequestMetadata cloneRequestMetadata) {
        return new ParticleSystemProceduralClip(cloneRequestMetadata, this);
    }

    @Override
    public void effectChanged(EffectChangedRequest request) {
        super.effectChanged(request);
        particlesCache.clear();
    }

}
