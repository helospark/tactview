package com.helospark.tactview.core.decoder.ffmpeg;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.decoder.MediaDataResponse;
import com.helospark.tactview.core.decoder.VideoMediaDataRequest;
import com.helospark.tactview.core.decoder.VideoMetadata;
import com.helospark.tactview.core.decoder.VisualMediaDecoder;
import com.helospark.tactview.core.decoder.framecache.GlobalMemoryManagerAccessor;
import com.helospark.tactview.core.decoder.framecache.MediaCache;
import com.helospark.tactview.core.decoder.framecache.MediaCache.MediaHashValue;
import com.helospark.tactview.core.timeline.TimelineLength;
import com.helospark.tactview.core.util.cacheable.Cacheable;

@Component
public class FFmpegBasedMediaDecoderDecorator implements VisualMediaDecoder {
    private static final int CHUNK_SIZE = 30;
    private FFmpegBasedMediaDecoderImplementation implementation;
    private MediaCache mediaCache;

    public FFmpegBasedMediaDecoderDecorator(FFmpegBasedMediaDecoderImplementation implementation, MediaCache mediaCache) {
        this.implementation = implementation;
        this.mediaCache = mediaCache;
    }

    @Override
    @Cacheable
    public VideoMetadata readMetadata(File file) {
        if (!file.exists()) {
            throw new RuntimeException(file.getAbsolutePath() + " does not exists");
        }

        FFmpegResult result = implementation.readMediaMetadata(file.getAbsolutePath());
        return VideoMetadata.builder()
                .withFps(result.fps)
                .withHeight(result.height)
                .withWidth(result.width)
                .withLength(TimelineLength.ofMicroseconds(result.lengthInMicroseconds))
                .build();
    }

    @Override
    public MediaDataResponse readFrames(VideoMediaDataRequest request) {
        VideoMetadata metadata = (VideoMetadata) request.getMetadata();
        int numberOfFrames = calculateNumberOfFrames(request);
        int startFrame = request.getStart().getSeconds().multiply(new BigDecimal(metadata.getFps())).intValue();
        String filePath = request.getFile().getAbsolutePath();

        Map<Integer, ByteBuffer> framesFromCache = findInCache(request, numberOfFrames, startFrame, filePath);

        List<ByteBuffer> result = new ArrayList<>(request.getNumberOfFrames());

        if (!framesFromCache.isEmpty()) { // todo handle frames partially in cache
            for (int i = 0; i < request.getNumberOfFrames(); ++i) {
                result.add(GlobalMemoryManagerAccessor.memoryManager.requestBuffer(request.getWidth() * request.getHeight() * 4));
            }
            copyToResult(result, framesFromCache.values());
        } else {
            System.out.println("Reading " + startFrame + " " + numberOfFrames);
            // Always read in chunks to minimize overhead
            int additionalFramesToReadInBeginning = startFrame % CHUNK_SIZE;
            int newStartFrame = startFrame - additionalFramesToReadInBeginning;
            if (newStartFrame < 0) {
                newStartFrame = 0;
            }

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

    private int calculateNumberOfFrames(VideoMediaDataRequest request) {
        int numberOfFrames = 1;
        if (request.getLength() != null) {
            numberOfFrames = lengthToFrames(request.getLength(), ((VideoMetadata) request.getMetadata()).getFps());
        } else {
            numberOfFrames = request.getNumberOfFrames();
        }
        return numberOfFrames;
    }

    private List<ByteBuffer> readResultBetweenIndices(List<ByteBuffer> readFrames, int additionalFramesToReadInBeginning, int numberOfFrames) {
        List<ByteBuffer> result;
        int arrayEndIndex = additionalFramesToReadInBeginning + numberOfFrames;
        if (arrayEndIndex > readFrames.size()) {
            arrayEndIndex = readFrames.size();
        }
        result = readFrames.subList(additionalFramesToReadInBeginning, arrayEndIndex);
        // TODO: clear this mess up
        for (int i = 0; i < additionalFramesToReadInBeginning; ++i) {
            GlobalMemoryManagerAccessor.memoryManager.returnBuffer(readFrames.get(i));
        }
        for (int i = arrayEndIndex; i < readFrames.size(); ++i) {
            GlobalMemoryManagerAccessor.memoryManager.returnBuffer(readFrames.get(i));
        }
        return result;
    }

    private void storeInCache(VideoMediaDataRequest request, int startFrame, String filePath, List<ByteBuffer> result) {
        String key = createHashKey(filePath, request);
        MediaHashValue value = new MediaHashValue(startFrame, startFrame + result.size(), result);
        mediaCache.cacheMedia(key, value);
    }

    private String createHashKey(String filePath, VideoMediaDataRequest request) {
        return filePath + " " + request.getWidth() + " " + request.getHeight();
    }

    private Map<Integer, ByteBuffer> findInCache(VideoMediaDataRequest request, int numberOfFrames, int startFrame, String filePath) {
        Map<Integer, ByteBuffer> frames = new TreeMap<>();
        int endFrames = startFrame + numberOfFrames;
        for (int frameToFindInCache = startFrame; frameToFindInCache < endFrames; ++frameToFindInCache) {
            Optional<MediaHashValue> found = mediaCache.findInCache(createHashKey(filePath, request), frameToFindInCache);
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

    private ByteBuffer[] readFromFile(VideoMediaDataRequest request, int startFrame, int numberOfFrames, String filePath) {
        FFmpegImageRequest ffmpegRequest = new FFmpegImageRequest();
        ffmpegRequest.numberOfFrames = numberOfFrames;
        ffmpegRequest.height = request.getHeight();
        ffmpegRequest.width = request.getWidth();
        ffmpegRequest.path = filePath;

        ffmpegRequest.startMicroseconds = frameToTimestamp(startFrame, ((VideoMetadata) request.getMetadata()).getFps())
                .getSeconds()
                .multiply(BigDecimal.valueOf(1000000L))
                .longValue();

        ByteBuffer[] buffers = new ByteBuffer[numberOfFrames];
        ffmpegRequest.frames = new FFMpegFrame();
        System.out.println("Requesting frames " + numberOfFrames);
        FFMpegFrame[] array = (FFMpegFrame[]) ffmpegRequest.frames.toArray(numberOfFrames);
        for (int i = 0; i < numberOfFrames; ++i) {
            array[i].data = GlobalMemoryManagerAccessor.memoryManager.requestBuffer(ffmpegRequest.width * ffmpegRequest.height * 4);
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

    private void copyToResult(List<ByteBuffer> result, Collection<ByteBuffer> collection) {
        int i = 0;
        for (ByteBuffer elementToCopy : collection) {
            ByteBuffer copyTo = result.get(i);

            copyTo.position(0);
            elementToCopy.position(0);
            copyTo.put(elementToCopy);

            ++i;
        }

    }

}
