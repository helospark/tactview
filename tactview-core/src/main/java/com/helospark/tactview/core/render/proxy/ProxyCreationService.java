package com.helospark.tactview.core.render.proxy;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;

import com.helospark.lightdi.annotation.Qualifier;
import com.helospark.lightdi.annotation.Service;
import com.helospark.tactview.core.decoder.VideoMetadata;
import com.helospark.tactview.core.decoder.framecache.GlobalMemoryManagerAccessor;
import com.helospark.tactview.core.render.proxy.compression.CompressedImageWriter;
import com.helospark.tactview.core.render.proxy.ffmpeg.ContinuousImageQueryFFmpegService;
import com.helospark.tactview.core.render.proxy.ffmpeg.FFmpegFrameWithFrameNumber;
import com.helospark.tactview.core.render.proxy.ffmpeg.InitializeReadJobRequest;
import com.helospark.tactview.core.render.proxy.ffmpeg.QueryFramesRequest;
import com.helospark.tactview.core.timeline.VideoClip;
import com.helospark.tactview.core.timeline.VisualMediaSource;
import com.helospark.tactview.core.timeline.clipfactory.ImageSequenceClipFactory;
import com.helospark.tactview.core.timeline.clipfactory.sequence.FileNamePatternToFileResolverService;
import com.helospark.tactview.core.timeline.message.progress.ProgressAdvancedMessage;
import com.helospark.tactview.core.timeline.message.progress.ProgressDoneMessage;
import com.helospark.tactview.core.timeline.message.progress.ProgressInitializeMessage;
import com.helospark.tactview.core.timeline.message.progress.ProgressType;
import com.helospark.tactview.core.util.messaging.MessagingService;

@Service
public class ProxyCreationService {
    private static final int WRITE_FRAME_BUFFER_SIZE = 10;
    private static final int NUMBER_OF_FRAMES_TO_READ_FROM_FILE_PER_CALL = 5;
    private static final int NUMBER_OF_PRODUCER_THREADS = Math.max(Runtime.getRuntime().availableProcessors() - 2, 1);

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    private ContinuousImageQueryFFmpegService imageGetterService;
    private CompressedImageWriter compressedImageWriter;
    private MessagingService messagingService;
    private ProxyMetadataHandler proxyMetadataHandler;

    private volatile boolean finished = false;
    private LinkedBlockingQueue<FFmpegFrameWithFrameNumber> queue = new LinkedBlockingQueue<>(WRITE_FRAME_BUFFER_SIZE);

    public ProxyCreationService(ContinuousImageQueryFFmpegService imageGetterService,
            ImageSequenceClipFactory imageSequenceClipFactory,
            MessagingService messagingService,
            ProxyMetadataHandler proxyMetadataHandler,
            @Qualifier("jpgCompressedImageWriter") CompressedImageWriter lz4CompressedImageWriter) {
        this.imageGetterService = imageGetterService;
        this.compressedImageWriter = lz4CompressedImageWriter;
        this.messagingService = messagingService;
        this.proxyMetadataHandler = proxyMetadataHandler;

    }

