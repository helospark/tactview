package com.helospark.tactview.ui.javafx.audio;

import com.helospark.lightdi.annotation.Configuration;
import com.helospark.tactview.core.timeline.AudioFrameResult;

@Configuration
public class JavaByteArrayConverter {

    /**
     * Converts input into format that can be used to stream audio via Java Sound API. Bytes and bytes/samples is not touched
     * @param audioFrameResult input
     * @param channels number of channels
     * @return converted sound
     */
    public byte[] convert(AudioFrameResult audioFrameResult, int channels) {
        int bytes = audioFrameResult.getBytesPerSample();
        int numberOfSamplesPerChannel = audioFrameResult.getNumberSamples();
        int numberOfChannels = channels;
        int originalNumberOfChannels = audioFrameResult.getChannels().size();
        int fullLength = numberOfChannels * numberOfSamplesPerChannel * bytes;
        byte[] result = new byte[fullLength];

        for (int sample = 0; sample < numberOfSamplesPerChannel; ++sample) {
            for (int channel = 0; channel < numberOfChannels; ++channel) {
                if (channel < originalNumberOfChannels) {
                    int rescaledSample = audioFrameResult.getRescaledSample(channel, audioFrameResult.getBytesPerSample(), audioFrameResult.getSamplePerSecond(), sample);
                    byte[] bytesToAdd = toBytes(rescaledSample, bytes);
                    for (int i = 0; i < bytes; ++i) {
                        result[sample * bytes * numberOfChannels + channel * bytes + i] = bytesToAdd[i];
                    }
                } else {
                    for (int i = 0; i < bytes; ++i) {
                        result[sample * bytes * numberOfChannels + channel * bytes + i] = 0;
                    }
                }
            }
        }

        return result;
    }

    private byte[] toBytes(int sample, int bytes) {
        byte[] result = new byte[bytes];
        for (int i = 0; i < bytes; ++i) {
            result[i] = (byte) ((sample >>> ((bytes - i - 1) * 8)) & 0xFF);
        }
        return result;
    }

}
