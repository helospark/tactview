package com.helospark.tactview.ui.javafx.audio;

import com.helospark.lightdi.annotation.Configuration;
import com.helospark.tactview.core.timeline.AudioFrameResult;

@Configuration
public class JavaByteArrayConverter {

    public byte[] convert(AudioFrameResult audioFrameResult, int bytes, int samples, int channels) {
        double length = audioFrameResult.getLength().getSeconds().doubleValue();
        int numberOfSamplesPerChannel = (int) Math.ceil(bytes * samples * length);
        int numberOfChannels = channels;
        int originalNumberOfChannels = audioFrameResult.getChannels().size();
        int fullLength = numberOfChannels * numberOfSamplesPerChannel;
        byte[] result = new byte[fullLength];

        for (int sample = 0; sample < samples * length; ++sample) {
            for (int channel = 0; channel < numberOfChannels; ++channel) {
                if (channel < originalNumberOfChannels) {
                    int rescaledSample = audioFrameResult.getRescaledSample(channel, bytes, samples, sample);
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
