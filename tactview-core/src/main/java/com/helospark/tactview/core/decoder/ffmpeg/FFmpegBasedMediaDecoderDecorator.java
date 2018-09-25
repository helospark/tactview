package com.helospark.tactview.core.decoder.ffmpeg;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
    private static final int CHUNK_SIZE = 30;
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
        int numberOfFrames = calculateNumberOfFrames(request);
        int startFrame = request.getStart().getSeconds().multiply(new BigDecimal(request.getMetadata().getFps())).intValue();
        String filePath = request.getFile().getAbsolutePath();

        Map<Integer, ByteBuffer> framesFromCache = findInCache(request, numberOfFrames, startFrame, filePath);

        List<ByteBuffer> result;
        if (!framesFromCache.isEmpty()) { // todo handle frames partially in cache
            result = new ArrayList<>(framesFromCache.values());
        } else {
            // Always read in chunks to minimize overhead
            int additionalFramesToReadInBeginning = startFrame % CHUNK_SIZE;
            int newStartFrame = startFrame - additionalFramesToReadInBeginning;

            int newEndFrame = newStartFrame;
            while (startFrame + numberOfFrames > newEndFrame) {
                newEndFrame += CHUNK_SIZE;
            }

            int newNumberOfFrame = newEndFrame - newStartFrame;
            System.out.println("Reading in chunks: " + newNumberOfFrame + " " + newStartFrame);
            List<ByteBuffer> readFrames = Arrays.asList(readFromFile(request, newStartFrame, newNumberOfFrame, filePath));

            storeInCache(request, newStartFrame, filePath, readFrames);

            result = readResultBetweenIndices(readFrames, additionalFramesToReadInBeginning, numberOfFrames);
        }
        return new MediaDataResponse(result);
    }

    private int calculateNumberOfFrames(MediaDataRequest request) {
        int numberOfFrames = 1;
        if (request.getLength() != null) {
            numberOfFrames = lengthToFrames(request.getLength(), request.getMetadata().getFps());
        } else {
            numberOfFrames = request.getNumberOfFrames();
        }
        return numberOfFrames;
    }

    private List<ByteBuffer> readResultBetweenIndices(List<ByteBuffer> readFrames, int additionalFramesToReadInBeginning, int numberOfFrames) {
        List<ByteBuffer> result;
        int arrayEndIndex = additionalFramesToReadInBeginning + numberOfFrames + 1;
        if (arrayEndIndex >= readFrames.size()) {
            arrayEndIndex = readFrames.size() - 1;
        }
        result = readFrames.subList(additionalFramesToReadInBeginning, arrayEndIndex);
        return result;
    }

    private void storeInCache(MediaDataRequest request, int startFrame, String filePath, List<ByteBuffer> result) {
        MediaHashKey key = new MediaHashKey(filePath, request.getWidth(), request.getHeight());
        MediaHashValue value = new MediaHashValue(startFrame, result);
        mediaCache.cacheMedia(key, value);
    }

    private Map<Integer, ByteBuffer> findInCache(MediaDataRequest request, int numberOfFrames, int startFrame, String filePath) {
        Map<Integer, ByteBuffer> frames = new TreeMap<>();
        int endFrames = startFrame + numberOfFrames;
        for (int frameToFindInCache = startFrame; frameToFindInCache < endFrames; ++frameToFindInCache) {
            Optional<MediaHashValue> found = mediaCache.findInCache(new MediaHashKey(filePath, request.getWidth(), request.getHeight()), frameToFindInCache);
            if (found.isPresent()) {
                MediaHashValue foundCache = found.get();
                int startCopyFrom = frameToFindInCache - foundCache.frameStart;
                for (int cacheFrameIndex = startCopyFrom; frameToFindInCache < endFrames && cacheFrameIndex < foundCache.frames.size(); ++frameToFindInCache, ++cacheFrameIndex) {
                    frames.put(frameToFindInCache, foundCache.frames.get(cacheFrameIndex));
                }
            }
        }
        return frames;
    }

    private ByteBuffer[] readFromFile(MediaDataRequest request, int startFrame, int numberOfFrames, String filePath) {
        FFmpegImageRequest ffmpegRequest = new FFmpegImageRequest();
        ffmpegRequest.numberOfFrames = numberOfFrames;
        ffmpegRequest.height = request.getHeight();
        ffmpegRequest.width = request.getWidth();
        ffmpegRequest.path = filePath;

        ffmpegRequest.startMicroseconds = frameToTimestamp(startFrame, request.getMetadata().getFps())
                .getSeconds()
                .multiply(BigDecimal.valueOf(1000000L))
                .longValue();

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

    private int lengthToFrames(TimelineLength length, double fps) {
        return (int) (length.getSeconds().doubleValue() * fps);
    }

    private TimelineLength frameToTimestamp(int startFrame, double fps) {
        return new TimelineLength(BigDecimal.valueOf(startFrame).divide(new BigDecimal(fps), 10, RoundingMode.HALF_DOWN));
    }

}
