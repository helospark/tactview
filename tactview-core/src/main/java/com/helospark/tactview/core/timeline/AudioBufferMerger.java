package com.helospark.tactview.core.timeline;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.decoder.framecache.GlobalMemoryManagerAccessor;

@Component
public class AudioBufferMerger {

    public AudioFrameResult mergeBuffers(List<AudioFrameResult> renderAudioFrameData) {
        if (renderAudioFrameData.isEmpty()) {
            return new AudioFrameResult(Collections.emptyList(), 0, 0);
        }
        int numberOfChannels = calculateNumberOfChannels(renderAudioFrameData);
        int maximumQuality = calculateMaximumQuality(renderAudioFrameData);
        int maximumByteLength = calculateMaximumBitLength(renderAudioFrameData);
        // TODO calculate this:
        int length = renderAudioFrameData.get(0).getChannels().get(0).capacity();
        List<ByteBuffer> channels = new ArrayList<>();
        for (int i = 0; i < numberOfChannels; ++i) {
            ByteBuffer channelData = GlobalMemoryManagerAccessor.memoryManager.requestBuffer(length);
            channels.add(channelData);
        }
        AudioFrameResult audioFrameResult = new AudioFrameResult(channels, maximumQuality, maximumByteLength);

        for (AudioFrameResult data : renderAudioFrameData) {
            for (int channelIndex = 0; channelIndex < data.getChannels().size(); ++channelIndex) {
                for (int sampleIndex = 0; sampleIndex < length; sampleIndex += maximumByteLength) {
                    int newData = data.getRescaledSample(channelIndex, maximumByteLength, maximumQuality, sampleIndex);
                    int oldData = audioFrameResult.getSampleAt(channelIndex, sampleIndex);
                    audioFrameResult.setSampleAt(channelIndex, sampleIndex, newData + oldData);
                }
            }
        }

        return audioFrameResult;
    }

    private int calculateMaximumBitLength(List<AudioFrameResult> renderAudioFrameData) {
        int bytesPerSample = 0;
        for (var data : renderAudioFrameData) {
            if (data.getBytesPerSample() > bytesPerSample) {
                bytesPerSample = data.getBytesPerSample();
            }
        }
        return bytesPerSample;
    }

    private int calculateMaximumQuality(List<AudioFrameResult> renderAudioFrameData) {
        int quality = 0;
        for (var data : renderAudioFrameData) {
            if (data.getSamplePerSecond() > quality) {
                quality = data.getSamplePerSecond();
            }
        }
        return quality;
    }

    private int calculateNumberOfChannels(List<AudioFrameResult> renderAudioFrameData) {
        int channels = 0;
        for (var data : renderAudioFrameData) {
            if (data.getChannels().size() > channels) {
                channels = data.getChannels().size();
            }
        }
        return channels;
    }

}
