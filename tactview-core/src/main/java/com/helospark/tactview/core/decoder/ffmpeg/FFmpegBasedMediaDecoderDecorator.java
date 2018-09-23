package com.helospark.tactview.core.decoder.ffmpeg;

import java.io.File;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.decoder.MediaDataRequest;
import com.helospark.tactview.core.decoder.MediaDataResponse;
import com.helospark.tactview.core.decoder.MediaDecoder;
import com.helospark.tactview.core.decoder.MediaMetadata;
import com.helospark.tactview.core.decoder.framecache.MediaCache;
import com.helospark.tactview.core.decoder.framecache.MediaCache.MediaHashKey;
import com.helospark.tactview.core.decoder.framecache.MediaCache.MediaHashValue;
import com.helospark.tactview.core.timeline.TimelineLength;
import com.helospark.tactview.core.util.cacheable.Cacheable;

@Component
public class FFmpegBasedMediaDecoderDecorator implements MediaDecoder {
    private FFmpegBasedMediaDecoderImplementation implementation;
    private MediaCache mediaCache;

    public FFmpegBasedMediaDecoderDecorator(FFmpegBasedMediaDecoderImplementation implementation, MediaCache mediaCache) {
        this.implementation = implementation;
        this.mediaCache = mediaCache;
    }

    @Override
    @Cacheable
    public MediaMetadata readMetadata(File file) {
        if (!file.exists()) {
            throw new RuntimeException(file.getAbsolutePath() + " does not exists");
        }

        FFmpegResult result = implementation.readMediaMetadata(file.getAbsolutePath());
        return MediaMetadata.builder()
                .withFps(result.fps)
                .withHeight(result.height)
                .withWidth(result.width)
                .withLength(TimelineLength.ofMicroseconds(result.lengthInMicroseconds))
                .build();
    }

    public MediaDataResponse readFrames(MediaDataRequest request) {
        int numberOfFrames = 1;
        if (request.getLength() != null) {
            numberOfFrames = (int) (request.getLength().getSeconds().doubleValue() * request.getMetadata().getFps());
        } else {
            numberOfFrames = request.getNumberOfFrames();
        }
        int startFrame = request.getStart().getSeconds().multiply(new BigDecimal(request.getMetadata().getFps())).intValue();
        String filePath = request.getFile().getAbsolutePath();

        Map<Integer, ByteBuffer> framesFromCache = findInCache(request, numberOfFrames, startFrame, filePath);
        List<ByteBuffer> result;
        if (!framesFromCache.isEmpty()) { // todo handle not all found in cache
            result = new ArrayList<>(framesFromCache.values());
        } else {
            result = Arrays.asList(readFromFile(request, numberOfFrames, filePath));
            storeInCache(request, startFrame, filePath, result);
        }
        return new MediaDataResponse(result);
    }

    private void storeInCache(MediaDataRequest request, int startFrame, String filePath, List<ByteBuffer> result) {
        MediaHashKey key = new MediaHashKey(filePath, request.getWidth(), request.getHeight());
        MediaHashValue value = new MediaHashValue(startFrame, result);
        mediaCache.cacheMedia(key, value);
    }

    private Map<Integer, ByteBuffer> findInCache(MediaDataRequest request, int numberOfFrames, int startFrame, String filePath) {
        Map<Integer, ByteBuffer> frames = new TreeMap<>();
        for (int i = startFrame; i < startFrame + numberOfFrames; ++i) {
            Optional<MediaHashValue> found = mediaCache.findInCache(new MediaHashKey(filePath, request.getWidth(), request.getHeight()), startFrame);
            if (found.isPresent()) {
                MediaHashValue foundCache = found.get();
                int startCopyFrom = i - foundCache.frameStart;
                for (int j = startCopyFrom; i < startFrame + numberOfFrames && j < foundCache.frames.size(); ++i, ++j) {
                    frames.put(i, foundCache.frames.get(j));
                }
            }
        }
        return frames;
    }

    private ByteBuffer[] readFromFile(MediaDataRequest request, int numberOfFrames, String filePath) {
        FFmpegImageRequest ffmpegRequest = new FFmpegImageRequest();
        ffmpegRequest.numberOfFrames = numberOfFrames;
        ffmpegRequest.height = request.getHeight();
        ffmpegRequest.width = request.getWidth();
        ffmpegRequest.path = filePath;
        ffmpegRequest.startMicroseconds = request.getStart().getSeconds().multiply(BigDecimal.valueOf(1000000L)).longValue();

        ByteBuffer[] buffers = new ByteBuffer[numberOfFrames];
        ffmpegRequest.frames = new FFMpegFrame();
        FFMpegFrame[] array = (FFMpegFrame[]) ffmpegRequest.frames.toArray(numberOfFrames);
        for (int i = 0; i < numberOfFrames; ++i) {
            array[i].data = mediaCache.requestBuffers(ffmpegRequest.width * ffmpegRequest.height * 4, 1).get(0);
            buffers[i] = array[i].data;
        }

        implementation.readFrames(ffmpegRequest);
        return buffers;
    }

}
