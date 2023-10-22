package com.helospark.tactview.core.decoder.ffmpeg.audio;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.annotation.Qualifier;
import com.helospark.tactview.core.decoder.AudioMediaDataRequest;
import com.helospark.tactview.core.decoder.AudioMediaDecoder;
import com.helospark.tactview.core.decoder.AudioMediaMetadata;
import com.helospark.tactview.core.decoder.MediaDataResponse;
import com.helospark.tactview.core.decoder.ffmpeg.FFMpegFrame;
import com.helospark.tactview.core.decoder.framecache.GlobalMemoryManagerAccessor;
import com.helospark.tactview.core.decoder.framecache.MediaCache;
import com.helospark.tactview.core.decoder.framecache.MediaCache.MediaDataFrame;
import com.helospark.tactview.core.decoder.framecache.MediaCache.MediaHashValue;
import com.helospark.tactview.core.decoder.framecache.MemoryManager;
import com.helospark.tactview.core.timeline.TimelineLength;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.util.cacheable.Cacheable;

@Component
public class AVCodecAudioMediaDecoderDecorator implements AudioMediaDecoder {
    private static final Logger LOGGER = LoggerFactory.getLogger(AVCodecAudioMediaDecoderDecorator.class);
    private static final int MINIMUM_LENGTH_TO_READ = 30;
    private AVCodecBasedAudioMediaDecoderImplementation implementation;
    private MediaCache mediaCache;
    private MemoryManager memoryManager;

    private ThreadPoolExecutor prefetchExecutor;
    private Map<PrefetchKey, CompletableFuture<Void>> readFutures = new ConcurrentHashMap<>();

    public AVCodecAudioMediaDecoderDecorator(AVCodecBasedAudioMediaDecoderImplementation implementation, MediaCache mediaCache, MemoryManager memoryManager,
            @Qualifier("prefetchThreadPoolExecutorService") ThreadPoolExecutor prefetchExecutor) {
        this.implementation = implementation;
        this.mediaCache = mediaCache;
        this.memoryManager = memoryManager;
        this.prefetchExecutor = prefetchExecutor;
    }

