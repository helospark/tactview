package com.helospark.tactview.core.decoder.ffmpeg;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.Striped;
import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.decoder.MediaDataResponse;
import com.helospark.tactview.core.decoder.VideoMediaDataRequest;
import com.helospark.tactview.core.decoder.VideoMetadata;
import com.helospark.tactview.core.decoder.VisualMediaDecoder;
import com.helospark.tactview.core.decoder.framecache.GlobalMemoryManagerAccessor;
import com.helospark.tactview.core.decoder.framecache.MediaCache;
import com.helospark.tactview.core.decoder.framecache.MediaCache.MediaHashValue;
import com.helospark.tactview.core.message.DropCachesMessage;
import com.helospark.tactview.core.timeline.TimelineLength;
import com.helospark.tactview.core.util.cacheable.Cacheable;
import com.helospark.tactview.core.util.messaging.MessagingServiceImpl;

@Component
public class FFmpegBasedMediaDecoderDecorator implements VisualMediaDecoder {
    private static final Logger LOGGER = LoggerFactory.getLogger(FFmpegBasedMediaDecoderDecorator.class);
    private static final int CHUNK_SIZE = 50;
    private Striped<Lock> duplicateReadLocks = Striped.lock(100);
    private FFmpegBasedMediaDecoderImplementation implementation;
    private MediaCache mediaCache;
    private MessagingServiceImpl messagingService;

    public FFmpegBasedMediaDecoderDecorator(FFmpegBasedMediaDecoderImplementation implementation, MediaCache mediaCache, MessagingServiceImpl messagingService) {
        this.implementation = implementation;
        this.mediaCache = mediaCache;
        this.messagingService = messagingService;
    }

    @PostConstruct
    public void init() {
        messagingService.register(DropCachesMessage.class, message -> mediaCache.dropCaches());
    }

    @Cacheable
    public VideoMetadata readMetadata(File file) {
        if (!file.exists()) {
            throw new RuntimeException(file.getAbsolutePath() + " does not exists");
        }

        FFmpegResult result = implementation.readMediaMetadata(file.getAbsolutePath());
        VideoMetadata resultMetadata = VideoMetadata.builder()
                .withFps(result.fps)
                .withHeight(result.height)
                .withWidth(result.width)
                .withBitRate(result.bitRate)
                .withLength(TimelineLength.ofMicroseconds(result.lengthInMicroseconds))
                .build();

        System.out.println("Video metadata read: " + resultMetadata);

        return resultMetadata;
    }

    @Override
    public MediaDataResponse readFrames(VideoMediaDataRequest request) {
        // TODO: eliminate copypaste copy paste from below

        VideoMetadata metadata = (VideoMetadata) request.getMetadata();
        int startFrame = request.getStart().getSeconds().multiply(new BigDecimal(metadata.getFps())).intValue();
        int additionalFramesToReadInBeginning = startFrame % CHUNK_SIZE;
        int newStartFrame = startFrame - additionalFramesToReadInBeginning;
        if (newStartFrame < 0) {
            newStartFrame = 0;
        }

        // end

        String readId = request.getFile().getAbsolutePath();
        readId += request.getWidth() + "x" + request.getHeight();

        Lock lock = duplicateReadLocks.get(readId);

        try {
            lock.tryLock(10000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace(); // previous thread stuck?
        }
        try {
            return readFramesInternal(request);
        } finally {
            lock.unlock();
        }
    }

    private MediaDataResponse readFramesInternal(VideoMediaDataRequest request) {
        VideoMetadata metadata = (VideoMetadata) request.getMetadata();
        int numberOfFrames = 1;
        int startFrame = request.getStart().getSeconds().multiply(new BigDecimal(metadata.getFps())).intValue();
        String filePath = request.getFile().getAbsolutePath();

        Optional<ByteBuffer> framesFromCache = findInCache(request, startFrame, filePath);

        ByteBuffer result;

        if (framesFromCache.isPresent()) {
            result = GlobalMemoryManagerAccessor.memoryManager.requestBuffer(request.getWidth() * request.getHeight() * 4);
            copyToResult(result, framesFromCache.get());
        } else if (!request.useApproximatePosition()) {
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

            List<ByteBuffer> readFrames = Arrays.asList(readFromFile(request, newStartFrame, newNumberOfFrame, filePath));

            result = readResultBetweenAtIndex(readFrames, additionalFramesToReadInBeginning);

            storeInCache(request, newStartFrame, filePath, readFrames);
        } else {
            System.out.println("Reading without cache " + request);
            List<ByteBuffer> readFrames = Arrays.asList(readFromFile(request, startFrame, 1, filePath));
            result = readResultBetweenAtIndex(readFrames, 0);
            readFrames.stream()
                    .forEach(a -> GlobalMemoryManagerAccessor.memoryManager.returnBuffer(a));
        }
        return new MediaDataResponse(result);
    }

    private ByteBuffer readResultBetweenAtIndex(List<ByteBuffer> readFrames, int additionalFramesToReadInBeginning) {
        ByteBuffer from = readFrames.get(additionalFramesToReadInBeginning);
        ByteBuffer result = GlobalMemoryManagerAccessor.memoryManager.requestBuffer(from.capacity());
        copyToResult(result, from);

        return result;
    }

    private void storeInCache(VideoMediaDataRequest request, int startFrame, String filePath, List<ByteBuffer> result) {
        String key = createHashKey(filePath, request);
        MediaHashValue value = new MediaHashValue(startFrame, startFrame + result.size(), result);
        mediaCache.cacheMedia(key, value, false);
    }

    private String createHashKey(String filePath, VideoMediaDataRequest request) {
        return filePath + " " + request.getWidth() + " " + request.getHeight();
    }

    private Optional<ByteBuffer> findInCache(VideoMediaDataRequest request, int startFrame, String filePath) {
        Optional<MediaHashValue> found = mediaCache.findInCache(createHashKey(filePath, request), startFrame);
        if (found.isPresent()) {
            MediaHashValue foundCache = found.get();
            int startCopyFrom = startFrame - foundCache.frameStart;

            ByteBuffer result = foundCache.frames.get(startCopyFrom);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Exact frame from cache {}", System.identityHashCode(result));
            }

            return Optional.ofNullable(result);
        }
        return Optional.empty();
    }

    private ByteBuffer[] readFromFile(VideoMediaDataRequest request, int startFrame, int numberOfFrames, String filePath) {
        FFmpegImageRequest ffmpegRequest = new FFmpegImageRequest();
        ffmpegRequest.numberOfFrames = numberOfFrames;
        ffmpegRequest.height = request.getHeight();
        ffmpegRequest.width = request.getWidth();
        ffmpegRequest.path = filePath;
        ffmpegRequest.useApproximatePosition = request.useApproximatePosition() ? 1 : 0;

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

    private TimelineLength frameToTimestamp(int startFrame, double fps) {
        return new TimelineLength(BigDecimal.valueOf(startFrame).divide(new BigDecimal(fps), 100, RoundingMode.HALF_DOWN));
    }

    private void copyToResult(ByteBuffer result, ByteBuffer fromBuffer) {
        for (int i = 0; i < result.capacity(); ++i) {
            result.put(i, fromBuffer.get(i));
        }

    }

}
