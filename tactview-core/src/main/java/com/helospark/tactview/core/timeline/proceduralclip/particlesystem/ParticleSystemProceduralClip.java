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
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.decoder.ImageMetadata;
import com.helospark.tactview.core.decoder.VisualMediaMetadata;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.GetFrameRequest;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Color;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ColorProvider;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.timeline.proceduralclip.ProceduralVisualClip;
import com.helospark.tactview.core.util.ReflectionUtil;
import com.helospark.tactview.core.util.RepeatableRandom;

public class ParticleSystemProceduralClip extends ProceduralVisualClip {
    private static final double SIMULATION_TIME = 1 / 30.0;
    private static final double CACHE_TIME = 0.600;
    private TreeMap<BigDecimal, List<Particle>> particle = new TreeMap<>();

    private ColorProvider startColorProvider;
    private ColorProvider endColorProvider;

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

        int size = 4;

        double currentSeconds = relativePosition.getSeconds().doubleValue();

        Color startColor = startColorProvider.getValueAt(relativePosition).multiply(Color.of(255, 255, 255));
        Color endColor = endColorProvider.getValueAt(relativePosition).multiply(Color.of(255, 255, 255));

        for (Particle particle : particles) {
            double normalizedAge = (currentSeconds - particle.bornTime) / particle.maxAge;
            for (int relativeX = -size; relativeX < size; ++relativeX) {
                for (int relativeY = -size; relativeY < size; ++relativeY) {
                    int x = (int) (particle.x + relativeX);
                    int y = (int) (particle.y + relativeY);
                    if (result.inBounds(x, y)) {
                        double normalizedDistance = (relativeX * relativeX + relativeY * relativeY) / (size * size);
                        if (normalizedDistance < 1) {
                            double alpha = normalizedDistance * (1.0 - normalizedAge);
                            Color color = startColor.interpolate(endColor, normalizedAge);

                            // TODO: blit
                            result.setRed((int) color.red, x, y);
                            result.setGreen((int) color.green, x, y);
                            result.setBlue((int) color.blue, x, y);
                            result.setAlpha((int) (255.0), x, y);
                        }
                    }
                }
            }
        }

        return result;
    }

    private List<Particle> getParticlesAtPosition(TimelinePosition requestedPosition) {
        Entry<BigDecimal, List<Particle>> cachedEntry = particle.floorEntry(requestedPosition.getSeconds());
        if (cachedEntry != null) {
            List<Particle> particles = cloneParticles(cachedEntry.getValue());
            BigDecimal startSecond = cachedEntry.getKey();
            return simulateParticles(requestedPosition, particles, startSecond);
        } else {
            return simulateParticles(requestedPosition, new ArrayList<>(), BigDecimal.ZERO);
        }
    }

    private List<Particle> simulateParticles(TimelinePosition requestedPosition, List<Particle> particles, BigDecimal startSecond) {
        double timeSinceLastCache = 0.0;

        startSecond = startSecond.add(BigDecimal.valueOf(SIMULATION_TIME));

        BigDecimal roundedEndPosition = requestedPosition.getSeconds().setScale(2, RoundingMode.CEILING);
        BigDecimal roundedStartPosition = startSecond.setScale(2, RoundingMode.CEILING);

        while (roundedStartPosition.compareTo(roundedEndPosition) < 0) {
            Random random = repeatableRandom.createRandomForPosition(roundedStartPosition);

            for (Particle particle : particles) {
                particle.x += particle.xVel;
                particle.y += particle.yVel;

                particle.yVel += 0.1;
            }

            for (int i = 0; i < 6; ++i) {
                Particle particle = new Particle();
                particle.x = 100;
                particle.y = 100;
                particle.xVel = random.nextDouble() * 4.0 - 2.0;
                particle.yVel = random.nextDouble() * 4.0 - 2.0;
                particle.maxAge = random.nextDouble() * 3.0;
                particle.bornTime = roundedStartPosition.doubleValue();
                particles.add(particle);
            }

            double currentSecondDouble = roundedStartPosition.doubleValue();
            particles.removeIf(particle -> particle.bornTime + particle.maxAge <= currentSecondDouble);

            timeSinceLastCache += SIMULATION_TIME;
            if (timeSinceLastCache >= CACHE_TIME) {
                timeSinceLastCache -= CACHE_TIME;
                particle.put(roundedStartPosition, cloneParticles(particles));
            }
            roundedStartPosition = roundedStartPosition.add(BigDecimal.valueOf(SIMULATION_TIME));
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
    }

    @Override
    public List<ValueProviderDescriptor> getDescriptorsInternal() {
        List<ValueProviderDescriptor> descriptors = super.getDescriptorsInternal();

        ValueProviderDescriptor startColorDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(startColorProvider)
                .withName("start color")
                .build();
        ValueProviderDescriptor endColorDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(startColorProvider)
                .withName("end color")
                .build();

        descriptors.add(startColorDescriptor);
        descriptors.add(endColorDescriptor);

        return descriptors;
    }

    @Override
    public TimelineClip cloneClip(CloneRequestMetadata cloneRequestMetadata) {
        return new ParticleSystemProceduralClip(cloneRequestMetadata, this);
    }

}
