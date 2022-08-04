package com.helospark.tactview.core.timeline;

import static com.helospark.tactview.core.util.async.ExceptionLoggerDecorator.withExceptionLogging;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.slf4j.Logger;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.decoder.framecache.GlobalMemoryManagerAccessor;
import com.helospark.tactview.core.repository.ProjectRepository;
import com.helospark.tactview.core.timeline.TimelineRenderResult.RegularRectangle;
import com.helospark.tactview.core.timeline.blendmode.BlendModeStrategy;
import com.helospark.tactview.core.timeline.effect.interpolation.KeyframeableEffect;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.evaluator.EvaluationContext;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.evaluator.EvaluationContextProviderData;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.evaluator.script.ExpressionScriptEvaluator;
import com.helospark.tactview.core.timeline.effect.transition.AbstractVideoTransitionEffect;
import com.helospark.tactview.core.timeline.framemerge.FrameBufferMerger;
import com.helospark.tactview.core.timeline.framemerge.RenderFrameData;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.timeline.proceduralclip.channelcopy.AdjustmentLayerProceduralClip;
import com.helospark.tactview.core.timeline.render.FrameExtender;
import com.helospark.tactview.core.util.logger.Slf4j;

@Component
public class TimelineManagerRenderService {
    ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(),
            new ThreadFactoryBuilder().setNameFormat("timeline-manager-executor-%d").build());
    private final FrameBufferMerger frameBufferMerger;
    private final AudioBufferMerger audioBufferMerger;
    private final ProjectRepository projectRepository;
    private final FrameExtender frameExtender;
    private final TimelineChannelsState timelineManager;
    private final TimelineManagerAccessor timelineManagerAccessor;
    private final ExpressionScriptEvaluator expressionScriptEvaluator;

    @Slf4j
    private Logger logger;

    public TimelineManagerRenderService(FrameBufferMerger frameBufferMerger, AudioBufferMerger audioBufferMerger, ProjectRepository projectRepository,
            FrameExtender frameExtender, TimelineChannelsState timelineManager, TimelineManagerAccessor timelineManagerAccessor, ExpressionScriptEvaluator expressionScriptEvaluator) {
        this.frameBufferMerger = frameBufferMerger;
        this.audioBufferMerger = audioBufferMerger;
        this.projectRepository = projectRepository;
        this.frameExtender = frameExtender;
        this.timelineManager = timelineManager;
        this.timelineManagerAccessor = timelineManagerAccessor;
        this.expressionScriptEvaluator = expressionScriptEvaluator;
    }

    public TimelineRenderResult getFrame(TimelineManagerFramesRequest request) {
        List<TimelineClip> allClips = timelineManager.channels
                .stream()
                .map(channel -> channel.getDataAt(request.getPosition()))
                .flatMap(Optional::stream)
                .collect(Collectors.toList());

        Map<String, TimelineClip> clipsToRender = allClips
                .stream()
                .collect(Collectors.toMap(a -> a.getId(), a -> a));

        List<String> renderOrder = allClips.stream()
                .filter(a -> a.isEnabled(request.getPosition()))
                .map(a -> a.getId())
                .collect(Collectors.toList());

        List<TreeNode> tree = buildRenderTree(clipsToRender, request.getPosition());

        List<List<TimelineClip>> layers = new ArrayList<>();
        recursiveLayering(tree, 0, layers);

        Map<String, RenderFrameData> clipsToFrames = new ConcurrentHashMap<>();
        Map<String, AudioFrameResult> audioToFrames = new ConcurrentHashMap<>();
        Map<String, RegularRectangle> clipToExpandedPosition = new ConcurrentHashMap<>();

        EvaluationContext evaluationContext = createEvaluationContext(clipsToRender);

        for (int i = 0; i < layers.size(); ++i) {
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            for (var clip : layers.get(i)) {
                if (clip instanceof VisualTimelineClip && request.isNeedVideo()) { // TODO: rest later
                    VisualTimelineClip visualClip = (VisualTimelineClip) clip;

                    futures.add(CompletableFuture.supplyAsync(withExceptionLogging(() -> {
                        ReadOnlyClipImage adjustmentImage = null;
                        ReadOnlyClipImage frameResult = null;
                        ReadOnlyClipImage expandedFrame = null;
                        try {
                            Map<String, ReadOnlyClipImage> requiredClips = visualClip.getClipDependency(request.getPosition())
                                    .stream()
                                    .filter(a -> clipsToFrames.containsKey(a))
                                    .map(a -> clipsToFrames.get(a))
                                    .collect(Collectors.toMap(a -> a.id, a -> a.clipFrameResult));
                            Map<String, ReadOnlyClipImage> channelCopiedClips = visualClip.getChannelDependency(request.getPosition())
                                    .stream()
                                    .flatMap(channelId -> timelineManagerAccessor.findChannelWithId(channelId).stream())
                                    .flatMap(channel -> channel.getDataAt(request.getPosition()).stream())
                                    .filter(a -> clipsToFrames.containsKey(a.getId()))
                                    .map(a -> clipsToFrames.get(a.getId()))
                                    .collect(Collectors.toMap(a -> a.channelId, a -> a.clipFrameResult, (a, b) -> a, HashMap::new));

                            if (clip instanceof AdjustmentLayerProceduralClip) {
                                Map<String, RenderFrameData> framesBelow = new TreeMap<>();
                                int startChannel = timelineManagerAccessor.findChannelIndexForClipId(visualClip.getId()).get() + 1;

                                for (int k = startChannel; k < timelineManager.channels.size(); ++k) {
                                    Optional<TimelineClip> clipAtChannel = timelineManager.channels.get(k).getDataAt(request.getPosition());
                                    if (clipAtChannel.isPresent()) {
                                        String clipId = clipAtChannel.get().getId();
                                        framesBelow.put(clipId, clipsToFrames.get(clipId));
                                    }
                                }

                                adjustmentImage = renderBelowLayers(request, renderOrder, framesBelow);
                                channelCopiedClips.put(AdjustmentLayerProceduralClip.LAYER_ID, adjustmentImage);
                            }

                            GetFrameRequest frameRequest = GetFrameRequest.builder()
                                    .withScale(request.getScale())
                                    .withPosition(request.getPosition())
                                    .withExpectedWidth(request.getPreviewWidth())
                                    .withExpectedHeight(request.getPreviewHeight())
                                    .withApplyEffects(request.isEffectsEnabled())
                                    .withRequestedClips(requiredClips)
                                    .withRequestedChannelClips(channelCopiedClips)
                                    .withLowResolutionPreview(request.isLowResolutionPreview())
                                    .withLivePlayback(request.isLivePlayback())
                                    .withEvaluationContext(evaluationContext.butWithClipId(clip.getId()))
                                    .build();

                            frameResult = visualClip.getFrame(frameRequest);
                            expandedFrame = expandFrame(request, visualClip, frameResult, clipToExpandedPosition);

                            BlendModeStrategy blendMode = visualClip.getBlendModeAt(request.getPosition());
                            double alpha = visualClip.getAlpha(request.getPosition());

                            String channelId = timelineManagerAccessor.findChannelForClipId(visualClip.getId()).get().getId();
                            return new RenderFrameData(visualClip.getId(), alpha, blendMode, expandedFrame,
                                    clip.getEffectsAtGlobalPosition(request.getPosition(), AbstractVideoTransitionEffect.class),
                                    channelId);
                        } finally {
                            if (frameResult != null) {
                                GlobalMemoryManagerAccessor.memoryManager.returnBuffer(frameResult.getBuffer());
                            }
                            if (adjustmentImage != null) {
                                GlobalMemoryManagerAccessor.memoryManager.returnBuffer(adjustmentImage.getBuffer());
                            }
                        }
                    }), executorService).thenAccept(a -> {
                        clipsToFrames.put(visualClip.getId(), a);
                    }).exceptionally(e -> {
                        logger.error("Unable to render", e);
                        return null;
                    }));
                } else if (clip instanceof AudibleTimelineClip && request.isNeedSound()) {
                    AudibleTimelineClip audibleClip = (AudibleTimelineClip) clip;

                    futures.add(CompletableFuture.supplyAsync(withExceptionLogging(() -> {
                        int sampleRateToUse = request.getAudioSampleRate().orElse(projectRepository.getSampleRate());
                        int bytesPerSampleToUse = request.getAudioBytesPerSample().orElse(projectRepository.getBytesPerSample());
                        int numberOfChannels = request.getNumberOfChannels().orElse(projectRepository.getNumberOfChannels());
                        TimelineLength defaultLength = new TimelineLength(projectRepository.getFrameTime());
                        TimelineLength length = request.getAudioLength().orElse(defaultLength);
                        AudioRequest audioRequest = AudioRequest.builder()
                                .withApplyEffects(request.isEffectsEnabled())
                                .withPosition(request.getPosition())
                                .withLength(length)
                                .withSampleRate(sampleRateToUse)
                                .withBytesPerSample(bytesPerSampleToUse)
                                .withNumberOfChannels(numberOfChannels)
                                .build();

                        return audibleClip.requestAudioFrame(audioRequest);

                    }), executorService).exceptionally(e -> {
                        logger.error("Unable to get audio", e);
                        return null;
                    }).thenAccept(a -> {
                        if (a == null) {
                            logger.error("Unable to get audio");
                        } else {
                            audioToFrames.put(audibleClip.getId(), a);
                        }
                    }));

                }
            }
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()])).join();
        }

        ReadOnlyClipImage finalImage = request.isNeedVideo() ? renderVideo(request, renderOrder, clipsToFrames) : null;
        AudioFrameResult audioBuffer = renderAudio(renderOrder, audioToFrames, request);

        clipsToFrames.values()
                .stream()
                .forEach(a -> GlobalMemoryManagerAccessor.memoryManager.returnBuffer(a.clipFrameResult.getBuffer()));
        audioToFrames.values()
                .stream()
                .flatMap(a -> a.getChannels().stream())
                .forEach(a -> GlobalMemoryManagerAccessor.memoryManager.returnBuffer(a));

        ReadOnlyClipImage finalResult = executeGlobalEffectsOn(finalImage);
        // TODO: audio effects

        return new TimelineRenderResult(new AudioVideoFragment(finalResult, audioBuffer), new HashMap<>(clipToExpandedPosition));
    }

    private EvaluationContext createEvaluationContext(Map<String, TimelineClip> clipsToRender) {
        Map<String, EvaluationContextProviderData> clipData = new HashMap<>();
        for (var clip : clipsToRender.values()) {
            Map<String, KeyframeableEffect<?>> data2 = new HashMap<>();
            for (var descriptor : clip.getDescriptors()) {
                data2.put(descriptor.getName(), descriptor.getKeyframeableEffect());
            }
            EvaluationContextProviderData ecpd = new EvaluationContextProviderData(data2);
            clipData.put(clip.getId(), ecpd);
        }
        return new EvaluationContext(clipData, expressionScriptEvaluator);
    }

    private ReadOnlyClipImage renderBelowLayers(TimelineManagerFramesRequest request, List<String> renderOrder, Map<String, RenderFrameData> framesBelow) {
        return renderVideo(request, renderOrder, framesBelow);
    }

    private ReadOnlyClipImage executeGlobalEffectsOn(ReadOnlyClipImage finalImage) {
        return finalImage; // todo: do implementation
    }

    static class TreeNode {
        private final TimelineClip clip;
        private final List<TreeNode> children = new ArrayList<>();

        public TreeNode(TimelineClip clip) {
            this.clip = clip;
        }

    }

    static class RenderAudioFrameData {
        List<ByteBuffer> channels;

        public RenderAudioFrameData(List<ByteBuffer> channels) {
            this.channels = channels;
        }

        public List<ByteBuffer> getChannels() {
            return channels;
        }

    }

    private ClipImage expandFrame(TimelineManagerFramesRequest request, VisualTimelineClip visualClip, ReadOnlyClipImage frameResult,
            Map<String, RegularRectangle> outBoundPositions) {
        FrameExtender.FrameExtendRequest frameExtendRequest = FrameExtender.FrameExtendRequest.builder()
                .withClip(visualClip)
                .withFrameResult(frameResult)
                .withPreviewWidth(request.getPreviewWidth())
                .withPreviewHeight(request.getPreviewHeight())
                .withScale(request.getScale())
                .withTimelinePosition(request.getPosition())
                .withOutBoundPositions(outBoundPositions)
                .build();
        return frameExtender.expandFrame(frameExtendRequest);
    }

    private AudioFrameResult renderAudio(List<String> renderOrder, Map<String, AudioFrameResult> audioToFrames, TimelineManagerFramesRequest request) {
        List<AudioFrameResult> audioFrames = renderOrder.stream()
                .filter(clipId -> {
                    TimelineChannel channelContainingCurrentClip = timelineManagerAccessor.findChannelForClipId(clipId).get();
                    return !channelContainingCurrentClip.isDisabled() && !channelContainingCurrentClip.isMute();
                })
                .map(a -> audioToFrames.get(a))
                .filter(a -> a != null)
                .collect(Collectors.toList());

        AudioFrameResult silence = generateSilence(request);
        audioFrames.add(0, silence);

        AudioFrameResult result = audioBufferMerger.mergeBuffers(audioFrames);

        silence.free();

        return result;
    }

    private AudioFrameResult generateSilence(TimelineManagerFramesRequest request) {
        int sampleRateToUse = request.getAudioSampleRate().orElse(projectRepository.getSampleRate());
        int bytesPerSampleToUse = request.getAudioBytesPerSample().orElse(projectRepository.getBytesPerSample());
        int numberOfChannels = request.getNumberOfChannels().orElse(projectRepository.getNumberOfChannels());
        TimelineLength defaultLength = new TimelineLength(projectRepository.getFrameTime());
        TimelineLength length = request.getAudioLength().orElse(defaultLength);
        int numberOfSamples = length.getSeconds().multiply(BigDecimal.valueOf(sampleRateToUse)).intValue();

        List<ByteBuffer> silenceBuffers = new ArrayList<>(numberOfChannels);
        for (int i = 0; i < numberOfChannels; ++i) {
            ByteBuffer silenceBuffer = GlobalMemoryManagerAccessor.memoryManager.requestBuffer(numberOfSamples * bytesPerSampleToUse);
            silenceBuffers.add(silenceBuffer);
        }
        return new AudioFrameResult(silenceBuffers, sampleRateToUse, bytesPerSampleToUse);
    }

    private ReadOnlyClipImage renderVideo(TimelineManagerFramesRequest request, List<String> renderOrder, Map<String, RenderFrameData> clipsToFrames) {
        List<String> existingFrames = renderOrder.stream().filter(a -> clipsToFrames.containsKey(a)).collect(Collectors.toList());
        Set<String> extraDisabledClips = findExtraDisabledClips(request, existingFrames);

        List<RenderFrameData> frames = renderOrder.stream()
                .filter(clipId -> {
                    TimelineChannel channelContainingCurrentClip = timelineManagerAccessor.findChannelForClipId(clipId).get();
                    if (channelContainingCurrentClip.isDisabled()) {
                        return true;
                    } else {
                        return !extraDisabledClips.contains(clipId);
                    }
                })
                .map(a -> clipsToFrames.get(a))
                .filter(a -> a != null)
                .collect(Collectors.toList());

        return frameBufferMerger.alphaMergeFrames(frames, request);
    }

    private Set<String> findExtraDisabledClips(TimelineManagerFramesRequest request, List<String> renderOrder) {
        Set<String> extraDisabledClips = new HashSet<>();

        for (int i = 0; i < renderOrder.size(); ++i) {
            TimelineClip clip = timelineManagerAccessor.findClipById(renderOrder.get(i)).get();
            if (clip instanceof AdjustmentLayerProceduralClip) {
                boolean hideClips = ((AdjustmentLayerProceduralClip) clip).shouldHideBelowClips(request.getPosition());
                if (hideClips) {
                    for (int j = i + 1; j < renderOrder.size(); ++j) {
                        extraDisabledClips.add(renderOrder.get(j));
                    }
                }
            }
        }
        return extraDisabledClips;
    }

    private List<TreeNode> buildRenderTree(Map<String, TimelineClip> clipsToRender, TimelinePosition position) {
        List<TreeNode> tree = new ArrayList<>();

        for (var clip : clipsToRender.values()) {
            if (clip.getClipDependency(position).isEmpty() && getChannelDependencies(clip, position).isEmpty()) {
                tree.add(new TreeNode(clip));
            }
        }
        List<TreeNode> treeNodeToUpdate = new ArrayList<>(tree);
        List<TreeNode> treeNodeToUpdateTmp = new ArrayList<>();
        int safetyLoopEscape = 0;
        while (!treeNodeToUpdate.isEmpty() && safetyLoopEscape < 1000) {
            for (var node : treeNodeToUpdate) {
                List<TreeNode> nodesDependentOnCurrentNode = findNodesDependentOn(clipsToRender, node.clip.id, position);
                if (!nodesDependentOnCurrentNode.isEmpty()) {
                    treeNodeToUpdateTmp.addAll(nodesDependentOnCurrentNode);
                    node.children.addAll(nodesDependentOnCurrentNode);
                    // TODO: cycle check
                }
            }
            treeNodeToUpdate.clear();
            treeNodeToUpdate.addAll(treeNodeToUpdateTmp);
            treeNodeToUpdateTmp.clear();
            ++safetyLoopEscape;
        }
        if (safetyLoopEscape >= 1000) {
            throw new IllegalStateException("Unexpected cycle in clips");
        }
        return tree;
    }

    private List<TreeNode> findNodesDependentOn(Map<String, TimelineClip> clipsToRender, String dependentClipId, TimelinePosition position) {
        List<TreeNode> result = new ArrayList<>();
        String dependentClipChannel = timelineManagerAccessor.findChannelForClipId(dependentClipId).map(channel -> channel.getId()).get();
        for (TimelineClip clip : clipsToRender.values()) {
            if (clip.getClipDependency(position).contains(dependentClipId) || getChannelDependencies(clip, position).contains(dependentClipChannel)) {
                result.add(new TreeNode(clip));
            }
        }
        return result;
    }

    private List<String> getChannelDependencies(TimelineClip clip, TimelinePosition position) {
        List<String> result = clip.getChannelDependency(position);

        if (clip instanceof AdjustmentLayerProceduralClip) {
            int clipIndex = timelineManagerAccessor.findChannelIndexForClipId(clip.getId()).get();
            for (int i = clipIndex + 1; i < timelineManager.channels.size(); ++i) {
                result.add(timelineManager.channels.get(i).getId());
            }
        }

        return result;
    }

    private void recursiveLayering(List<TreeNode> tree, int i, List<List<TimelineClip>> layers) {
        if (tree.isEmpty()) {
            return;
        }
        List<TimelineClip> currentLayer;
        if (layers.size() < i) {
            currentLayer = layers.get(i);
        } else {
            currentLayer = new ArrayList<>();
            layers.add(currentLayer);
        }
        for (var element : tree) {
            currentLayer.add(element.clip);
            recursiveLayering(element.children, i + 1, layers);
        }
    }

}
