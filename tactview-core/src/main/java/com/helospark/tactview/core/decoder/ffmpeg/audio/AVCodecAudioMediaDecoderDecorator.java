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
    private static final int MINIMUM_LENGTH_TO_READ = 60;
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
        String hashKey = request.getFile().getAbsolutePath() + " " + request.getExpectedSampleRate() + " " + request.getExpectedBytesPerSample();
        int startSample = secondsToBytes(request.getStart().getSeconds(), request.getExpectedBytesPerSample(), sampleRate);

        Optional<MediaHashValue> cachedResult = mediaCache.findInCache(hashKey, startSample);
        if (cachedResult.isPresent()) {
            int frameStartIndexInBuffer = cachedResult.get().frameStart;
            List<ByteBuffer> foindInCache = cachedResult.get().frames;

            return copyRelevantParts(request, startSample - frameStartIndexInBuffer, foindInCache);
        } else {
            FileReadResult result = readFromFile(request);
            int realStartSample = secondsToBytes(result.actualStartPosition, request.getExpectedBytesPerSample(), sampleRate);
            mediaCache.cacheMedia(hashKey, new MediaHashValue(realStartSample, realStartSample + result.actualLength, result.data), false);

            return copyRelevantParts(request, startSample - realStartSample, result.data);
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
        if (size <= 0) {
            return new MediaDataResponse(List.of()); // TODO: This should not happen, but it does sometimes :(
        }

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

    private FileReadResult readFromFile(AudioMediaDataRequest request) {
        BigDecimal remainder = request.getStart().getSeconds().remainder(BigDecimal.valueOf(MINIMUM_LENGTH_TO_READ));
        BigDecimal correctedStartPosition = request.getStart().getSeconds().subtract(remainder);
        int bufferSize = request.getExpectedBytesPerSample() * request.getExpectedSampleRate() * MINIMUM_LENGTH_TO_READ;
        AVCodecAudioRequest nativeRequest = new AVCodecAudioRequest();
        nativeRequest.numberOfChannels = request.getExpectedChannels();
        nativeRequest.bufferSize = bufferSize;
        nativeRequest.sampleRate = request.getExpectedSampleRate();
        nativeRequest.bytesPerSample = request.getExpectedBytesPerSample();
        nativeRequest.path = request.getFile().getAbsolutePath();
        nativeRequest.startMicroseconds = correctedStartPosition.multiply(BigDecimal.valueOf(1000000)).longValue();
        nativeRequest.channels = new FFMpegFrame();
        FFMpegFrame[] channels = (FFMpegFrame[]) nativeRequest.channels.toArray(request.getExpectedChannels());
        List<ByteBuffer> result = new ArrayList<>();

        System.out.println("Actually reading audio file " + request.getStart().getSeconds());

        for (int i = 0; i < nativeRequest.numberOfChannels; ++i) {
            ByteBuffer buffer = GlobalMemoryManagerAccessor.memoryManager.requestBuffer(bufferSize);
            channels[i].data = buffer;
            result.add(buffer);
        }

        int readBytes = implementation.readAudio(nativeRequest);

        return new FileReadResult(correctedStartPosition, readBytes, result);
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

    private int secondsToBytes(BigDecimal startSeconds, int bytesPerSample, BigDecimal sampleRate) {
        return startSeconds.multiply(sampleRate).intValue() * bytesPerSample;
    }

    private class FileReadResult {
        BigDecimal actualStartPosition;
        int actualLength;
        List<ByteBuffer> data;

        public FileReadResult(BigDecimal actualStartPosition, int actualLength, List<ByteBuffer> data) {
            this.actualStartPosition = actualStartPosition;
            this.actualLength = actualLength;
            this.data = data;
        }

    }

}