    public void createProxy(VideoClip clip, int width, int height) {
        executorService.submit(() -> {
            try {
                createProxyInternal(clip, width, height);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void createProxyInternal(VideoClip clip, int width, int height) throws IOException, InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(NUMBER_OF_PRODUCER_THREADS);

        File inputFile = new File((clip.getBackingSource()).backingFile);

        File proxyImageFolder = new File(System.getProperty("java.io.tmpdir") + File.separator + inputFile.getName() + "_" + inputFile.length() + File.separator + width + "_" + height);
        proxyImageFolder.mkdirs();

        InitializeReadJobRequest initRequest = new InitializeReadJobRequest();
        initRequest.path = inputFile.getAbsolutePath();
        initRequest.width = width;
        initRequest.height = height;

        BigDecimal fps = new BigDecimal(((VideoMetadata) clip.getMediaMetadata()).getFps());
        int approximageNumberOfJobs = clip.getMediaMetadata().getLength().getSeconds().multiply(fps).intValue();
        String progressJobId = UUID.randomUUID().toString();
        messagingService.sendAsyncMessage(new ProgressInitializeMessage(progressJobId, approximageNumberOfJobs, ProgressType.LOW_RES_PROXY_CREATE));

        if (!proxyImageFolder.exists() || Math.abs(proxyImageFolder.list((d, name) -> name.startsWith("image_")).length - approximageNumberOfJobs) > 10) {
            FileUtils.cleanDirectory(proxyImageFolder);
            proxyMetadataHandler.writeMetadata(proxyImageFolder, width, height);

            int jobId = imageGetterService.openFile(initRequest);

            if (jobId < 0) {
                throw new RuntimeException("Unable to initialize job");
            }

            finished = false;

            Thread producerThread = startProducer(initRequest, jobId, progressJobId);

            for (int i = 0; i < NUMBER_OF_PRODUCER_THREADS; ++i) {
                executorService.execute(() -> renderFrames(proxyImageFolder, initRequest));
            }

            producerThread.join();
            executorService.awaitTermination(10000, TimeUnit.MILLISECONDS);

            imageGetterService.freeJob(jobId);
        }

        String imageSequencePath = proxyImageFolder.getPath() + FileNamePatternToFileResolverService.PATH_FILENAME_SEPARATOR + compressedImageWriter.getImageNamePattern();

        VideoMetadata metadata = VideoMetadata.builder()
                .withWidth(width)
                .withHeight(height)
                .withFps(fps.doubleValue())
                .withLength(clip.getMediaMetadata().getLength())
                .build();

        VisualMediaSource videoSource = new VisualMediaSource(imageSequencePath, compressedImageWriter.getImageSequenceDecoderDecorator());

        VideoClip.LowResolutionProxyData proxyData = new VideoClip.LowResolutionProxyData(videoSource, metadata);
        clip.setLowResolutionProxy(proxyData);

        messagingService.sendAsyncMessage(new ProgressDoneMessage(progressJobId));

    }

    private Thread startProducer(InitializeReadJobRequest initRequest, int jobId, String progressJobId) {
        Thread thread = new Thread(() -> {
            int readFrames = 0;
            do {
                QueryFramesRequest queryFramesRequest = new QueryFramesRequest();
                queryFramesRequest.numberOfFrames = NUMBER_OF_FRAMES_TO_READ_FROM_FILE_PER_CALL;
                queryFramesRequest.jobId = jobId;
                queryFramesRequest.frames = new FFmpegFrameWithFrameNumber();

                FFmpegFrameWithFrameNumber[] frames = (FFmpegFrameWithFrameNumber[]) queryFramesRequest.frames.toArray(NUMBER_OF_FRAMES_TO_READ_FROM_FILE_PER_CALL);

                for (int i = 0; i < queryFramesRequest.numberOfFrames; ++i) {
                    frames[i].data = GlobalMemoryManagerAccessor.memoryManager.requestBuffer(initRequest.width * initRequest.height * 4);
                }

                readFrames = imageGetterService.readFrames(queryFramesRequest);

                for (int i = 0; i < readFrames; ++i) {
                    try {
                        queue.offer(frames[i], 10000, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
                for (int i = readFrames; i < NUMBER_OF_FRAMES_TO_READ_FROM_FILE_PER_CALL; ++i) {
                    GlobalMemoryManagerAccessor.memoryManager.returnBuffer(frames[i].data);
                }

                messagingService.sendAsyncMessage(new ProgressAdvancedMessage(progressJobId, readFrames));

            } while (readFrames > 0);

            finished = true;

        });
        thread.start();
        return thread;
    }

    private void renderFrames(File proxyFolder, InitializeReadJobRequest initRequest) {
        while (!finished || queue.size() > 0) {
            FFmpegFrameWithFrameNumber frame = null;
            try {
                frame = queue.poll(100, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }

            if (frame != null) {
                if (frame.frame == -1) {
                    finished = true;
                    continue;
                }

                compressedImageWriter.writeCompressedFrame(frame, proxyFolder, initRequest.width, initRequest.height);
            }
        }
    }

}
