package com.helospark.tactview.core.timeline;

import java.nio.ByteBuffer;
import java.util.List;

public class AudioFrameResult {
    private List<ByteBuffer> channels;
    private int samples;
    private int bytesPerSample;
    private TimelineLength length;

    public AudioFrameResult(List<ByteBuffer> channels, int samplesPerSecond, int bytesPerSample) {
        this.channels = channels;
        this.samples = samplesPerSecond;
        this.bytesPerSample = bytesPerSample;
        this.length = TimelineLength.ofSeconds((double) channels.get(0).capacity() / (samplesPerSecond * bytesPerSample));
    }

    public List<ByteBuffer> getChannels() {
        return channels;
    }

    public int getBytesPerSample() {
        return bytesPerSample;
    }

    public int getSamplePerSecond() {
        return samples;
    }

    public int getSampleAt(int channelIndex, int sampleIndex) {
        ByteBuffer channelBuffer = channels.get(channelIndex);
        if (sampleIndex > channelBuffer.capacity()) {
            return 0;
        }
        return signedToUnsignedByte(channelBuffer.get(sampleIndex));
    }

    public void setSampleAt(int channelIndex, int sampleIndex, int newValue) {
        channels.get(channelIndex).put(sampleIndex, (byte) (saturateIfNeeded(newValue) & 0xFF));
    }

    private int signedToUnsignedByte(byte b) {
        int value;
        if (b < 0) {
            value = 256 + b;
        } else {
            value = b;
        }
        return value;
    }

    private int saturateIfNeeded(int i) {
        if (i > 255) {
            return 255;
        } else if (i < 0) {
            return 0;
        } else {
            return i;
        }
    }

    public TimelineLength getLength() {
        return length;
    }

    public int getRescaledSample(int channel, int rescaleToBits, int rescaleToSamples, int position) {
        double scaledPosition = ((double) samples / rescaleToBits) * position;
        int samplePosition1 = (int) Math.ceil(scaledPosition);
        int samplePosition2 = (int) Math.floor(scaledPosition);
        int firstSample = getSampleAt(channel, samplePosition1);
        int secondSample = getSampleAt(channel, samplePosition2);

        // todo: sample scale

        return (firstSample + secondSample) / 2;
    }

}
