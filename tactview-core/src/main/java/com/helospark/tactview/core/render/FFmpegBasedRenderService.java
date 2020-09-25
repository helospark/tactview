package com.helospark.tactview.core.render;

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
import com.helospark.tactview.core.optionprovider.OptionProvider;
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
import com.helospark.tactview.core.render.helper.IntervalThreadingPartitioner;
import com.helospark.tactview.core.render.helper.ThreadingAccessorResult;
import com.helospark.tactview.core.repository.ProjectRepository;
import com.helospark.tactview.core.timeline.AudioFrameResult;
import com.helospark.tactview.core.timeline.AudioVideoFragment;
import com.helospark.tactview.core.timeline.MergeOnIntersectingIntervalList;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.timeline.TimelineManagerRenderService;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ValueListElement;
import com.helospark.tactview.core.timeline.effect.scale.service.ScaleService;
import com.helospark.tactview.core.timeline.message.progress.ProgressAdvancedMessage;
import com.helospark.tactview.core.util.messaging.MessagingService;

@Component
public class FFmpegBasedRenderService extends AbstractRenderService {
    private static final Logger LOGGER = LoggerFactory.getLogger(FFmpegBasedRenderService.class);
    private static final Set<String> COMMON_AUDIO_CONTAINERS = Set.of("mp3", "wav", "oga");
    private static final Set<String> COMMON_VIDEO_CONTAINERS = Set.of("mp4", "ogg", "flv", "webm", "avi", "gif", "wmv");
    private static final String DEFAULT_VALUE = "default";
    private static final String NONE_VALUE = "none";
    private static final int MAX_NUMBER_OF_CODECS = 400;
    private final FFmpegBasedMediaEncoder ffmpegBasedMediaEncoder;
    private final TimelineManagerAccessor timelineManagerAccessor;
    private final IntervalThreadingPartitioner intervalThreadingPartitioner;

    public FFmpegBasedRenderService(TimelineManagerRenderService timelineManager, FFmpegBasedMediaEncoder ffmpegBasedMediaEncoder, MessagingService messagingService,
            ScaleService scaleService, TimelineManagerAccessor timelineManagerAccessor, ProjectRepository projectRepository, IntervalThreadingPartitioner intervalThreadingPartitioner) {
        super(timelineManager, messagingService, scaleService, projectRepository);
        this.ffmpegBasedMediaEncoder = ffmpegBasedMediaEncoder;
        this.timelineManagerAccessor = timelineManagerAccessor;
        this.intervalThreadingPartitioner = intervalThreadingPartitioner;
    }

    @Override
    public void renderInternal(RenderRequest renderRequest) {

        int videoBitRate = (int) renderRequest.getOptions().get("videobitrate").getValue();

        System.out.println("Video BitRate: " + videoBitRate);

        int audioBitRate = (int) renderRequest.getOptions().get("audiobitrate").getValue();
        System.out.println("Audio BitRate: " + audioBitRate);

        int audioSampleRate = (int) renderRequest.getOptions().get("audiosamplerate").getValue();
        System.out.println("Audio SampleRate: " + audioSampleRate);

        int bytesPerSample = Integer.parseInt(renderRequest.getOptions().get("audiobytespersample").getValue().toString());
        System.out.println("Audio bytes per sample: " + bytesPerSample);

        String videoCodec = (String) renderRequest.getOptions().get("videocodec").getValue();
        System.out.println("VideoCodec: " + videoCodec);

        String audioCodec = (String) renderRequest.getOptions().get("audiocodec").getValue();
        System.out.println("AudioCodec: " + audioCodec);

        String videoPixelFormat = (String) renderRequest.getOptions().get("videoPixelFormat").getValue();
        System.out.println("videoPixelFormat: " + videoPixelFormat);

        int numberOfChannels = Integer.parseInt(renderRequest.getOptions().get("audionumberofchannels").getValue().toString());
        System.out.println("numberOfChannels: " + numberOfChannels);

        String videoPresetOrNull = (String) Optional.ofNullable(renderRequest.getOptions().get("preset")).map(a -> a.getValue()).orElse(null);
        System.out.println("video preset: " + videoPresetOrNull);

        int threads = Integer.parseInt(renderRequest.getOptions().get("threads").getValue().toString());
        System.out.println("threads: " + threads);

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
        initNativeRequest.videoCodec = videoCodec;
        initNativeRequest.audioCodec = audioCodec;
        initNativeRequest.videoPixelFormat = videoPixelFormat;
        initNativeRequest.videoPreset = videoPresetOrNull;
        initNativeRequest.metadata = convertToNativeMap(renderRequest.getMetadata());
        // frame not freed

        int encoderIndex = ffmpegBasedMediaEncoder.initEncoder(initNativeRequest);

        if (encoderIndex < 0) {
            throw new RuntimeException("Unable to render, statuscode is " + encoderIndex + " , check logs");
        }

        int frameIndex = 0;

        boolean needsVideo = !videoCodec.equals(NONE_VALUE);
        boolean needsAudio = !audioCodec.equals(NONE_VALUE);

        // Producer consumer pattern below

        if (threads < 1) {
            threads = 1;
        }

        BlockingQueue<CompletableFuture<AudioVideoFragment>> queue = new ArrayBlockingQueue<>(threads);
        ExecutorService executorService = Executors.newFixedThreadPool(threads);

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
                        while (currentPosition.isLessOrEqualToThan(renderRequest.getEndPosition()) && !renderRequest.getIsCancelledSupplier().get()) {
                            TimelineInterval nextIntersection = partitionList.size() > partitionIndex ? partitionList.get(partitionIndex) : TimelineInterval.ofPoint(renderRequest.getEndPosition());

                            // TODO: parallel and single threaded part is almost entirely a copy-pase of each other, fix it
                            // parallel render
                            while (currentPosition.isLessOrEqualToThan(renderRequest.getEndPosition())
                                    && currentPosition.isLessThan(nextIntersection.getStartPosition())
                                    && !renderRequest.getIsCancelledSupplier().get()) {

                                TimelinePosition position = currentPosition; // thanks Java...
                                CompletableFuture<AudioVideoFragment> job = CompletableFuture
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
                                                    .build();
                                            return queryFrameAt(superRequest);
                                        }, executorService);
                                queue.put(job);
                                currentPosition = currentPosition.add(renderRequest.getStep());
                            }

