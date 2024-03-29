package com.helospark.tactview.core.decoder.ffmpeg;

import static com.helospark.tactview.core.util.async.RunnableExceptionLoggerDecorator.withExceptionLogging;

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
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.Striped;
import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.annotation.Qualifier;
import com.helospark.tactview.core.decoder.DecoderPreferences;
import com.helospark.tactview.core.decoder.MediaDataResponse;
import com.helospark.tactview.core.decoder.VideoMediaDataRequest;
import com.helospark.tactview.core.decoder.VideoMetadata;
import com.helospark.tactview.core.decoder.VisualMediaDecoder;
import com.helospark.tactview.core.decoder.framecache.GlobalMemoryManagerAccessor;
import com.helospark.tactview.core.decoder.framecache.MediaCache;
import com.helospark.tactview.core.decoder.framecache.MediaCache.MediaDataFrame;
import com.helospark.tactview.core.decoder.framecache.MediaCache.MediaHashValue;
import com.helospark.tactview.core.markers.ResettableBean;
import com.helospark.tactview.core.message.DropCachesMessage;
import com.helospark.tactview.core.timeline.TimelineLength;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.util.cacheable.Cacheable;
import com.helospark.tactview.core.util.memoryoperations.MemoryOperations;
import com.helospark.tactview.core.util.messaging.MessagingService;

@Component
public class FFmpegBasedMediaDecoderDecorator implements VisualMediaDecoder, ResettableBean {
    private static final BigDecimal MICROSECONDS = new BigDecimal("1000000");
    private static final Logger LOGGER = LoggerFactory.getLogger(FFmpegBasedMediaDecoderDecorator.class);
    private Striped<Lock> duplicateReadLocks = Striped.lock(100);
    private Map<PrefetchCacheKey, CompletableFuture<Void>> readFutures = new ConcurrentHashMap<>();
    private FFmpegBasedMediaDecoderImplementation implementation;
    private MediaCache mediaCache;
    private MessagingService messagingService;
    private MemoryOperations memoryOperations;
    private ThreadPoolExecutor prefetchExecutor;
    private ScheduledExecutorService scheduledExecutorService;
    private DecoderPreferences decoderPreferences;

    public FFmpegBasedMediaDecoderDecorator(FFmpegBasedMediaDecoderImplementation implementation, MediaCache mediaCache, MessagingService messagingService, MemoryOperations memoryOperations,
            @Qualifier("prefetchThreadPoolExecutorService") ThreadPoolExecutor prefetchExecutor, DecoderPreferences decoderPreferences,
            @Qualifier("generalTaskScheduledService") ScheduledExecutorService scheduledExecutorService) {
        this.implementation = implementation;
        this.mediaCache = mediaCache;
        this.messagingService = messagingService;
        this.memoryOperations = memoryOperations;
        this.prefetchExecutor = prefetchExecutor;
        this.decoderPreferences = decoderPreferences;
        this.scheduledExecutorService = scheduledExecutorService;
    }

