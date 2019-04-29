package com.helospark.tactview.core.timeline;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.slf4j.Logger;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.decoder.framecache.GlobalMemoryManagerAccessor;
import com.helospark.tactview.core.repository.ProjectRepository;
import com.helospark.tactview.core.timeline.blendmode.BlendModeStrategy;
import com.helospark.tactview.core.timeline.effect.transition.AbstractVideoTransitionEffect;
import com.helospark.tactview.core.timeline.framemerge.FrameBufferMerger;
import com.helospark.tactview.core.timeline.framemerge.RenderFrameData;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.timeline.render.FrameExtender;
import com.helospark.tactview.core.util.logger.Slf4j;

@Component
public class TimelineManagerRenderService {
    ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private FrameBufferMerger frameBufferMerger;
    private AudioBufferMerger audioBufferMerger;
    private ProjectRepository projectRepository;
    private FrameExtender frameExtender;
    private TimelineChannelsState timelineManager;
    private TimelineManagerAccessor timelineManagerAccessor;

    @Slf4j
    private Logger logger;

    public TimelineManagerRenderService(FrameBufferMerger frameBufferMerger, AudioBufferMerger audioBufferMerger, ProjectRepository projectRepository,
            FrameExtender frameExtender, TimelineChannelsState timelineManager, TimelineManagerAccessor timelineManagerAccessor) {
        this.frameBufferMerger = frameBufferMerger;
        this.audioBufferMerger = audioBufferMerger;
        this.projectRepository = projectRepository;
        this.frameExtender = frameExtender;
        this.timelineManager = timelineManager;
        this.timelineManagerAccessor = timelineManagerAccessor;
    }