    @Override
    public MediaDataResponse readFrames(AudioMediaDataRequest request) {
        BigDecimal sampleRate = new BigDecimal(request.getExpectedSampleRate());
        String hashKey = request.getFile().getAbsolutePath() + " " + request.getExpectedSampleRate() + " " + request.getExpectedBytesPerSample() + " " + request.getExpectedChannels();
        int startSample = secondsToBytes(request.getStart().getSeconds(), request.getExpectedBytesPerSample(), sampleRate);

        if (!request.isAvoidPrefetch()) {
            schedulePrefetchJobIfNeeded(request, hashKey);
        }

        PrefetchKey prefetchKey = new PrefetchKey(hashKey, getCacheStartPosition(request));
        CompletableFuture<Void> prefetchFuture = readFutures.get(prefetchKey);

        if (prefetchFuture != null) {
            LOGGER.debug("Waiting for the prefetch future at {}", prefetchKey);
            try {
                prefetchFuture.get(2000, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                LOGGER.error("Timeout while waiting for prefetch", e);
            }
        }

        Optional<MediaDataFrame> cachedResult = mediaCache.findInCache(hashKey, request.getStart().getSeconds());
        if (cachedResult.isPresent()) {
            MediaDataFrame foundInCache = cachedResult.get();
            List<ByteBuffer> dataFrames = foundInCache.allDataFrames;
            int realStartSample = startSample - secondsToBytes(foundInCache.startTime, request.getExpectedBytesPerSample(), sampleRate);

            return copyRelevantParts(request, realStartSample, dataFrames);
        } else {
            FileReadResult result = readFromFile(request);
            int realStartSample = secondsToBytes(result.data.startTime, request.getExpectedBytesPerSample(), sampleRate);
            mediaCache.cacheMedia(hashKey, new MediaHashValue(List.of(result.data), result.endPosition, result.data.startTime), false);

            return copyRelevantParts(request, startSample - realStartSample, result.data.allDataFrames);
        }
    }

    private void schedulePrefetchJobIfNeeded(AudioMediaDataRequest request, String hashKey) {
        BigDecimal correctedStartPosition = getCacheStartPosition(request).add(BigDecimal.valueOf(MINIMUM_LENGTH_TO_READ));
        BigDecimal distance = correctedStartPosition.subtract(request.getStart().getSeconds());
        PrefetchKey prefetchKey = new PrefetchKey(hashKey, correctedStartPosition);
        if (mediaCache.findInCache(hashKey, correctedStartPosition).isEmpty() &&
                distance.compareTo(BigDecimal.valueOf(4)) < 0 &&
                readFutures.get(prefetchKey) == null) {
            CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(() -> {
                LOGGER.debug("Prefetching audio (1) at {} due to read at {}", correctedStartPosition, request.getStart().getSeconds());
                if (mediaCache.findInCache(hashKey, correctedStartPosition).isEmpty()
                        && readFutures.get(prefetchKey) == null) {
                    LOGGER.debug("Prefetching audio at {} due to read at {}", correctedStartPosition, request.getStart().getSeconds());
                    AudioMediaDataRequest newRequest = AudioMediaDataRequest.builderFrom(request)
                            .withAvoidPrefetch(true)
                            .withStart(new TimelinePosition(correctedStartPosition))
                            .build();

                    MediaDataResponse result = readFrames(newRequest);

                    GlobalMemoryManagerAccessor.memoryManager.returnBuffers(result.getFrames());
                    LOGGER.debug("Prefetching audio done at {}", correctedStartPosition);
                }
                readFutures.remove(prefetchKey);
            }, prefetchExecutor);
            readFutures.put(prefetchKey, completableFuture);
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
        int oneAdditionalSample = request.getLength().getSeconds()
                .multiply(BigDecimal.valueOf(request.getExpectedSampleRate()))
                .intValue() * request.getExpectedBytesPerSample();
        BigDecimal correctedStartPosition = getCacheStartPosition(request);
        int bufferSize = request.getExpectedBytesPerSample() * request.getExpectedSampleRate() * MINIMUM_LENGTH_TO_READ + oneAdditionalSample;
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

        LOGGER.debug("Actually reading audio file from={}, position={}", correctedStartPosition, request.getStart().getSeconds());

        for (int i = 0; i < nativeRequest.numberOfChannels; ++i) {
            ByteBuffer buffer = GlobalMemoryManagerAccessor.memoryManager.requestBuffer(bufferSize);
            channels[i].data = buffer;
            result.add(buffer);
        }

        int readBytes = implementation.readAudio(nativeRequest);

        int correctedEndBytes = readBytes;
        if (readBytes >= bufferSize - 100) {
            correctedEndBytes = readBytes - oneAdditionalSample;
        } else {
            LOGGER.debug("Audio file end has been reached");
        }

        BigDecimal endPosition = correctedStartPosition.add(bytesToSeconds(correctedEndBytes, request.getExpectedBytesPerSample(), request.getExpectedSampleRate()));

        LOGGER.debug("Audio file read from {} to {}", correctedStartPosition, endPosition);

        return new FileReadResult(endPosition, new MediaDataFrame(result, correctedStartPosition));
    }

    private BigDecimal getCacheStartPosition(AudioMediaDataRequest request) {
        BigDecimal remainder = request.getStart().getSeconds().remainder(BigDecimal.valueOf(MINIMUM_LENGTH_TO_READ));
        BigDecimal correctedStartPosition = request.getStart().getSeconds().subtract(remainder);
        return correctedStartPosition;
    }

    @Override
    @Cacheable
    public AudioMediaMetadata readMetadata(File file) {
        AVCodecAudioMetadataResponse readMetadata = implementation.readMetadata(file.getAbsolutePath());
        return AudioMediaMetadata.builder()
                .withChannels(readMetadata.channels)
                .withSampleRate(readMetadata.sampleRate)
                .withBytesPerSample(readMetadata.bytesPerSample)
                .withBitRate(readMetadata.bitRate)
                .withLength(TimelineLength.ofMicroseconds(readMetadata.lengthInMicroseconds))
                .build();
    }

    private int secondsToBytes(BigDecimal startSeconds, int bytesPerSample, BigDecimal sampleRate) {
        return startSeconds.multiply(sampleRate).intValue() * bytesPerSample;
    }

    private BigDecimal bytesToSeconds(int bytes, int bytesPerSample, int sampleRate) {
        return BigDecimal.valueOf(bytes).divide(BigDecimal.valueOf(bytesPerSample * sampleRate), 20, RoundingMode.FLOOR);
    }

    private class FileReadResult {
        public BigDecimal endPosition;
        public MediaDataFrame data;

        public FileReadResult(BigDecimal endPosition, MediaDataFrame data) {
            this.endPosition = endPosition;
            this.data = data;
        }

    }

    static class PrefetchKey {
        String hashkey;
        BigDecimal position;

        public PrefetchKey(String hashkey, BigDecimal position) {
            this.hashkey = hashkey;
            this.position = position;
        }

        @Override
        public int hashCode() {
            return Objects.hash(hashkey, position);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            PrefetchKey other = (PrefetchKey) obj;
            return Objects.equals(hashkey, other.hashkey) && Objects.equals(position, other.position);
        }

        @Override
        public String toString() {
            return "PrefetchKey [hashkey=" + hashkey + ", position=" + position + "]";
        }

    }
}
