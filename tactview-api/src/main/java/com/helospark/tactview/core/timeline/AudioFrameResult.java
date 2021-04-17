package com.helospark.tactview.core.timeline;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.helospark.tactview.core.decoder.framecache.GlobalMemoryManagerAccessor;

public class AudioFrameResult {
    private final List<ByteBuffer> channels;
    private final int samples;
    private final int bytesPerSample;

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

    public int getNumberSamples() {
        return channels.get(0).capacity() / bytesPerSample;
    }

    public AudioFrameResult makeHeapCopy() {
        List<ByteBuffer> heapChannels = new ArrayList<>();
        for (int i = 0; i < channels.size(); ++i) {
            ByteBuffer buffer = ByteBuffer.allocate(channels.get(i).capacity());
            for (int j = 0; j < channels.get(i).capacity(); ++j) {
                buffer.put(channels.get(i).get(j));
            }
            heapChannels.add(buffer);
        }
        return new AudioFrameResult(heapChannels, samples, bytesPerSample);
    }

    public int getSampleAt(int channelIndex, int sampleIndex) {
        int byteOffset = sampleIndex * bytesPerSample;
        ByteBuffer channelBuffer = channels.get(channelIndex);
        if (byteOffset + bytesPerSample > channelBuffer.capacity()) {
            return 0;
        }
        if (bytesPerSample == 2) {
            return channelBuffer.getShort(byteOffset);
        } else if (bytesPerSample == 1) {
            return channelBuffer.get(byteOffset);
        } else {
            return channelBuffer.getInt(byteOffset);
        }
    }

    public float getNormalizedSampleAt(int channelIndex, int sampleIndex) {
        int byteOffset = sampleIndex * bytesPerSample;
        ByteBuffer channelBuffer = channels.get(channelIndex);
        if (byteOffset + bytesPerSample > channelBuffer.capacity()) {
            return 0;
        }
        if (bytesPerSample == 2) {
            return channelBuffer.getShort(byteOffset) / ((float) Short.MAX_VALUE);
        } else if (bytesPerSample == 1) {
            return channelBuffer.get(byteOffset) / ((float) Byte.MAX_VALUE);
        } else {
            return channelBuffer.getInt(byteOffset) / ((float) Integer.MAX_VALUE);
        }
    }

    public static int unsignedByteToInt(byte b) {
        return b & 0xFF;
    }

    public void setNormalizedSampleAt(int channelIndex, int sampleIndex, float newValue) {
        setSampleAt(channelIndex, sampleIndex, (int) (newValue * (1 << (bytesPerSample * 8 - 1))));
    }

    public void setSampleAt(int channelIndex, int sampleIndex, int newValue) {
        ByteBuffer channel = channels.get(channelIndex);
        int saturatedValue = saturateIfNeeded(newValue, (long) 1 << (bytesPerSample * 8 - 1));
        int byteOffset = sampleIndex * bytesPerSample;
        if (bytesPerSample == 1) {
            channel.put(byteOffset, (byte) saturatedValue);
        } else if (bytesPerSample == 2) {
            channel.putShort(byteOffset, (short) saturatedValue);
        } else {
            channel.putInt(byteOffset, saturatedValue);
        }
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
            return TimelineLength.ofSeconds((double) channels.get(0).capacity() / (this.samples * this.bytesPerSample));
        } else {
            return TimelineLength.ofZero();
        }
    }

    public int getRescaledSample(int channel, int rescaleToBytes, int rescaleToSamples, int position) {
        double scaledPosition = ((double) samples / rescaleToSamples) * position;
        int samplePosition1 = (int) Math.ceil(scaledPosition);
        int samplePosition2 = (int) Math.floor(scaledPosition);
        int firstSample = getSampleAt(channel, samplePosition1);

        int scaleBits = (rescaleToBytes - bytesPerSample) * 8;

        int unscaledResult;
        if (samplePosition1 == samplePosition2) {
            unscaledResult = firstSample;
        } else {
            int secondSample = getSampleAt(channel, samplePosition2);
            double distance = (scaledPosition - samplePosition2);
            unscaledResult = (int) (firstSample * distance + secondSample * (1.0 - distance));
        }

        int scaledResult;
        if (scaleBits > 0) {
            long scaleValue = 1 << scaleBits;
            scaledResult = (int) (unscaledResult * scaleValue);
        } else {
            scaledResult = unscaledResult / (1 << -scaleBits);
        }

        if (rescaleToBytes == 2) {
            return (short) scaledResult;
        } else if (rescaleToBytes == 1) {
            return (byte) scaledResult;
        } else {
            return scaledResult;
        }
    }

    public static AudioFrameResult sameSizeAndFormatAs(AudioFrameResult input) {
        List<ByteBuffer> newChannels = input.getChannels()
                .stream()
                .map(channelBuffer -> GlobalMemoryManagerAccessor.memoryManager.requestBuffer(channelBuffer.capacity()))
                .collect(Collectors.toList());

        return new AudioFrameResult(newChannels, input.getSamplePerSecond(), input.getBytesPerSample());
    }

    public boolean isEmpty() {
        return this.channels.isEmpty() || this.getNumberSamples() == 0;
    }

    public AudioFrameResult onHeapCopy() {
        List<ByteBuffer> newChannels = this.getChannels()
                .stream()
                .map(channelBuffer -> {
                    ByteBuffer heapBuffer = ByteBuffer.allocate(channelBuffer.capacity());
                    for (int i = 0; i < channelBuffer.capacity(); ++i) {
                        heapBuffer.put(i, channelBuffer.get(i));
                    }
                    return heapBuffer;
                })
                .collect(Collectors.toList());

        return new AudioFrameResult(newChannels, this.getSamplePerSecond(), this.getBytesPerSample());
    }

}