                            // single threaded render interval
                            while (currentPosition.isLessOrEqualToThan(renderRequest.getEndPosition())
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
                                        .build();
                                AudioVideoFragment frame = queryFrameAt(superRequest);
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

            // Encoding must be done in single thread
            while (!producerThread.isFinished || queue.peek() != null) {
                CompletableFuture<AudioVideoFragment> future = pollQueue(queue);
                if (future != null) {
                    AudioVideoFragment frame = future.join();
                    FFmpegEncodeFrameRequest nativeRequest = new FFmpegEncodeFrameRequest();
                    nativeRequest.frame = new RenderFFMpegFrame();
                    RenderFFMpegFrame[] array = (RenderFFMpegFrame[]) nativeRequest.frame.toArray(1);
                    if (needsVideo) {
                        array[0].imageData = frame.getVideoResult().getBuffer();
                    }
                    boolean audioConverted = false;
                    if (needsAudio && frame.getAudioResult().getChannels().size() > 0) {
                        array[0].audioData = convertAudio(frame.getAudioResult());
                        array[0].numberOfAudioSamples = frame.getAudioResult().getNumberSamples();
                        audioConverted = true;
                    }
                    nativeRequest.encoderIndex = encoderIndex;
                    nativeRequest.startFrameIndex = frameIndex;

                    int encodeResult = ffmpegBasedMediaEncoder.encodeFrames(nativeRequest);
                    if (encodeResult < 0) {
                        throw new RuntimeException("Cannot encode frames, error code " + encodeResult);
                    }
                    renderRequest.getEncodedImageCallback().accept(frame.getVideoResult());

                    GlobalMemoryManagerAccessor.memoryManager.returnBuffer(frame.getVideoResult().getBuffer());
                    for (var buffer : frame.getAudioResult().getChannels()) {
                        GlobalMemoryManagerAccessor.memoryManager.returnBuffer(buffer);
                    }
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

        } finally {
            FFmpegClearEncoderRequest clearRequest = new FFmpegClearEncoderRequest();
            clearRequest.encoderIndex = encoderIndex;
            ffmpegBasedMediaEncoder.clearEncoder(clearRequest);
            executorService.shutdownNow();
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

    private CompletableFuture<AudioVideoFragment> pollQueue(BlockingQueue<CompletableFuture<AudioVideoFragment>> queue) {
        try {
            return queue.poll(1, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private ByteBuffer convertAudio(AudioFrameResult audioResult) {
        ByteBuffer result = GlobalMemoryManagerAccessor.memoryManager.requestBuffer(audioResult.getBytesPerSample() * audioResult.getNumberSamples() * audioResult.getChannels().size());

        int index = 0;
        for (int i = 0; i < audioResult.getNumberSamples(); ++i) {
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
        OptionProvider<Integer> bitRateProvider = OptionProvider.integerOptionBuilder()
                .withTitle("Video bitrate")
                .withDefaultValue(maximumVideoBitRate > 0 ? maximumVideoBitRate : 3200000)
                .withIsEnabled(f -> !isAudioContainerForExtension(FilenameUtils.getExtension(f.getFileName())))
                .withValidationErrorProvider(bitRate -> {
                    List<String> errors = new ArrayList<>();
                    if (bitRate < 100) {
                        errors.add("Too low bitrate");
                    }
                    return errors;
                })
                .build();
        int maximumAudioBitRate = timelineManagerAccessor.findMaximumAudioBitRate();
        OptionProvider<Integer> audioBitRateProvider = OptionProvider.integerOptionBuilder()
                .withTitle("Audio bitrate")
                .withDefaultValue(maximumAudioBitRate > 0 ? maximumAudioBitRate : 192000)
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
                .withIsEnabled(f -> !isAudioContainerForExtension(FilenameUtils.getExtension(f.getFileName())))
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
                .withValidationErrorProvider(t -> {
                    List<String> errors = new ArrayList<>();

                    if (t < 1) {
                        errors.add("At least 1 thread is needed");
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

        return result;
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

    private boolean isAudioContainerForExtension(String extension) {
        if (extension == null) {
            return false;
        }
        return COMMON_AUDIO_CONTAINERS.contains(extension);
    }

    private boolean isVideoContainer(String extension) {
        if (extension == null) {
            return false;
        }
        return COMMON_VIDEO_CONTAINERS.contains(extension);
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
    public List<ValueListElement> handledExtensions() {
        return Arrays.asList(
                new ValueListElement("mp4", "mp4"),
                new ValueListElement("ogg", "ogg"),
                new ValueListElement("flv", "flv"),
                new ValueListElement("webm", "webm"),
                new ValueListElement("avi", "avi"),
                new ValueListElement("gif", "gif (animated)"),
                new ValueListElement("wmv", "wmv"),
                new ValueListElement("mp3", "mp3 (audio only)"),
                new ValueListElement("wav", "wav (audio only)"),
                new ValueListElement("oga", "oga (audio only)"));
    }

}
