package com.helospark.tactview.core.decoder.ffmpeg;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

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
import com.helospark.tactview.core.decoder.framecache.MediaCache.MediaDataFrame;
import com.helospark.tactview.core.decoder.framecache.MediaCache.MediaHashValue;
import com.helospark.tactview.core.message.DropCachesMessage;
import com.helospark.tactview.core.timeline.TimelineLength;
import com.helospark.tactview.core.util.cacheable.Cacheable;
import com.helospark.tactview.core.util.messaging.MessagingService;

@Component
public class FFmpegBasedMediaDecoderDecorator implements VisualMediaDecoder {
    private static final BigDecimal MICROSECONDS = new BigDecimal("1000000");
    private static final Logger LOGGER = LoggerFactory.getLogger(FFmpegBasedMediaDecoderDecorator.class);
    private Striped<Lock> duplicateReadLocks = Striped.lock(100);
    private FFmpegBasedMediaDecoderImplementation implementation;
    private MediaCache mediaCache;
    private MessagingService messagingService;

    public FFmpegBasedMediaDecoderDecorator(FFmpegBasedMediaDecoderImplementation implementation, MediaCache mediaCache, MessagingService messagingService) {
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
        BigDecimal chunkSize = calculateChunkSize(request);
        BigDecimal frameNeeded = request.getStart().getSeconds();
        BigDecimal readStart = request.getStart().getSeconds().divideToIntegralValue(chunkSize).multiply(chunkSize);
        BigDecimal readEnd = readStart.add(chunkSize);

        Optional<ByteBuffer> framesFromCache = findInCache(request, frameNeeded, request.getFilePath());
        ByteBuffer result;
        if (framesFromCache.isPresent()) {
            result = GlobalMemoryManagerAccessor.memoryManager.requestBuffer(request.getWidth() * request.getHeight() * 4);
            copyToResult(result, framesFromCache.get());
            return new MediaDataResponse(result);
        }

        // end
        String readId = request.getFile().getAbsolutePath();
        readId += request.getWidth() + "x" + request.getHeight();

        Lock lock = duplicateReadLocks.get(readId);

        try {
            lock.tryLock(10000000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace(); // previous thread stuck?
        }
        try {
            return readFramesInternal(request, readStart, readEnd, frameNeeded);
        } finally {
            lock.unlock();
        }
    }

    // simple logic for now to determine how many frames to cache
    // ideally this should also use the available memory in the system (more memory->more cache) & user settings as well
    // This should not return different number based on the time index!
    private BigDecimal calculateChunkSize(VideoMediaDataRequest request) {
        int numberOfBytesPerSecond = (int) (request.getWidth() * request.getHeight() * 4 * ((VideoMetadata) request.getMetadata()).getFps());

        if (numberOfBytesPerSecond < 30_000_000) {
            return new BigDecimal("1.0"); // ~30Mb
        } else if (numberOfBytesPerSecond < 150_000_000) {
            return new BigDecimal("0.4"); // ~57Mb
        } else if (numberOfBytesPerSecond < 600_000_000) {
            return new BigDecimal("0.2"); // <100Mb
        } else {
            return new BigDecimal("0.1");
        }
    }

    private MediaDataResponse readFramesInternal(VideoMediaDataRequest request, BigDecimal startFrame, BigDecimal endFrame, BigDecimal frameNeeded) {
        VideoMetadata metadata = (VideoMetadata) request.getMetadata();
        String filePath = request.getFile().getAbsolutePath();

        Optional<ByteBuffer> framesFromCache = findInCache(request, request.getStart().getSeconds(), filePath);

        ByteBuffer result;

        if (framesFromCache.isPresent()) {
            result = GlobalMemoryManagerAccessor.memoryManager.requestBuffer(request.getWidth() * request.getHeight() * 4);
            copyToResult(result, framesFromCache.get());
        } else if (!request.useApproximatePosition()) {
            LOGGER.debug("Reading " + startFrame + " " + endFrame);

            MediaHashValue readFrames = readFromFile(request, startFrame, endFrame, filePath);
            result = copyResultAtIndex(readFrames, frameNeeded);

            storeInCache(request, filePath, readFrames);
        } else {
            System.out.println("Reading without cache " + request);
            MediaHashValue readFrames = readFromFile(request, frameNeeded, frameNeeded.add(BigDecimal.valueOf(1.0 / metadata.getFps())), filePath);
            result = copyResultAtIndex(readFrames, frameNeeded);
            readFrames.frames
                    .stream()
                    .forEach(a -> GlobalMemoryManagerAccessor.memoryManager.returnBuffer(a.frame));
        }
        return new MediaDataResponse(result);
    }

    private ByteBuffer copyResultAtIndex(MediaHashValue readFrames, BigDecimal time) {
        MediaDataFrame from = readFrames.getFrameAt(time);
        ByteBuffer result = GlobalMemoryManagerAccessor.memoryManager.requestBuffer(from.frame.capacity());
        copyToResult(result, from.frame);

        return result;
    }

    private void storeInCache(VideoMediaDataRequest request, String filePath, MediaHashValue readFrames) {
        String key = createHashKey(filePath, request);
        mediaCache.cacheMedia(key, readFrames, false);
    }

    private String createHashKey(String filePath, VideoMediaDataRequest request) {
        return filePath + " " + request.getWidth() + " " + request.getHeight();
    }

    private Optional<ByteBuffer> findInCache(VideoMediaDataRequest request, BigDecimal time, String filePath) {
        Optional<MediaDataFrame> found = mediaCache.findInCache(createHashKey(filePath, request), time);
        if (found.isPresent()) {
            ByteBuffer result = found.get().frame;

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Exact frame from cache {}", System.identityHashCode(result));
            }

            return Optional.ofNullable(result);
        }
        return Optional.empty();
    }

