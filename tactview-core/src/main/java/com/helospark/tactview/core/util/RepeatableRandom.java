package com.helospark.tactview.core.util;

import java.math.BigDecimal;
import java.util.Random;

public class RepeatableRandom implements SavedContentAddable<RepeatableRandom> {
    int seed;

    public RepeatableRandom() {
        seed = new Random().nextInt();
    }

    public RepeatableRandom(int seed) {
        this.seed = seed;
    }

    public int nextInt(BigDecimal position) {
        Random random = createRandom(position);
        return random.nextInt();
    }

    public double nextDouble(BigDecimal position) {
        Random random = createRandom(position);
        return random.nextDouble();
    }

    public double nextDouble(BigDecimal position, int x, int y) {
        Random random = createRandom(position, y);
        return random.nextDouble();
    }

    public void getNextGaussians(BigDecimal position, double[] result) {
        Random random = createRandom(position);
        for (int i = 0; i < result.length; ++i) {
            result[i] = random.nextGaussian();
        }
    }

    private Random createRandom(BigDecimal position, int x) {
        // I'm not convinced, that this is correct, but seems random
        int seedAtCurrentPosition = position.multiply(BigDecimal.valueOf(1000)).hashCode() * seed + 1;
        seedAtCurrentPosition += (int) (x * 2654435761L);
        return new Random(seedAtCurrentPosition);
    }

    private Random createRandom(BigDecimal seconds) {
        int seedAtCurrentPosition = seconds.multiply(BigDecimal.valueOf(1000)).hashCode() * seed;
        return new Random(seedAtCurrentPosition);
    }

    public void getNextDoubles(BigDecimal seconds, double[] result) {
        Random random = createRandom(seconds);
        for (int i = 0; i < result.length; ++i) {
            result[i] = random.nextGaussian();
        }
    }

    @Override
    public Class<? extends DesSerFactory<RepeatableRandom>> generateSerializableContent() {
        return RepeatableRandomFactory.class;
    }

}