    @PostConstruct
    public void init() {
        messagingService.register(DropCachesMessage.class, message -> {
            mediaCache.dropCaches();
            clearNativeState();
        });
        scheduledExecutorService.scheduleAtFixedRate(withExceptionLogging(() -> {
            LOGGER.trace("Clearing native memory for FFMPEG");
            implementation.runGc();
        }), 10, 10, TimeUnit.SECONDS);
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
                .withRotation(result.rotationAngle)
                .withHwDecodingSupported(result.hwDecodingSupported != 0)
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
        String computeFutureCacheKey = createHashKey(request.getFilePath(), request);

        boolean enablePrefetch = decoderPreferences.isEnableVideoPrefetch() && !request.useApproximatePosition() && !request.isAvoidPrefetch();

        if (enablePrefetch) {
            if (readFutures.size() < 5) {
                schedulePrefetchJobIfNeeded(request, computeFutureCacheKey, readStart, chunkSize);
            } else {
                LOGGER.warn("Too many prefetching is in-progress, skipping prefetch: {}", readFutures.size());
            }
        }

        Optional<ByteBuffer> framesFromCache = findInCacheAndClone(request, frameNeeded, request.getFilePath());
        if (framesFromCache.isPresent()) {
            return new MediaDataResponse(framesFromCache.get());
        }

        if (enablePrefetch) {
            Optional<CompletableFuture<Void>> futureCache = findInFuture(computeFutureCacheKey, request.getStart().getSeconds());
            if (futureCache.isPresent()) {
                try {
                    LOGGER.debug("Found future at {}", request.getStart().getSeconds());
                    futureCache.get().get(1000, TimeUnit.MILLISECONDS);
                } catch (Exception e) {
                    LOGGER.error("Unable to prefetch frames for {}", request, e);
                }
            }
        }
        // Possible loaded above from futures
        framesFromCache = findInCacheAndClone(request, frameNeeded, request.getFilePath());
        if (framesFromCache.isPresent()) {
            return new MediaDataResponse(framesFromCache.get());
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

    private void clearNativeState() {
        // TODO: should be synchronized
        implementation.clearState();
    }

    private void schedulePrefetchJobIfNeeded(VideoMediaDataRequest request, String cacheKey, BigDecimal readStart, BigDecimal chunkSize) {
        BigDecimal nextChunkPosition = readStart.add(chunkSize);
        Optional<MediaDataFrame> found = mediaCache.findInCache(createHashKey(request.getFilePath(), request), nextChunkPosition);
        Optional<CompletableFuture<Void>> readFuture = findInFuture(cacheKey, nextChunkPosition);
        if (found.isEmpty() && readFuture.isEmpty()) {
            PrefetchCacheKey prefetchKey = new PrefetchCacheKey();
            prefetchKey.fileCache = cacheKey;
            prefetchKey.startTime = nextChunkPosition;
            prefetchKey.endTime = nextChunkPosition.add(chunkSize);
            LOGGER.debug("Prefetching video at {} due to read at {}", nextChunkPosition, request.getStart().getSeconds());

            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    Optional<MediaDataFrame> found2 = mediaCache.findInCache(createHashKey(request.getFilePath(), request), nextChunkPosition);
                    Optional<CompletableFuture<Void>> readFuture2 = findInFuture(cacheKey, request.getStart().getSeconds());
                    if (found2.isEmpty() && readFuture2.isEmpty()) {
                        LOGGER.debug("Started working for {}", prefetchKey);
                        VideoMediaDataRequest newRequest = VideoMediaDataRequest.builderFrom(request)
                                .withAvoidPrefetch(true)
                                .withStart(new TimelinePosition(nextChunkPosition))
                                .build();

                        MediaDataResponse result = readFrames(newRequest);

                        GlobalMemoryManagerAccessor.memoryManager.returnBuffers(result.getFrames()); // TODO: it could be optimized to avoid cloning
                    }
                } catch (Exception e) {
                    LOGGER.error("Unable to prefetch frames at {}", prefetchKey, e);
                }
                LOGGER.debug("Finished working for {}, removing key", prefetchKey);
                readFutures.remove(prefetchKey);
            }, prefetchExecutor);
            readFutures.put(prefetchKey, future);
        }
    }

    private Optional<CompletableFuture<Void>> findInFuture(String cacheKey, BigDecimal seconds) {
        return readFutures.entrySet()
                .stream()
                .filter(entry -> entry.getKey().fileCache.equals(cacheKey))
                .filter(entry -> seconds.compareTo(entry.getKey().startTime) >= 0 && seconds.compareTo(entry.getKey().endTime) <= 0)
                .map(entry -> entry.getValue())
                .findFirst();
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

    private MediaDataResponse readFramesInternal(VideoMediaDataRequest request, BigDecimal startTime, BigDecimal endTime, BigDecimal frameNeeded) {
        VideoMetadata metadata = (VideoMetadata) request.getMetadata();
        String filePath = request.getFile().getAbsolutePath();

        Optional<ByteBuffer> framesFromCache = findInCacheAndClone(request, request.getStart().getSeconds(), filePath);

        ByteBuffer result;

        if (framesFromCache.isPresent()) {
            result = framesFromCache.get();
        } else if (!request.useApproximatePosition()) {
            LOGGER.debug("Reading " + startTime + " " + endTime);

            MediaHashValue readFrames = readFromFile(request, startTime, endTime, filePath);

            if (readFrames.frames.size() == 0) {
                LOGGER.warn("There were no frames read for {}, adding transparent frame", startTime);
                readFrames.frames.add(new MediaDataFrame(ClipImage.fromSize(request.getWidth(), request.getHeight()).getBuffer(), startTime));
            }

            result = copyResultAtIndex(readFrames, frameNeeded);

            storeInCache(request, filePath, readFrames);
        } else {
            LOGGER.debug("Reading without cache {}", request);
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

    private Optional<ByteBuffer> findInCacheAndClone(VideoMediaDataRequest request, BigDecimal time, String filePath) {
        Optional<MediaDataFrame> found = mediaCache.findInCacheAndClone(createHashKey(filePath, request), time);
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

        VideoMetadata videoMetadata = (VideoMetadata) request.getMetadata();
        BigDecimal fps = new BigDecimal(videoMetadata.getFps());
        int numberOfFrames = endTime.subtract(startTime).multiply(fps).intValue() + 5;

        ffmpegRequest.numberOfFrames = numberOfFrames;
        ffmpegRequest.height = request.getHeight();
        ffmpegRequest.width = request.getWidth();
        ffmpegRequest.path = filePath;
        ffmpegRequest.useApproximatePosition = request.useApproximatePosition() ? 1 : 0;

        ffmpegRequest.startMicroseconds = startTime.multiply(MICROSECONDS).longValue();
        ffmpegRequest.endTimeInMs = endTime.multiply(MICROSECONDS).longValue();

        ffmpegRequest.useHardwareDecoding = videoMetadata.isHwDecodingSupported() && decoderPreferences.isEnableHardwareAcceleration() ? 1 : 0;

        ByteBuffer[] buffers = new ByteBuffer[numberOfFrames];
        ffmpegRequest.frames = new FFMpegFrame();
        LOGGER.debug("Requesting '{}' number of frames {} from {} to {}", filePath, numberOfFrames, startTime, endTime);
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

        return new MediaHashValue(outFrames, endTimeSecond, startTime);
    }

    private BigDecimal microsecondsToSeconds(long time) {
        return BigDecimal.valueOf(time).divide(MICROSECONDS, 5, RoundingMode.HALF_UP);
    }

    private void copyToResult(ByteBuffer result, ByteBuffer fromBuffer) {
        memoryOperations.copyBuffer(fromBuffer, result, fromBuffer.capacity());
    }

    static class PrefetchCacheKey {
        String fileCache;
        BigDecimal startTime;
        BigDecimal endTime;

        @Override
        public String toString() {
            return "PrefetchCacheKey [fileCache=" + fileCache + ", startTime=" + startTime + ", endTime=" + endTime + "]";
        }

        @Override
        public int hashCode() {
            return Objects.hash(endTime, fileCache, startTime);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            PrefetchCacheKey other = (PrefetchCacheKey) obj;
            return Objects.equals(endTime, other.endTime) && Objects.equals(fileCache, other.fileCache) && Objects.equals(startTime, other.startTime);
        }

    }

    @Override
    public void resetDefaults() {
        implementation.clearState();
    }

}
