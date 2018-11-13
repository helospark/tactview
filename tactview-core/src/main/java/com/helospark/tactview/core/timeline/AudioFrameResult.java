package com.helospark.tactview.core.timeline;

import java.nio.ByteBuffer;
import java.util.List;

public class AudioFrameResult {
    private List<ByteBuffer> channels;
    private int samples;
    private int bytesPerSample;

    public AudioFrameResult(List<ByteBuffer> channels, int samplesPerSecond, int bytesPerSample) {
        this.channels = channels;
        this.samples = samplesPerSecond;
        this.bytesPerSample = bytesPerSample;
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
        int byteOffset = sampleIndex * bytesPerSample;
        ByteBuffer channelBuffer = channels.get(channelIndex);
        if (byteOffset + bytesPerSample > channelBuffer.capacity()) {
            return 0;
        }
        int result = 0;
        for (int i = 0; i < bytesPerSample; ++i) {
            result <<= 8;
            result |= unsignedByteToInt(channelBuffer.get(byteOffset + i));
        }
        return result;
    }

    public static int unsignedByteToInt(byte b) {
        return b & 0xFF;
    }

    public void setSampleAt(int channelIndex, int sampleIndex, int newValue) {
        ByteBuffer channel = channels.get(channelIndex);
        int saturatedValue = saturateIfNeeded(newValue, (long) 1 << (bytesPerSample * 8));
        for (int i = 0; i < bytesPerSample; ++i) {
            channel.put(sampleIndex * bytesPerSample + i, (byte) ((saturatedValue >> ((bytesPerSample - i - 1) * 8)) & 0xFF));
        }
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

    private int saturateIfNeeded(int i, long limit) {
        if (i >= limit) {
            return (int) (limit - 1);
        } else if (i < -limit) {
            return (int) -limit;
        } else {
            return i;
        }
    }

    public TimelineLength getLength() {
        if (channels.size() > 0) {
            return TimelineLength.ofSeconds((double) channels.get(0).capacity() / (this.samples));
        } else {
            return TimelineLength.ofZero();
        }
    }

    public int getRescaledSample(int channel, int rescaleToBytes, int rescaleToSamples, int position) {
        double scaledPosition = ((double) samples / rescaleToSamples) * position * bytesPerSample / rescaleToBytes;
        int samplePosition1 = (int) Math.ceil(scaledPosition);
        int samplePosition2 = (int) Math.floor(scaledPosition);
        int firstSample = getSampleAt(channel, samplePosition1);
        int secondSample = getSampleAt(channel, samplePosition2);

        int scaleBits = (rescaleToBytes - bytesPerSample) * 8;

        int unscaledResult = (firstSample + secondSample) / 2;

        if (scaleBits > 0) {
            long scaleValue = 1 << scaleBits;
            return (int) (unscaledResult * scaleValue);
        } else {
            long scaleValue = 1 << -scaleBits;
            return (int) (unscaledResult / scaleValue);
        }

    }

}
