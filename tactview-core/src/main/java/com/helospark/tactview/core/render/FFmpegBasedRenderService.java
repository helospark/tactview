package com.helospark.tactview.core.render;

import static com.helospark.tactview.core.render.helper.ExtensionType.AUDIO;
import static com.helospark.tactview.core.render.helper.ExtensionType.VIDEO;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.decoder.framecache.GlobalMemoryManagerAccessor;
import com.helospark.tactview.core.decoder.opencv.ImageMediaLoader;
import com.helospark.tactview.core.decoder.opencv.ImageMetadataRequest;
import com.helospark.tactview.core.decoder.opencv.ImageMetadataResponse;
import com.helospark.tactview.core.decoder.opencv.ImageRequest;
import com.helospark.tactview.core.optionprovider.OptionProvider;
import com.helospark.tactview.core.optionprovider.OptionProvider.OptionExample;
import com.helospark.tactview.core.render.domain.FFmpegRenderThreadResult;
import com.helospark.tactview.core.render.ffmpeg.ChapterInformation;
import com.helospark.tactview.core.render.ffmpeg.CodecExtraDataRequest;
import com.helospark.tactview.core.render.ffmpeg.CodecInformation;
import com.helospark.tactview.core.render.ffmpeg.FFmpegBasedMediaEncoder;
import com.helospark.tactview.core.render.ffmpeg.FFmpegClearEncoderRequest;
import com.helospark.tactview.core.render.ffmpeg.FFmpegEncodeFrameRequest;
import com.helospark.tactview.core.render.ffmpeg.FFmpegInitEncoderRequest;
import com.helospark.tactview.core.render.ffmpeg.NativeMap;
import com.helospark.tactview.core.render.ffmpeg.NativePair;
import com.helospark.tactview.core.render.ffmpeg.QueryCodecRequest;
import com.helospark.tactview.core.render.ffmpeg.RenderFFMpegFrame;
import com.helospark.tactview.core.render.helper.ExtensionType;
import com.helospark.tactview.core.render.helper.HandledExtensionValueElement;
import com.helospark.tactview.core.render.helper.IntervalThreadingPartitioner;
import com.helospark.tactview.core.render.helper.ThreadingAccessorResult;
import com.helospark.tactview.core.repository.ProjectRepository;
import com.helospark.tactview.core.timeline.AudioFrameResult;
import com.helospark.tactview.core.timeline.AudioVideoFragment;
import com.helospark.tactview.core.timeline.MergeOnIntersectingIntervalList;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelineLength;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.timeline.TimelineManagerRenderService;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ValueListElement;
import com.helospark.tactview.core.timeline.effect.scale.service.ScaleService;
import com.helospark.tactview.core.timeline.message.progress.ProgressAdvancedMessage;
import com.helospark.tactview.core.util.messaging.MessagingService;

@Component
public class FFmpegBasedRenderService extends AbstractRenderService {
    private static final BigDecimal SECONDS_TO_MICROSECONDS = BigDecimal.valueOf(1000000);
    private static final Logger LOGGER = LoggerFactory.getLogger(FFmpegBasedRenderService.class);
    private static final Set<String> COMMON_AUDIO_CONTAINERS = Set.of("mp3", "wav", "oga");
    private static final String DEFAULT_VALUE = "default";
    private static final String NONE_VALUE = "none";
    private static final int MAX_NUMBER_OF_CODECS = 400;
    private final FFmpegBasedMediaEncoder ffmpegBasedMediaEncoder;
    private final TimelineManagerAccessor timelineManagerAccessor;
    private final IntervalThreadingPartitioner intervalThreadingPartitioner;
    private ImageMediaLoader coverPhotoLoader;

    public FFmpegBasedRenderService(TimelineManagerRenderService timelineManager, FFmpegBasedMediaEncoder ffmpegBasedMediaEncoder, MessagingService messagingService,
            ScaleService scaleService, TimelineManagerAccessor timelineManagerAccessor, ProjectRepository projectRepository, IntervalThreadingPartitioner intervalThreadingPartitioner,
            ImageMediaLoader coverPhotoLoader) {
        super(timelineManager, messagingService, scaleService, projectRepository);
        this.ffmpegBasedMediaEncoder = ffmpegBasedMediaEncoder;
        this.timelineManagerAccessor = timelineManagerAccessor;
        this.intervalThreadingPartitioner = intervalThreadingPartitioner;
        this.coverPhotoLoader = coverPhotoLoader;
    }