    public AudioVideoFragment getFrame(TimelineManagerFramesRequest request) {
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

        for (int i = 0; i < layers.size(); ++i) {
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            for (var clip : layers.get(i)) {
                if (clip instanceof VisualTimelineClip && request.isNeedVideo()) { // TODO: rest later
                    VisualTimelineClip visualClip = (VisualTimelineClip) clip;

                    futures.add(CompletableFuture.supplyAsync(() -> {
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
                                .collect(Collectors.toMap(a -> a.channelId, a -> a.clipFrameResult));

                        GetFrameRequest frameRequest = GetFrameRequest.builder()
                                .withScale(request.getScale())
                                .withPosition(request.getPosition())
                                .withExpectedWidth(request.getPreviewWidth())
                                .withExpectedHeight(request.getPreviewHeight())
                                .withApplyEffects(request.isEffectsEnabled())
                                .withRequestedClips(requiredClips)
                                .withRequestedChannelClips(channelCopiedClips)
                                .build();

                        ReadOnlyClipImage frameResult = visualClip.getFrame(frameRequest);
                        ReadOnlyClipImage expandedFrame = expandFrame(request, visualClip, frameResult);

                        BlendModeStrategy blendMode = visualClip.getBlendModeAt(request.getPosition());
                        double alpha = visualClip.getAlpha(request.getPosition());

                        GlobalMemoryManagerAccessor.memoryManager.returnBuffer(frameResult.getBuffer());

                        String channelId = timelineManagerAccessor.findChannelForClipId(visualClip.getId()).get().getId();
                        return new RenderFrameData(visualClip.getId(), alpha, blendMode, expandedFrame, clip.getEffectsAtGlobalPosition(request.getPosition(), AbstractVideoTransitionEffect.class),
                                channelId);
                    }, executorService).thenAccept(a -> {
                        clipsToFrames.put(visualClip.getId(), a);
                    }).exceptionally(e -> {
                        logger.error("Unable to render", e);
                        return null;
                    }));
                } else if (clip instanceof AudibleTimelineClip && request.isNeedSound()) {
                    AudibleTimelineClip audibleClip = (AudibleTimelineClip) clip;

                    futures.add(CompletableFuture.supplyAsync(() -> {
                        int sampleRateToUse = request.getAudioSampleRate().orElse(projectRepository.getSampleRate());
                        int bytesPerSampleToUse = request.getAudioBytesPerSample().orElse(projectRepository.getBytesPerSample());
                        AudioRequest audioRequest = AudioRequest.builder()
                                .withApplyEffects(request.isEffectsEnabled())
                                .withPosition(request.getPosition())
                                .withLength(new TimelineLength(BigDecimal.valueOf(1).divide(projectRepository.getFps(), 100, RoundingMode.HALF_DOWN)))
                                .withSampleRate(sampleRateToUse)
                                .withBytesPerSample(bytesPerSampleToUse)
                                .build();

                        return audibleClip.requestAudioFrame(audioRequest);

                    }, executorService).exceptionally(e -> {
                        logger.error("Unable to get audio", e);
                        return null;
                    }).thenAccept(a -> {
                        audioToFrames.put(audibleClip.getId(), a);
                    }));

                }
            }
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()])).join();
        }

        ReadOnlyClipImage finalImage = renderVideo(request, renderOrder, clipsToFrames);
        AudioFrameResult audioBuffer = renderAudio(renderOrder, audioToFrames);

        clipsToFrames.values()
                .stream()
                .forEach(a -> GlobalMemoryManagerAccessor.memoryManager.returnBuffer(a.clipFrameResult.getBuffer()));
        audioToFrames.values()
                .stream()
                .flatMap(a -> a.getChannels().stream())
                .forEach(a -> GlobalMemoryManagerAccessor.memoryManager.returnBuffer(a));

        ReadOnlyClipImage finalResult = executeGlobalEffectsOn(finalImage);
        // TODO: audio effects

        return new AudioVideoFragment(finalResult, audioBuffer);
    }

    private ReadOnlyClipImage executeGlobalEffectsOn(ReadOnlyClipImage finalImage) {
        return finalImage; // todo: do implementation
    }

    static class TreeNode {
        private TimelineClip clip;
        private List<TreeNode> children = new ArrayList<>();

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

    private ClipImage expandFrame(TimelineManagerFramesRequest request, VisualTimelineClip visualClip, ReadOnlyClipImage frameResult) {
        FrameExtender.FrameExtendRequest frameExtendRequest = FrameExtender.FrameExtendRequest.builder()
                .withClip(visualClip)
                .withFrameResult(frameResult)
                .withPreviewWidth(request.getPreviewWidth())
                .withPreviewHeight(request.getPreviewHeight())
                .withScale(request.getScale())
                .withTimelinePosition(request.getPosition())
                .build();
        return frameExtender.expandFrame(frameExtendRequest);
    }

    private AudioFrameResult renderAudio(List<String> renderOrder, Map<String, AudioFrameResult> audioToFrames) {
        List<AudioFrameResult> audioFrames = renderOrder.stream()
                .filter(clipId -> {
                    TimelineChannel channelContainingCurrentClip = timelineManagerAccessor.findChannelForClipId(clipId).get();
                    return !channelContainingCurrentClip.isDisabled() && !channelContainingCurrentClip.isMute();
                })
                .map(a -> audioToFrames.get(a))
                .filter(a -> a != null)
                .collect(Collectors.toList());

        AudioFrameResult audioBuffer = audioBufferMerger.mergeBuffers(audioFrames);
        return audioBuffer;
    }

    private ReadOnlyClipImage renderVideo(TimelineManagerFramesRequest request, List<String> renderOrder, Map<String, RenderFrameData> clipsToFrames) {
        List<RenderFrameData> frames = renderOrder.stream()
                .filter(clipId -> {
                    TimelineChannel channelContainingCurrentClip = timelineManagerAccessor.findChannelForClipId(clipId).get();
                    return !channelContainingCurrentClip.isDisabled();
                })
                .map(a -> clipsToFrames.get(a))
                .filter(a -> a != null)
                .collect(Collectors.toList());

        ReadOnlyClipImage finalImage = frameBufferMerger.alphaMergeFrames(frames, request);
        return finalImage;
    }

    private List<TreeNode> buildRenderTree(Map<String, TimelineClip> clipsToRender, TimelinePosition position) {
        List<TreeNode> tree = new ArrayList<>();

        for (var clip : clipsToRender.values()) {
            if (clip.getClipDependency(position).isEmpty() && clip.getChannelDependency(position).isEmpty()) {
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
            if (clip.getClipDependency(position).contains(dependentClipId) || clip.getChannelDependency(position).contains(dependentClipChannel)) {
                result.add(new TreeNode(clip));
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
