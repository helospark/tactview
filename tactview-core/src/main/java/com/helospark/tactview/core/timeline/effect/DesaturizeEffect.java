package com.helospark.tactview.core.timeline.effect;

import java.nio.ByteBuffer;
import java.util.UUID;

import com.helospark.tactview.core.timeline.StatelessVideoEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;

public class DesaturizeEffect implements StatelessVideoEffect {
    private TimelineInterval interval;
    private String id;

    public DesaturizeEffect(TimelineInterval interval) {
        this.interval = interval;
        this.id = UUID.randomUUID().toString();
    }

    @Override
    public TimelineInterval getInterval() {
        return interval;
    }

    @Override
    public void fillFrame(ByteBuffer result, ByteBuffer currentFrame) {
        for (int i = 0; i < currentFrame.capacity(); i += 4) {
            int r = printByte(currentFrame.get(i + 0));
            int g = printByte(currentFrame.get(i + 1));
            int b = printByte(currentFrame.get(i + 2));
            byte a = currentFrame.get(i + 3);
            int desaturized = (r + g + b) / 3;
            result.put(i + 0, (byte) (desaturized & 0xFF));
            result.put(i + 1, (byte) (desaturized & 0xFF));
            result.put(i + 2, (byte) (desaturized & 0xFF));
            result.put(i + 3, a);
        }
    }

    @Override
    public String getId() {
        return id;
    }

    public static int printByte(byte b) {
        int value;
        if (b < 0) {
            value = 256 + b;
        } else {
            value = b;
        }
        return value;
    }

}