    @Override
    public void renderInternal(RenderRequest renderRequest) {

        int videoBitRate = (int) renderRequest.getOptions().get("videobitrate").getValue();

        LOGGER.info("Video BitRate: {}", videoBitRate);

        int audioBitRate = (int) renderRequest.getOptions().get("audiobitrate").getValue();
        LOGGER.info("Audio BitRate: {}", audioBitRate);

        int audioSampleRate = (int) renderRequest.getOptions().get("audiosamplerate").getValue();
        LOGGER.info("Audio SampleRate: {}", audioSampleRate);

        int bytesPerSample = Integer.parseInt(renderRequest.getOptions().get("audiobytespersample").getValue().toString());
        LOGGER.info("Audio bytes per sample: {}", bytesPerSample);

        String audioCodec = (String) renderRequest.getOptions().get("audiocodec").getValue();
        LOGGER.info("AudioCodec: {}", audioCodec);

        String videoCodec = (String) renderRequest.getOptions().get("videocodec").getValue();
        if (isAudioContainerForFileName(renderRequest.getFileName(), renderRequest.getSelectedExtensionType())) {
            videoCodec = NONE_VALUE;
        }
        LOGGER.info("VideoCodec: {}", videoCodec);

        String videoPixelFormat = (String) renderRequest.getOptions().get("videoPixelFormat").getValue();
        LOGGER.info("videoPixelFormat: {}", videoPixelFormat);

        int numberOfChannels = Integer.parseInt(renderRequest.getOptions().get("audionumberofchannels").getValue().toString());
        LOGGER.info("numberOfChannels: {}", numberOfChannels);

        String videoPresetOrNull = (String) Optional.ofNullable(renderRequest.getOptions().get("preset")).map(a -> a.getValue()).orElse(null);
        LOGGER.info("video preset: {}", videoPresetOrNull);

        int threads = Integer.parseInt(renderRequest.getOptions().get("threads").getValue().toString());
        LOGGER.info("threads: {}", threads);

        Map<TimelinePosition, String> chapters = renderRequest.getChapters();
        LOGGER.info("chapters: {}", chapters);

        FFmpegInitEncoderRequest initNativeRequest = new FFmpegInitEncoderRequest();
        initNativeRequest.fileName = renderRequest.getFileName();
        initNativeRequest.fps = renderRequest.getFps().doubleValue();
        int width = renderRequest.getWidth();
        if (width % 2 == 1) {
            width++;
        }
        int height = renderRequest.getHeight();
        if (height % 2 == 1) {
            height++;
        }

        ByteBuffer coverPhoto = null;
        File coverPhotoFile = (File) renderRequest.getOptions().get("coverPhoto").getValue();
        String initVideoCodec = videoCodec;
        if (!coverPhotoFile.getName().equals("")) {
            ImageMetadataResponse metadata = loadMetadata(coverPhotoFile);
            coverPhoto = loadCoverPhotoImage(coverPhotoFile, metadata);

            width = metadata.width;
            height = metadata.height;
            initVideoCodec = DEFAULT_VALUE;
        }

        initNativeRequest.renderWidth = width;
        initNativeRequest.renderHeight = height;

        initNativeRequest.actualWidth = width;
        initNativeRequest.actualHeight = height;
        initNativeRequest.bytesPerSample = bytesPerSample;
        initNativeRequest.audioChannels = numberOfChannels;
        initNativeRequest.sampleRate = audioSampleRate;

        initNativeRequest.audioBitRate = audioBitRate;
        initNativeRequest.videoBitRate = videoBitRate;
        initNativeRequest.audioSampleRate = audioSampleRate;
        initNativeRequest.videoCodec = initVideoCodec;
        initNativeRequest.audioCodec = audioCodec;
        initNativeRequest.videoPixelFormat = videoPixelFormat;
        initNativeRequest.videoPreset = videoPresetOrNull;
        initNativeRequest.metadata = convertToNativeMap(renderRequest.getMetadata());

        initNativeRequest.totalLengthInMicroseconds = renderRequest.getEndPosition().multiply(SECONDS_TO_MICROSECONDS).getSeconds().longValue();
        initNativeRequest.numberOfChapters = renderRequest.getChapters().size();
        if (renderRequest.getChapters().size() > 0) {
            initNativeRequest.chapters = new ChapterInformation();
            ChapterInformation[] chaptersArray = (ChapterInformation[]) initNativeRequest.chapters.toArray(chapters.size());
            fillChapters(chaptersArray, chapters);
        }
        // frame not freed

        int encoderIndex = ffmpegBasedMediaEncoder.initEncoder(initNativeRequest);

        if (encoderIndex < 0) {
            throw new RuntimeException("Unable to render, statuscode is " + encoderIndex + " , check logs");
        }

        int frameIndex = 0;

        boolean needsVideo = !videoCodec.equals(NONE_VALUE) && coverPhoto == null;
        boolean needsAudio = !audioCodec.equals(NONE_VALUE);

        // Producer consumer pattern below

        if (threads < 1) {
            threads = 1;
        }

        BlockingQueue<CompletableFuture<FFmpegRenderThreadResult>> queue = new ArrayBlockingQueue<>(threads);
        ExecutorService executorService = Executors.newFixedThreadPool(threads);

        // This hack is needed because the number of samples during a frame might not be an integer.
        // For example for a 29.97 FPS video & 44100 samples/sec, number of samples per frame is 1471.4715, however number of samples can only be an integer.
        // therefore only 1471 samples are sent to the audio file. This means that 0.53 samples will be missing for every frame.
        // this will lead to audio misalignment in a 1h video: 29.97⋅60⋅60⋅0.53/44100 = 1.29s
        // Solution is to ask a little more samples, keep tract of how many samples we sent and if there is a misalignment copy more/less samples from result.
        // in the previous example 1 additional sample will be used per ~2 frames.
        TimelineLength fewExtraAudioSamples = new TimelineLength(BigDecimal.TEN.divide(BigDecimal.valueOf(projectRepository.getSampleRate()), 5, RoundingMode.HALF_UP));

        try {
            var producerThread = new Thread() {
                public volatile boolean isFinished = false;

                @Override
                public void run() {
                    ThreadingAccessorResult partitionResult = intervalThreadingPartitioner
                            .partitionBasedOnRenderThreadability(new TimelineInterval(renderRequest.getStartPosition(), renderRequest.getEndPosition()));
                    MergeOnIntersectingIntervalList partitionList = partitionResult.getSingleThreadedIntervals();

                    LOGGER.debug("Partitioned single threaded intervals " + partitionList);

                    partitionResult.getSingleTheadRenderables()
                            .stream()
                            .forEach(a -> a.onStartRender());

                    TimelinePosition currentPosition = renderRequest.getStartPosition();
                    try {
                        int partitionIndex = 0;
                        while (currentPosition.isLessThan(renderRequest.getEndPosition()) && !renderRequest.getIsCancelledSupplier().get()) {
                            TimelineInterval nextIntersection = partitionList.size() > partitionIndex ? partitionList.get(partitionIndex) : TimelineInterval.ofPoint(renderRequest.getEndPosition());

                            // TODO: parallel and single threaded part is almost entirely a copy-pase of each other, fix it
                            // parallel render
                            while (currentPosition.isLessThan(renderRequest.getEndPosition())
                                    && currentPosition.isLessThan(nextIntersection.getStartPosition())
                                    && !renderRequest.getIsCancelledSupplier().get()) {

                                TimelinePosition position = currentPosition; // thanks Java...
                                CompletableFuture<FFmpegRenderThreadResult> job = CompletableFuture
                                        .supplyAsync(() -> {
                                            RenderRequestFrameRequest superRequest = RenderRequestFrameRequest.builder()
                                                    .withBytesPerSample(Optional.of(bytesPerSample))
                                                    .withCurrentPosition(position)
                                                    .withNeedsSound(needsAudio)
                                                    .withNeedsVideo(needsVideo)
                                                    .withNumberOfChannels(Optional.of(numberOfChannels))
                                                    .withRenderRequest(renderRequest)
                                                    .withSampleRate(Optional.of(audioSampleRate))
                                                    .withExpectedHeight(initNativeRequest.renderHeight)
                                                    .withExpectedWidth(initNativeRequest.renderWidth)
                                                    .withAudioLength(new TimelineLength(projectRepository.getFrameTime()).add(fewExtraAudioSamples))
                                                    .build();
                                            AudioVideoFragment audioVideo = queryFrameAt(superRequest);
                                            return new FFmpegRenderThreadResult(audioVideo, position);
                                        }, executorService);
                                queue.put(job);
                                currentPosition = currentPosition.add(renderRequest.getStep());
                            }

                            // single threaded render interval
                            while (currentPosition.isLessThan(renderRequest.getEndPosition())
                                    && currentPosition.isLessThan(nextIntersection.getEndPosition())
                                    && !renderRequest.getIsCancelledSupplier().get()) {

                                RenderRequestFrameRequest superRequest = RenderRequestFrameRequest.builder()
                                        .withBytesPerSample(Optional.of(bytesPerSample))
                                        .withCurrentPosition(currentPosition)
                                        .withNeedsSound(needsAudio)
                                        .withNeedsVideo(needsVideo)
                                        .withNumberOfChannels(Optional.of(numberOfChannels))
                                        .withRenderRequest(renderRequest)
                                        .withSampleRate(Optional.of(audioSampleRate))
                                        .withExpectedHeight(initNativeRequest.renderHeight)
                                        .withExpectedWidth(initNativeRequest.renderWidth)
                                        .withAudioLength(new TimelineLength(projectRepository.getFrameTime()).add(fewExtraAudioSamples))
                                        .build();
                                FFmpegRenderThreadResult frame = new FFmpegRenderThreadResult(queryFrameAt(superRequest), currentPosition);
                                queue.put(CompletableFuture.completedFuture(frame));
                                currentPosition = currentPosition.add(renderRequest.getStep());
                            }

                            ++partitionIndex;
                        }
                    } catch (Exception e) {
                        isFinished = true;
                        throw new RuntimeException(e);
                    }

                    try {
                        partitionResult.getSingleTheadRenderables()
                                .stream()
                                .forEach(a -> a.onEndRender());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    isFinished = true;
                }
            };
            producerThread.start();

            boolean hasAlreadyEncodedCoverImage = false;

            long allAudioSamples = 0;
            // Encoding must be done in single thread
            while (!producerThread.isFinished || queue.peek() != null) {
                CompletableFuture<FFmpegRenderThreadResult> future = pollQueue(queue);
                if (future != null) {
                    FFmpegRenderThreadResult threadResult = future.join();
                    AudioVideoFragment frame = threadResult.audioVideo;
                    FFmpegEncodeFrameRequest nativeRequest = new FFmpegEncodeFrameRequest();
                    nativeRequest.frame = new RenderFFMpegFrame();
                    RenderFFMpegFrame[] array = (RenderFFMpegFrame[]) nativeRequest.frame.toArray(1);
                    if (frame.getVideoResult() != null) {
                        array[0].imageData = frame.getVideoResult().getBuffer();
                    } else if (coverPhoto != null && !hasAlreadyEncodedCoverImage) {
                        array[0].imageData = coverPhoto;
                        hasAlreadyEncodedCoverImage = true;
                    }
                    boolean audioConverted = false;
                    if (needsAudio && frame.getAudioResult().getChannels().size() > 0) {
                        int expectedNumberOfSamples = threadResult.time.getSeconds()
                                .add(projectRepository.getFrameTime())
                                .multiply(BigDecimal.valueOf(audioSampleRate)).intValue();

                        long nextSampleCount = allAudioSamples + frame.getAudioResult().getNumberSamples();
                        int deltaSamples = (int) (nextSampleCount - expectedNumberOfSamples);

                        LOGGER.trace("Audio rendering delta samples = {}", deltaSamples);

                        int numberOfSamples = frame.getAudioResult().getNumberSamples();
                        if (deltaSamples > 0) {
                            numberOfSamples = frame.getAudioResult().getNumberSamples() - deltaSamples;
                        } else if (deltaSamples < 0) {
                            LOGGER.error("Delta samples are less than 0, this will cause misalignment of audio and video. This is not supposed to happen");
                        }
                        array[0].audioData = convertAudio(frame.getAudioResult(), numberOfSamples);
                        array[0].numberOfAudioSamples = numberOfSamples;
                        allAudioSamples += numberOfSamples;

                        audioConverted = true;
                    }
                    nativeRequest.encoderIndex = encoderIndex;
                    nativeRequest.startFrameIndex = frameIndex;

                    int encodeResult = ffmpegBasedMediaEncoder.encodeFrames(nativeRequest);
                    if (encodeResult < 0) {
                        throw new RuntimeException("Cannot encode frames, error code " + encodeResult);
                    }
                    if (frame.getVideoResult() != null) {
                        renderRequest.getEncodedImageCallback().accept(frame.getVideoResult());
                    }
                    frame.free();
                    if (audioConverted) {
                        GlobalMemoryManagerAccessor.memoryManager.returnBuffer(array[0].audioData);
                    }

                    messagingService.sendAsyncMessage(new ProgressAdvancedMessage(renderRequest.getRenderId(), 1));
                    ++frameIndex;
                }

            }

            try {
                producerThread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            FFmpegClearEncoderRequest clearRequest = new FFmpegClearEncoderRequest();
            clearRequest.encoderIndex = encoderIndex;
            ffmpegBasedMediaEncoder.clearEncoder(clearRequest);
            executorService.shutdownNow();
        }
    }

    private void fillChapters(ChapterInformation[] chaptersArray, Map<TimelinePosition, String> chapters) {
        int i = 0;
        for (var entry : chapters.entrySet()) {
            chaptersArray[i].name = entry.getValue();
            chaptersArray[i].timeInMicroseconds = entry.getKey().getSeconds().multiply(SECONDS_TO_MICROSECONDS).longValue();
            ++i;
        }
    }

    private NativeMap convertToNativeMap(Map<String, String> metadataMap) {
        if (metadataMap.size() == 0) {
            return null;
        } else {
            NativePair nativePairs = new NativePair();
            NativePair[] nativePairMap = (NativePair[]) nativePairs.toArray(metadataMap.size());
            int i = 0;
            for (var entry : metadataMap.entrySet()) {
                nativePairMap[i].key = entry.getKey();
                nativePairMap[i].value = entry.getValue();
                ++i;
            }

            NativeMap map = new NativeMap();
            map.size = metadataMap.size();
            map.data = nativePairs;
            return map;
        }
    }

    private CompletableFuture<FFmpegRenderThreadResult> pollQueue(BlockingQueue<CompletableFuture<FFmpegRenderThreadResult>> queue) {
        try {
            return queue.poll(1, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private ByteBuffer convertAudio(AudioFrameResult audioResult, int numberOfSamples) {
        ByteBuffer result = GlobalMemoryManagerAccessor.memoryManager.requestBuffer(audioResult.getBytesPerSample() * numberOfSamples * audioResult.getChannels().size());

        int index = 0;
        for (int i = 0; i < numberOfSamples; ++i) {
            for (int j = 0; j < audioResult.getChannels().size(); ++j) {
                for (int k = 0; k < audioResult.getBytesPerSample(); ++k) {
                    result.put(index++, audioResult.getChannels().get(j).get(i * audioResult.getBytesPerSample() + k));
                }
            }
        }

        return result;
    }

    @Override
    public String getId() {
        return "ffmpegrenderer";
    }

    @Override
    public List<String> getSupportedFormats() {
        return List.of("mpeg");
    }

    @Override
    public boolean supports(RenderRequest renderRequest) {
        // return renderRequest.getFileName().endsWith(".mpeg");
        return true;
    }

    @Override
    public Map<String, OptionProvider<?>> getOptionProviders(CreateValueProvidersRequest request) {
        int maximumVideoBitRate = timelineManagerAccessor.findMaximumVideoBitRate();
        int sourceVideoBitrate = maximumVideoBitRate > 0 ? maximumVideoBitRate : 3200000;
        OptionProvider<Integer> bitRateProvider = OptionProvider.integerOptionBuilder()
                .withTitle("Video bitrate")
                .withDefaultValue(sourceVideoBitrate)
                .withIsEnabled(f -> !isAudioContainerForExtension(FilenameUtils.getExtension(f.getFileName())))
                .withExamples(List.of(
                        new OptionExample<>(sourceVideoBitrate, String.format("%.2f Mbps (source)", (sourceVideoBitrate / 1_000_000.0))),
                        new OptionExample<>(45_000_000, "45 Mbps (4k 30fps YT)"),
                        new OptionExample<>(16_000_000, "16 Mbps (1440p 30fps YT)"),
                        new OptionExample<>(8_000_000, "8 Mbps (1080p 30fps YT)"),
                        new OptionExample<>(5_000_000, "5 Mbps (720p 30fps YT)"),
                        new OptionExample<>(2_500_000, "2.5 Mbps (480p 30fps YT)"),
                        new OptionExample<>(1_000_000, "1 Mbps (360p 30fps YT)"),

                        new OptionExample<>(68_000_000, "68 Mbps (4k 60fps YT)"),
                        new OptionExample<>(24_000_000, "24 Mbps (1440p 60fps YT)"),
                        new OptionExample<>(12_000_000, "12 Mbps (1080p 60fps YT)"),
                        new OptionExample<>(7_500_000, "7.5 Mbps (720p 60fps YT)"),
                        new OptionExample<>(4_000_000, "4 Mbps (480p 60fps YT)"),
                        new OptionExample<>(1_500_000, "1.5 Mbps (360p 60fps YT)")))
                .withValidationErrorProvider(bitRate -> {
                    List<String> errors = new ArrayList<>();
                    if (bitRate < 100) {
                        errors.add("Too low bitrate");
                    }
                    return errors;
                })
                .build();
        int maximumAudioBitRate = timelineManagerAccessor.findMaximumAudioBitRate();
        int sourceAudioBitrate = maximumAudioBitRate > 0 ? maximumAudioBitRate : 192000;
        OptionProvider<Integer> audioBitRateProvider = OptionProvider.integerOptionBuilder()
                .withTitle("Audio bitrate")
                .withDefaultValue(sourceAudioBitrate)
                .withExamples(List.of(
                        new OptionExample<>(sourceAudioBitrate, String.format("%.0f kbps (source)", (sourceAudioBitrate / 1_000.0))),
                        new OptionExample<>(384_000, "384 kbps (Stereo YT)"),
                        new OptionExample<>(128_000, "128 kbps (Mono YT)")))
                .withValidationErrorProvider(bitRate -> {
                    List<String> errors = new ArrayList<>();
                    if (bitRate < 100) {
                        errors.add("Too low bitrate");
                    }
                    return errors;
                })
                .build();
        OptionProvider<Integer> audioSampleRateProvider = OptionProvider.integerOptionBuilder()
                .withTitle("Audio samplerate")
                .withDefaultValue(projectRepository.getSampleRate())
                .withValidationErrorProvider(bitRate -> {
                    List<String> errors = new ArrayList<>();
                    return errors;
                })
                .withExamples(List.of(
                        new OptionExample<>(projectRepository.getSampleRate(), String.valueOf(projectRepository.getSampleRate()) + " (source)"),
                        new OptionExample<>(44100, "44100 (CD)"),
                        new OptionExample<>(8000, "8000 (Telephone) "),
                        new OptionExample<>(22050, "22050 (AM audio) "),
                        new OptionExample<>(96000, "96000 (DVD) ")))
                .build();
        OptionProvider<String> audioBytesPerSampelProvider = OptionProvider.stringOptionBuilder()
                .withTitle("Bytes/sample")
                .withDefaultValue(projectRepository.getBytesPerSample() + "")
                .withValidValues(List.of(createElement("1"), createElement("2"), createElement("4")))
                .build();
        OptionProvider<String> numberOfChannelsProvider = OptionProvider.stringOptionBuilder()
                .withTitle("Number of channels")
                .withDefaultValue(projectRepository.getNumberOfChannels() + "")
                .withValidValues(
                        IntStream.range(1, 3) // TODO: Check why can't we use more channels
                                .mapToObj(a -> String.valueOf(a))
                                .map(a -> createElement(a))
                                .collect(Collectors.toList()))
                .build();

        CodecValueElements codecs = queryCodecInformation();

        OptionProvider<String> videoCodecProvider = OptionProvider.stringOptionBuilder()
                .withTitle("Video codec")
                .withIsEnabled(f -> !isAudioContainerForFileName(f.getFileName(), f.getSelectedExtensionType()))
                .withDefaultValue(DEFAULT_VALUE)
                .withValidValues(codecs.videoCodecs)
                .withShouldTriggerUpdate(true)
                .build();
        OptionProvider<String> audioCodecProvider = OptionProvider.stringOptionBuilder()
                .withTitle("Audio codec")
                .withDefaultValue(DEFAULT_VALUE)
                .withValidValues(codecs.audioCodecs)
                .build();

        EncoderExtraData extra = queryCodecExtraData(request.fileName, DEFAULT_VALUE);

        OptionProvider<String> videoPixelFormatProvider = OptionProvider.stringOptionBuilder()
                .withTitle("Video pixel format")
                .withIsEnabled(f -> !isAudioContainerForExtension(FilenameUtils.getExtension(f.getFileName())))
                .withDefaultValue(DEFAULT_VALUE)
                .withValidValues(extra.pixelFormats)
                .build();

        List<ValueListElement> presets = new ArrayList<>();
        presets.add(new ValueListElement("ultrafast", "ultrafast"));
        presets.add(new ValueListElement("superfast", "superfast"));
        presets.add(new ValueListElement("veryfast", "veryfast"));
        presets.add(new ValueListElement("faster", "faster"));
        presets.add(new ValueListElement("fast", "fast"));
        presets.add(new ValueListElement("medium", "medium"));
        presets.add(new ValueListElement("slow", "slow"));
        presets.add(new ValueListElement("slower", "slower"));
        presets.add(new ValueListElement("veryslow", "veryslow"));

        OptionProvider<String> presetProviders = OptionProvider.stringOptionBuilder()
                .withTitle("Preset")
                .withDefaultValue("medium")
                .withValidValues(presets)
                .withShouldShow(a -> a.getFileName().endsWith(".mp4"))
                .build();

        int threads = Runtime.getRuntime().availableProcessors() - 2;
        if (threads < 1) {
            threads = 1;
        }

        OptionProvider<Integer> numberOfThreadsProvider = OptionProvider.integerOptionBuilder()
                .withTitle("Thread number")
                .withDefaultValue(threads)
                .withValidValues(createThreadsValidValues())
                .withValidationErrorProvider(t -> {
                    List<String> errors = new ArrayList<>();

                    if (t < 1) {
                        errors.add("At least 1 thread is needed");
                    }

                    return errors;
                })
                .build();

        OptionProvider<File> coverPhotoProvider = OptionProvider.fileOptionBuilder()
                .withTitle("Cover image")
                .withDefaultValue(new File(""))
                .withShouldShow(v -> isAudioContainerForFileName(v.getFileName(), v.getSelectedExtensionType()) && !v.getFileName().endsWith(".wav"))
                .withValidationErrorProvider(t -> {
                    List<String> errors = new ArrayList<>();

                    if (!t.getName().equals("") && t.exists()) {
                        errors.add("File not found");
                    } else if (loadMetadata(t) != null) {
                        errors.add("Not valid image");
                    }

                    return errors;
                })
                .build();

        LinkedHashMap<String, OptionProvider<?>> result = new LinkedHashMap<>();

        result.put("videocodec", videoCodecProvider);
        result.put("videobitrate", bitRateProvider);
        result.put("videoPixelFormat", videoPixelFormatProvider);

        result.put("audiocodec", audioCodecProvider);
        result.put("audiobitrate", audioBitRateProvider);
        result.put("audiosamplerate", audioSampleRateProvider);

        result.put("audiobytespersample", audioBytesPerSampelProvider);
        result.put("audionumberofchannels", numberOfChannelsProvider);

        result.put("preset", presetProviders);
        result.put("threads", numberOfThreadsProvider);

        result.put("coverPhoto", coverPhotoProvider);

        return result;
    }

    private List<ValueListElement> createThreadsValidValues() {
        List<ValueListElement> result = new ArrayList<>();
        int cores = Runtime.getRuntime().availableProcessors();
        result.add(createElement("1"));
        for (int i = 2; i <= cores; ++i) {
            result.add(createElement(i + ""));
        }
        return result;
    }

    private ImageMetadataResponse loadMetadata(File t) {
        try {
            ImageMetadataRequest request = new ImageMetadataRequest();
            request.path = t.getAbsolutePath();
            return coverPhotoLoader.readMetadata(request);
        } catch (Exception e) {
            return null;
        }
    }

    private ByteBuffer loadCoverPhotoImage(File t, ImageMetadataResponse imageMetadataResponse) {
        ImageRequest request = new ImageRequest();
        request.width = imageMetadataResponse.width;
        request.height = imageMetadataResponse.height;
        request.path = t.getAbsolutePath();
        coverPhotoLoader.readImage(request);

        return request.data;
    }

    @Override
    public Map<String, OptionProvider<?>> updateValueProviders(UpdateValueProvidersRequest request) {
        Map<String, OptionProvider<?>> optionsToUpdate = new LinkedHashMap<>(request.options);

        // update pixel formats
        String codec = (String) optionsToUpdate.get("videocodec").getValue();
        OptionProvider<?> pixelFormat = optionsToUpdate.get("videoPixelFormat");
        EncoderExtraData extra = queryCodecExtraData(request.fileName, codec);
        if (!pixelFormat.getValidValues().equals(extra.pixelFormats)) {
            optionsToUpdate.put("videoPixelFormat", pixelFormat.butWithUpdatedValidValues(extra.pixelFormats));
        }

        return optionsToUpdate;
    }

    private boolean isAudioContainerForFileName(String fileName, HandledExtensionValueElement handledExtensionValueElement) {
        if (handledExtensionValueElement != null) {
            return handledExtensionValueElement.extensionType.equals(ExtensionType.AUDIO);
        }
        String[] parts = fileName.split("\\.");
        if (parts.length == 0) {
            return false;
        }
        String extension = parts[parts.length - 1];
        return isAudioContainerForExtension(extension);
    }

    private boolean isAudioContainerForExtension(String extension) {
        if (extension == null) {
            return false;
        }
        return COMMON_AUDIO_CONTAINERS.contains(extension);
    }

    private ValueListElement createElement(String e) {
        return new ValueListElement(e, e);
    }

    static class CodecValueElements {
        List<ValueListElement> videoCodecs = new ArrayList<>();
        List<ValueListElement> audioCodecs = new ArrayList<>();
    }

    private CodecValueElements queryCodecInformation() {
        QueryCodecRequest nativeRequest = new QueryCodecRequest();
        nativeRequest.audioCodecs = new CodecInformation();
        CodecInformation[] audioCodecs = (CodecInformation[]) nativeRequest.audioCodecs.toArray(MAX_NUMBER_OF_CODECS);
        nativeRequest.videoCodecs = new CodecInformation();
        CodecInformation[] videoCodecs = (CodecInformation[]) nativeRequest.videoCodecs.toArray(MAX_NUMBER_OF_CODECS);

        ffmpegBasedMediaEncoder.queryCodecs(nativeRequest);

        CodecValueElements result = new CodecValueElements();

        result.videoCodecs.add(new ValueListElement(DEFAULT_VALUE, DEFAULT_VALUE));
        result.audioCodecs.add(new ValueListElement(DEFAULT_VALUE, DEFAULT_VALUE));

        result.audioCodecs.add(new ValueListElement(NONE_VALUE, "Do not render audio"));
        result.videoCodecs.add(new ValueListElement(NONE_VALUE, "Do not render video"));

        for (int i = 0; i < nativeRequest.videoCodecNumber; ++i) {
            result.videoCodecs.add(new ValueListElement(videoCodecs[i].id, "[" + videoCodecs[i].id + "] " + videoCodecs[i].longName));
        }
        for (int i = 0; i < nativeRequest.audioCodecNumber; ++i) {
            result.audioCodecs.add(new ValueListElement(audioCodecs[i].id, "[" + audioCodecs[i].id + "] " + audioCodecs[i].longName));
        }

        return result;
    }

    static class EncoderExtraData {
        List<ValueListElement> pixelFormats = new ArrayList<>();
    }

    private EncoderExtraData queryCodecExtraData(String fileName, String videoCodec) {
        CodecExtraDataRequest nativeRequest = new CodecExtraDataRequest();
        nativeRequest.availablePixelFormatNumber = 0;
        nativeRequest.fileName = fileName;
        nativeRequest.videoCodec = videoCodec;
        nativeRequest.availablePixelFormats = new CodecInformation();
        CodecInformation[] availablePixelFormats = (CodecInformation[]) nativeRequest.availablePixelFormats.toArray(MAX_NUMBER_OF_CODECS);

        ffmpegBasedMediaEncoder.queryCodecExtraData(nativeRequest);

        EncoderExtraData result = new EncoderExtraData();

        result.pixelFormats.add(new ValueListElement(DEFAULT_VALUE, DEFAULT_VALUE));
        for (int i = 0; i < nativeRequest.availablePixelFormatNumber; ++i) {
            result.pixelFormats.add(new ValueListElement(availablePixelFormats[i].id, availablePixelFormats[i].id));
        }
        return result;
    }

    @Override
    public List<HandledExtensionValueElement> handledExtensions() {
        return Arrays.asList(
                new HandledExtensionValueElement("mp4", "mp4", VIDEO),
                new HandledExtensionValueElement("ogg_video", "ogg", "ogg", VIDEO),
                new HandledExtensionValueElement("flv", "flv", VIDEO),
                new HandledExtensionValueElement("webm", "webm", VIDEO),
                new HandledExtensionValueElement("avi", "avi", VIDEO),
                new HandledExtensionValueElement("gif", "gif (animated)", VIDEO),
                new HandledExtensionValueElement("wmv", "wmv", VIDEO),
                new HandledExtensionValueElement("mp3", "mp3 (audio only)", AUDIO),
                new HandledExtensionValueElement("wav", "wav (audio only)", AUDIO),
                new HandledExtensionValueElement("oga", "oga (audio only)", AUDIO),
                new HandledExtensionValueElement("ogg_audio", "ogg", "ogg vorbis (audio only)", AUDIO));
    }

}