    private MediaHashValue readFromFile(VideoMediaDataRequest request, BigDecimal startTime, BigDecimal endTime, String filePath) {
        FFmpegImageRequest ffmpegRequest = new FFmpegImageRequest();

        BigDecimal fps = new BigDecimal(((VideoMetadata) request.getMetadata()).getFps());
        int numberOfFrames = endTime.subtract(startTime).multiply(fps).intValue() + 5;

        ffmpegRequest.numberOfFrames = numberOfFrames;
        ffmpegRequest.height = request.getHeight();
        ffmpegRequest.width = request.getWidth();
        ffmpegRequest.path = filePath;
        ffmpegRequest.useApproximatePosition = request.useApproximatePosition() ? 1 : 0;

        ffmpegRequest.startMicroseconds = startTime.multiply(MICROSECONDS).longValue();
        ffmpegRequest.endTimeInMs = endTime.multiply(MICROSECONDS).longValue();

        ByteBuffer[] buffers = new ByteBuffer[numberOfFrames];
        ffmpegRequest.frames = new FFMpegFrame();
        System.out.println("Requesting frames " + numberOfFrames);
        FFMpegFrame[] array = (FFMpegFrame[]) ffmpegRequest.frames.toArray(numberOfFrames);
        for (int i = 0; i < numberOfFrames; ++i) {
            array[i].data = GlobalMemoryManagerAccessor.memoryManager.requestBuffer(ffmpegRequest.width * ffmpegRequest.height * 4);
            buffers[i] = array[i].data;
        }

        implementation.readFrames(ffmpegRequest);

        for (int i = ffmpegRequest.actualNumberOfFramesRead; i < buffers.length; ++i) {
            GlobalMemoryManagerAccessor.memoryManager.returnBuffer(buffers[i]);
        }
        List<MediaDataFrame> outFrames = new ArrayList<>();
        for (int i = 0; i < ffmpegRequest.actualNumberOfFramesRead; ++i) {
            outFrames.add(new MediaDataFrame(array[i].data, microsecondsToSeconds(array[i].startTimeInMs)));
        }
        BigDecimal endTimeSecond = microsecondsToSeconds(ffmpegRequest.endTimeInMs);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Read frames for '{}' at {} with endTime {}", filePath, outFrames.stream().map(a -> a.startTime.toString()).collect(Collectors.toList()), endTimeSecond);
        }

        return new MediaHashValue(outFrames, endTimeSecond);
    }

    private BigDecimal microsecondsToSeconds(long time) {
        return BigDecimal.valueOf(time).divide(MICROSECONDS, 5, RoundingMode.HALF_UP);
    }

    private void copyToResult(ByteBuffer result, ByteBuffer fromBuffer) {
        for (int i = 0; i < result.capacity(); ++i) {
            result.put(i, fromBuffer.get(i));
        }

    }

}
