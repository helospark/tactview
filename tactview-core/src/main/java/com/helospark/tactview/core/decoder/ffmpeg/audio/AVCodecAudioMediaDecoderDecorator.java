package com.helospark.tactview.core.decoder.ffmpeg.audio;

import java.io.File;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.decoder.AudioMediaDataRequest;
import com.helospark.tactview.core.decoder.AudioMediaDecoder;
import com.helospark.tactview.core.decoder.AudioMediaMetadata;
import com.helospark.tactview.core.decoder.MediaDataResponse;
import com.helospark.tactview.core.decoder.ffmpeg.FFMpegFrame;
import com.helospark.tactview.core.decoder.framecache.GlobalMemoryManagerAccessor;
import com.helospark.tactview.core.decoder.framecache.MediaCache;
import com.helospark.tactview.core.decoder.framecache.MediaCache.MediaHashValue;
import com.helospark.tactview.core.decoder.framecache.MemoryManager;
import com.helospark.tactview.core.timeline.TimelineLength;

@Component
public class AVCodecAudioMediaDecoderDecorator implements AudioMediaDecoder {
    private AVCodecBasedAudioMediaDecoderImplementation implementation;
    private MediaCache mediaCache;
    private MemoryManager memoryManager;

    public AVCodecAudioMediaDecoderDecorator(AVCodecBasedAudioMediaDecoderImplementation implementation, MediaCache mediaCache, MemoryManager memoryManager) {
        this.implementation = implementation;
        this.mediaCache = mediaCache;
        this.memoryManager = memoryManager;
    }

    @Override
    public MediaDataResponse readFrames(AudioMediaDataRequest request) {
        BigDecimal sampleRate = new BigDecimal(request.getExpectedSampleRate());
        String hashKey = request.getFile().getAbsolutePath() + " " + request.getExpectedSampleRate();
        int startSample = request.getStart().getSeconds().multiply(sampleRate).intValue() * request.getExpectedBytesPerSample();

        Optional<MediaHashValue> cachedResult = mediaCache.findInCache(hashKey, startSample);
        if (cachedResult.isPresent()) {
            int frameStartIndexInBuffer = cachedResult.get().frameStart;
            List<ByteBuffer> foindInCache = cachedResult.get().frames;

            return copyRelevantParts(request, startSample - frameStartIndexInBuffer, foindInCache);
        } else {
            MediaDataResponse result = readFromFile(request);
            mediaCache.cacheMedia(hashKey, new MediaHashValue(startSample, result.getFrames().get(0).capacity(), result.getFrames()), false);

            return copyRelevantParts(request, 0, result.getFrames());
        }
    }

    private MediaDataResponse copyRelevantParts(AudioMediaDataRequest request, int startSample, List<ByteBuffer> foindInCache) {
        BigDecimal sampleRate = new BigDecimal(request.getExpectedSampleRate());
        int endSample = startSample + request.getLength().getSeconds()
                .multiply(sampleRate)
                .intValue() * request.getExpectedBytesPerSample();
        List<ByteBuffer> buffers = new ArrayList<>();
        int to = Math.min(endSample, foindInCache.get(0).capacity());
        int size = to - startSample;
        for (int channel = 0; channel < request.getExpectedChannels(); ++channel) {
            ByteBuffer channelBuffer = memoryManager.requestBuffer(size);
            buffers.add(channelBuffer);
            for (int i = startSample, j = 0; i < to; ++i, ++j) {
                byte value = foindInCache.get(channel).get(i);
                channelBuffer.put(j, value);
            }
        }
        return new MediaDataResponse(buffers);
    }

    private MediaDataResponse readFromFile(AudioMediaDataRequest request) {
        int bufferSize = request.getExpectedBytesPerSample() * request.getExpectedSampleRate() * 30;
        AVCodecAudioRequest nativeRequest = new AVCodecAudioRequest();
        nativeRequest.numberOfChannels = request.getExpectedChannels();
        nativeRequest.bufferSize = bufferSize;
        nativeRequest.path = request.getFile().getAbsolutePath();
        nativeRequest.startMicroseconds = request.getStart().getSeconds().multiply(BigDecimal.valueOf(1000000)).longValue();
        nativeRequest.channels = new FFMpegFrame();
        FFMpegFrame[] channels = (FFMpegFrame[]) nativeRequest.channels.toArray(request.getExpectedChannels());
        List<ByteBuffer> result = new ArrayList<>();
        for (int i = 0; i < nativeRequest.numberOfChannels; ++i) {
            ByteBuffer buffer = GlobalMemoryManagerAccessor.memoryManager.requestBuffer(bufferSize);
            channels[i].data = buffer;
            result.add(buffer);
        }

        implementation.readAudio(nativeRequest);

        System.out.println();
        System.out.println("New on java side");
        for (int i = 0; i < 5000; ++i) {
            System.out.print(((int) result.get(0).get(i)) + " ");
        }
        System.out.println();

        return new MediaDataResponse(result);
    }

    @Override
    public AudioMediaMetadata readMetadata(File file) {
        AVCodecAudioMetadataResponse readMetadata = implementation.readMetadata(file.getAbsolutePath());
        return AudioMediaMetadata.builder()
                .withChannels(readMetadata.channels)
                .withSampleRate(readMetadata.sampleRate)
                .withBytesPerSample(readMetadata.bytesPerSample)
                .withLength(TimelineLength.ofMicroseconds(readMetadata.lengthInMicroseconds))
                .build();
    }

}
