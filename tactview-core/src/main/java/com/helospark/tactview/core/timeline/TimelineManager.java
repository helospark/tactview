package com.helospark.tactview.core.timeline;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.slf4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.Saveable;
import com.helospark.tactview.core.decoder.framecache.GlobalMemoryManagerAccessor;
import com.helospark.tactview.core.repository.ProjectRepository;
import com.helospark.tactview.core.timeline.blendmode.BlendModeStrategy;
import com.helospark.tactview.core.timeline.effect.CreateEffectRequest;
import com.helospark.tactview.core.timeline.effect.EffectFactory;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.transition.AbstractVideoTransitionEffect;
import com.helospark.tactview.core.timeline.message.ChannelAddedMessage;
import com.helospark.tactview.core.timeline.message.ChannelRemovedMessage;
import com.helospark.tactview.core.timeline.message.ClipAddedMessage;
import com.helospark.tactview.core.timeline.message.ClipDescriptorsAdded;
import com.helospark.tactview.core.timeline.message.ClipMovedMessage;
import com.helospark.tactview.core.timeline.message.ClipRemovedMessage;
import com.helospark.tactview.core.timeline.message.ClipResizedMessage;
import com.helospark.tactview.core.timeline.message.EffectAddedMessage;
import com.helospark.tactview.core.timeline.message.EffectDescriptorsAdded;
import com.helospark.tactview.core.timeline.message.EffectMovedMessage;
import com.helospark.tactview.core.timeline.message.EffectRemovedMessage;
import com.helospark.tactview.core.timeline.message.EffectResizedMessage;
import com.helospark.tactview.core.util.logger.Slf4j;
import com.helospark.tactview.core.util.messaging.EffectMovedToDifferentClipMessage;
import com.helospark.tactview.core.util.messaging.MessagingService;

@Component
public class TimelineManager implements Saveable {
    private ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    // state
    private List<StatelessVideoEffect> globalEffects;
    private CopyOnWriteArrayList<TimelineChannel> channels = new CopyOnWriteArrayList<>();

    @Slf4j
    private Logger logger;

    // stateless
    private List<EffectFactory> effectFactoryChain;
    private MessagingService messagingService;
    private ClipFactoryChain clipFactoryChain;
    private FrameBufferMerger frameBufferMerger;
    private ObjectMapper objectMapper;
    private AudioBufferMerger audioBufferMerger;
    private ProjectRepository projectRepository;

    public TimelineManager(FrameBufferMerger frameBufferMerger,
            List<EffectFactory> effectFactoryChain, MessagingService messagingService, ClipFactoryChain clipFactoryChain,
            ObjectMapper objectMapper, AudioBufferMerger audioBufferMerger, ProjectRepository projectRepository) {
        this.effectFactoryChain = effectFactoryChain;
        this.messagingService = messagingService;
        this.clipFactoryChain = clipFactoryChain;
        this.frameBufferMerger = frameBufferMerger;
        this.objectMapper = objectMapper;
        this.audioBufferMerger = audioBufferMerger;
        this.projectRepository = projectRepository;
    }

    public boolean canAddClipAt(String channelId, TimelinePosition position, TimelineLength length) {
        if (!findChannelForClipId(channelId).isPresent()) {
            return false;
        }
        TimelineChannel channel = findChannelForClipId(channelId).get();
        return channel.canAddResourceAt(position, length);
    }

    public TimelineClip addResource(AddClipRequest request) {
        String channelId = request.getChannelId();
        TimelineClip clip = clipFactoryChain.createClip(request);
        TimelineChannel channelToAddResourceTo = findChannelWithId(channelId).orElseThrow(() -> new IllegalArgumentException("Channel doesn't exist"));
        addClip(channelToAddResourceTo, clip);

        return clip;
    }

    public void addClip(TimelineChannel channelToAddResourceTo, TimelineClip clip) {
        if (channelToAddResourceTo.canAddResourceAt(clip.getInterval())) {
            channelToAddResourceTo.addResource(clip);
        } else {
            throw new IllegalArgumentException("Cannot add clip");
        }
        List<ValueProviderDescriptor> descriptors = clip.getDescriptors(); // must call before sending clip added message to initialize descriptors
        messagingService.sendMessage(new ClipAddedMessage(clip.getId(), channelToAddResourceTo.getId(), clip.getInterval().getStartPosition(), clip, clip.isResizable(), clip.interval));
        messagingService.sendMessage(new ClipDescriptorsAdded(clip.getId(), descriptors, clip));
        // TODO: keyframes

        for (var effect : clip.getEffects()) {
            messagingService.sendAsyncMessage(new EffectDescriptorsAdded(effect.getId(), effect.getValueProviders(), effect));
            int channelIndex = clip.getEffectWithIndex(effect);
            messagingService.sendMessage(new EffectAddedMessage(effect.getId(), clip.getId(), effect.interval.getStartPosition(), effect, channelIndex, effect.getGlobalInterval()));
        }
    }

    private Optional<TimelineChannel> findChannelWithId(String channelId) {
        return channels.stream()
                .filter(channel -> channel.getId().equals(channelId))
                .findFirst();
    }

    public AudioVideoFragment getFrames(TimelineManagerFramesRequest request) {
        return getSingleFrame(request); // todo: multiple frames
    }

    static class TreeNode {
        private TimelineClip clip;
        private List<TreeNode> children = new ArrayList<>();

        public TreeNode(TimelineClip clip) {
            this.clip = clip;
        }

    }

    static class RenderFrameData {
        double globalAlpha;
        BlendModeStrategy blendModeStrategy;
        ClipFrameResult clipFrameResult;
        String id;
        Optional<AbstractVideoTransitionEffect> videoTransition;

        public RenderFrameData(String id, double globalAlpha, BlendModeStrategy blendModeStrategy, ClipFrameResult clipFrameResult, List<AbstractVideoTransitionEffect> list) {
            this.id = id;
            this.globalAlpha = globalAlpha;
            this.blendModeStrategy = blendModeStrategy;
            this.clipFrameResult = clipFrameResult;
            videoTransition = list.isEmpty() ? Optional.empty() : Optional.ofNullable(list.get(0)); // should multiple transition be handled?
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

    public AudioVideoFragment getSingleFrame(TimelineManagerFramesRequest request) {
        List<TimelineClip> allClips = channels
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
                if (clip instanceof VisualTimelineClip) { // TODO: rest later
                    VisualTimelineClip visualClip = (VisualTimelineClip) clip;

                    futures.add(CompletableFuture.supplyAsync(() -> {
                        Map<String, ClipFrameResult> requiredClips = visualClip.getClipDependency(request.getPosition())
                                .stream()
                                .filter(a -> clipsToFrames.containsKey(a))
                                .map(a -> clipsToFrames.get(a))
                                .collect(Collectors.toMap(a -> a.id, a -> a.clipFrameResult));

                        GetFrameRequest frameRequest = GetFrameRequest.builder()
                                .withScale(request.getScale())
                                .withPosition(request.getPosition())
                                .withExpectedWidth(request.getPreviewWidth())
                                .withExpectedHeight(request.getPreviewHeight())
                                .withApplyEffects(true)
                                .withRequestedClips(requiredClips)
                                .build();

                        ClipFrameResult frameResult = visualClip.getFrame(frameRequest);
                        ClipFrameResult expandedFrame = expandFrame(frameResult, visualClip, request);

                        BlendModeStrategy blendMode = visualClip.getBlendModeAt(request.getPosition());
                        double alpha = visualClip.getAlpha(request.getPosition());

                        GlobalMemoryManagerAccessor.memoryManager.returnBuffer(frameResult.getBuffer());

                        return new RenderFrameData(visualClip.getId(), alpha, blendMode, expandedFrame, clip.getEffectsAtGlobalPosition(request.getPosition(), AbstractVideoTransitionEffect.class));
                    }, executorService).thenAccept(a -> {
                        clipsToFrames.put(visualClip.getId(), a);
                    }).exceptionally(e -> {
                        logger.error("Unable to render", e);
                        return null;
                    }));
                } else if (clip instanceof AudibleTimelineClip) {
                    AudibleTimelineClip audibleClip = (AudibleTimelineClip) clip;

                    futures.add(CompletableFuture.supplyAsync(() -> {
                        AudioRequest audioRequest = AudioRequest.builder()
                                .withApplyEffects(true)
                                .withPosition(request.getPosition())
                                .withLength(new TimelineLength(BigDecimal.valueOf(1).divide(projectRepository.getFps(), 2, RoundingMode.HALF_DOWN)))
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

        ClipFrameResult finalImage = renderVideo(request, renderOrder, clipsToFrames);
        AudioFrameResult audioBuffer = renderAudio(renderOrder, audioToFrames);

        clipsToFrames.values()
                .stream()
                .forEach(a -> GlobalMemoryManagerAccessor.memoryManager.returnBuffer(a.clipFrameResult.getBuffer()));
        audioToFrames.values()
                .stream()
                .flatMap(a -> a.getChannels().stream())
                .forEach(a -> GlobalMemoryManagerAccessor.memoryManager.returnBuffer(a));

        ClipFrameResult finalResult = executeGlobalEffectsOn(finalImage);
        // TODO: audio effects

        return new AudioVideoFragment(finalResult, audioBuffer);
    }

    private AudioFrameResult renderAudio(List<String> renderOrder, Map<String, AudioFrameResult> audioToFrames) {
        List<AudioFrameResult> audioFrames = renderOrder.stream()
                .map(a -> audioToFrames.get(a))
                .filter(a -> a != null)
                .collect(Collectors.toList());

        AudioFrameResult audioBuffer = audioBufferMerger.mergeBuffers(audioFrames);
        return audioBuffer;
    }

    private ClipFrameResult renderVideo(TimelineManagerFramesRequest request, List<String> renderOrder, Map<String, RenderFrameData> clipsToFrames) {
        List<RenderFrameData> frames = renderOrder.stream()
                .map(a -> clipsToFrames.get(a))
                .filter(a -> a != null)
                .collect(Collectors.toList());

        ClipFrameResult finalImage = frameBufferMerger.alphaMergeFrames(frames, request);
        return finalImage;
    }

    private List<TreeNode> buildRenderTree(Map<String, TimelineClip> clipsToRender, TimelinePosition position) {
        List<TreeNode> tree = new ArrayList<>();

        for (var clip : clipsToRender.values()) {
            if (clip.getClipDependency(position).isEmpty()) {
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
        for (TimelineClip clip : clipsToRender.values()) {
            if (clip.getClipDependency(position).contains(dependentClipId)) {
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

    private ClipFrameResult expandFrame(ClipFrameResult frameResult, VisualTimelineClip clip, TimelineManagerFramesRequest request) {
        int previewHeight = request.getPreviewHeight();
        int previewWidth = request.getPreviewWidth();
        TimelinePosition timelinePosition = request.getPosition();
        ByteBuffer outputBuffer = GlobalMemoryManagerAccessor.memoryManager.requestBuffer(previewHeight * previewWidth * 4);
        ByteBuffer inputBuffer = frameResult.getBuffer();

        int requestedXPosition = clip.getXPosition(timelinePosition, request.getScale());
        int requestedYPosition = clip.getYPosition(timelinePosition, request.getScale());

        int destinationStartX = Math.max(requestedXPosition, 0);
        int destinationStartY = Math.max(requestedYPosition, 0);

        int destinationEndX = Math.min(requestedXPosition + frameResult.getWidth(), previewWidth);
        int destinationEndY = Math.min(requestedYPosition + frameResult.getHeight(), previewHeight);

        int sourceX = Math.max(0, -requestedXPosition);
        int sourceY = Math.max(0, -requestedYPosition);

        int width = Math.max(0, destinationEndX - destinationStartX);
        int height = Math.max(0, destinationEndY - destinationStartY);

        int numberOfBytesInARow = width * 4;
        byte[] tmpBuffer = new byte[numberOfBytesInARow];

        int toY = sourceY + height;
        for (int i = sourceY; i < toY; ++i) {
            inputBuffer.position(i * frameResult.getWidth() * 4 + sourceX * 4);
            inputBuffer.get(tmpBuffer, 0, numberOfBytesInARow);

            outputBuffer.position((destinationStartY + i) * previewWidth * 4 + destinationStartX * 4);
            outputBuffer.put(tmpBuffer, 0, numberOfBytesInARow);
        }
        return new ClipFrameResult(outputBuffer, previewWidth, previewHeight);
    }

    private ClipFrameResult executeGlobalEffectsOn(ClipFrameResult finalImage) {
        return finalImage; // todo: do implementation
    }

    public StatelessEffect addEffectForClip(String id, String effectId, TimelinePosition position) {
        TimelineClip clipById = findClipById(id).get();
        StatelessEffect effect = createEffect(effectId, position, clipById);
        addEffectForClip(clipById, effect);
        effect.notifyAfterInitialized();
        return effect;
    }

    public void addEffectForClip(TimelineClip clipById, StatelessEffect effect) {
        int newEffectChannelId = clipById.addEffectAtAnyChannel(effect);
        messagingService.sendAsyncMessage(new EffectDescriptorsAdded(effect.getId(), effect.getValueProviders(), effect));
        messagingService.sendMessage(new EffectAddedMessage(effect.getId(), clipById.getId(), effect.interval.getStartPosition(), effect, newEffectChannelId, effect.getGlobalInterval()));
        // TODO: keyframes
    }

    private StatelessEffect createEffect(String effectId, TimelinePosition position, TimelineClip clipById) {
        CreateEffectRequest request = new CreateEffectRequest(position, effectId, clipById.getType());
        return effectFactoryChain.stream()
                .filter(effectFactory -> effectFactory.doesSupport(request))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No factory for " + effectId))
                .createEffect(request);
    }

    public void removeResource(String clipId) {
        TimelineInterval originalInterval = findClipById(clipId).orElseThrow(() -> new IllegalArgumentException("No such clip")).getGlobalInterval();
        TimelineChannel channel = findChannelForClipId(clipId)
                .orElseThrow(() -> new IllegalArgumentException("No channel contains " + clipId));
        channel.removeClip(clipId);
        messagingService.sendMessage(new ClipRemovedMessage(clipId, originalInterval));
    }

    public Optional<TimelineClip> findClipById(String id) {
        return channels
                .stream()
                .map(channel -> channel.findClipById(id))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

    public Optional<TimelineChannel> findChannelForClipId(String id) {
        return channels
                .stream()
                .filter(channel -> channel.findClipById(id).isPresent())
                .findFirst();
    }

    public TimelineChannel createChannel(int index) {
        TimelineChannel channelToInsert = new TimelineChannel();
        if (index >= 0 && index < channels.size()) {
            channels.add(index, channelToInsert);
        } else {
            channels.add(channelToInsert);
        }
        messagingService.sendAsyncMessage(new ChannelAddedMessage(channelToInsert.getId(), channels.indexOf(channelToInsert)));
        return channelToInsert;
    }

    public void removeChannel(String channelId) {
        boolean success = findChannelForClipId(channelId)
                .map(channelToRemove -> channels.remove(channelToRemove))
                .orElse(false);
        if (success) {
            messagingService.sendAsyncMessage(new ChannelRemovedMessage(channelId));
        }
    }

    public Optional<TimelineClip> findClipForEffect(String effectId) {
        return channels.stream()
                .map(channel -> channel.findClipContainingEffect(effectId))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

    public boolean moveClip(MoveClipRequest moveClipRequest) {
        String clipId = moveClipRequest.clipId;
        TimelinePosition newPosition = moveClipRequest.newPosition;
        String newChannelId = moveClipRequest.newChannelId;

        TimelineChannel originalChannel = findChannelForClipId(clipId).orElseThrow(() -> new IllegalArgumentException("Cannot find clip " + clipId));
        TimelineChannel newChannel = findChannelWithId(newChannelId).orElseThrow(() -> new IllegalArgumentException("Cannot find channel " + newChannelId));

        TimelineClip clipToMove = findClipById(clipId).orElseThrow(() -> new IllegalArgumentException("Cannot find clip"));
        TimelineInterval originalInterval = clipToMove.getGlobalInterval();

        Optional<ClosesIntervalChannel> specialPositionUsed = Optional.empty();

        if (moveClipRequest.enableJumpingToSpecialPosition) {
            TimelineChannel channel = findChannelWithId(newChannelId).orElseThrow();
            TimelineLength clipLength = clipToMove.getInterval().getLength();

            specialPositionUsed = calculateSpecialPositionAround(newPosition, moveClipRequest.maximumJump, clipToMove.getInterval(), clipToMove.getId())
                    .stream()
                    .filter(a -> channel.canAddResourceAtExcluding(new TimelineInterval(a.getClipPosition(), a.getClipPosition().add(clipLength)), clipToMove.getId()))
                    .sorted()
                    .findFirst();
            if (specialPositionUsed.isPresent()) {
                newPosition = specialPositionUsed.get().getClipPosition();
            }
        }

        if (!originalChannel.equals(newChannel)) {
            // todo: some atomity would be nice here
            if (newChannel.canAddResourceAt(clipToMove.getInterval())) {
                originalChannel.removeClip(clipId);
                newChannel.addResource(clipToMove);

                messagingService.sendAsyncMessage(new ClipMovedMessage(clipId, newPosition, newChannelId, specialPositionUsed, originalInterval, clipToMove.getGlobalInterval()));
            }
        } else {
            boolean success = originalChannel.moveClip(clipId, newPosition);

            if (success) {
                messagingService.sendAsyncMessage(new ClipMovedMessage(clipId, newPosition, newChannelId, specialPositionUsed, originalInterval, clipToMove.getGlobalInterval()));
            }
        }
        return true;
    }

    public boolean moveEffect(String effectId, TimelinePosition globalNewPosition, Optional<TimelineLength> maximumJumpToSpecialPositions) {
        TimelineClip currentClip = findClipForEffect(effectId).orElseThrow(() -> new IllegalArgumentException("Clip not found"));
        StatelessEffect effect = currentClip.getEffect(effectId).orElseThrow(() -> new IllegalArgumentException("Effect not found"));
        TimelineInterval interval = effect.getInterval();

        Optional<ClosesIntervalChannel> specialPosition = Optional.empty();
        if (maximumJumpToSpecialPositions.isPresent()) {
            specialPosition = calculateSpecialPositionAround(globalNewPosition, maximumJumpToSpecialPositions.get(), effect.getGlobalInterval(), effectId)
                    .stream()
                    .findFirst();
            globalNewPosition = specialPosition.map(a -> a.getSpecialPosition()).orElse(globalNewPosition);
        }

        int newChannel = currentClip.moveEffect(effect, globalNewPosition);

        EffectMovedMessage message = EffectMovedMessage.builder()
                .withEffectId(effectId)
                .withOriginalClipId(currentClip.getId())
                .withOldPosition(interval.getStartPosition())
                .withNewPosition(effect.getInterval().getStartPosition())
                .withNewChannelIndex(newChannel)
                .withOriginalInterval(interval)
                .withNewInterval(effect.getInterval())
                .withSpecialPositionUsed(specialPosition)
                .build();

        messagingService.sendMessage(message);

        return true;
    }

    public void removeEffect(String effectId) {
        findClipForEffect(effectId)
                .ifPresent(clip -> {
                    StatelessEffect removedElement = clip.removeEffectById(effectId);
                    messagingService.sendAsyncMessage(new EffectRemovedMessage(removedElement.getId(), clip.getId(), removedElement.getGlobalInterval()));
                });
    }

    public Optional<StatelessEffect> findEffectById(String effectId) {
        return findClipForEffect(effectId).flatMap(clip -> clip.getEffect(effectId));
    }

    public void resizeClip(TimelineClip clip, boolean left, TimelinePosition position) {
        TimelineInterval originalInterval = clip.getInterval();
        TimelineChannel channel = findChannelForClipId(clip.getId()).orElseThrow(() -> new IllegalArgumentException("No such channel"));
        boolean success = channel.resizeClip(clip, left, position);
        if (success) {
            TimelineClip renewedClip = findClipById(clip.getId()).orElseThrow(() -> new IllegalArgumentException("No such clip"));
            ClipResizedMessage clipResizedMessage = ClipResizedMessage.builder()
                    .withClipId(clip.getId())
                    .withOriginalInterval(originalInterval)
                    .withNewInterval(renewedClip.getInterval())
                    .build();
            messagingService.sendMessage(clipResizedMessage);
        }
    }

    public void resizeEffect(StatelessEffect effect, boolean left, TimelinePosition globalPosition) {
        TimelineClip clip = findClipForEffect(effect.getId()).orElseThrow(() -> new IllegalArgumentException("No such clip"));
        TimelineInterval originalInterval = clip.getInterval();
        boolean success = clip.resizeEffect(effect, left, globalPosition);

        if (success) {
            StatelessEffect renewedClip = findEffectById(effect.getId()).orElseThrow(() -> new IllegalArgumentException("No such effect"));
            EffectResizedMessage clipResizedMessage = EffectResizedMessage.builder()
                    .withClipId(clip.getId())
                    .withEffectId(renewedClip.getId())
                    .withNewInterval(renewedClip.getInterval())
                    .withOriginalInterval(originalInterval)
                    .build();
            messagingService.sendMessage(clipResizedMessage);
        }
    }

    public void cutClip(String clipId, TimelinePosition globalTimelinePosition) {
        TimelineChannel channel = findChannelForClipId(clipId).orElseThrow(() -> new IllegalArgumentException("No such channel"));
        TimelineClip clip = findClipById(clipId).orElseThrow(() -> new IllegalArgumentException("Cannot find clip"));

        TimelinePosition localPosition = globalTimelinePosition.from(clip.getInterval().getStartPosition());
        List<TimelineClip> cuttedParts = clip.createCutClipParts(localPosition);

        removeResource(clipId);
        addClip(channel, cuttedParts.get(0));
        addClip(channel, cuttedParts.get(1));
    }

    @Override
    public String generateSavedContent() {
        for (TimelineChannel channel : channels) {
            channel.generateSavedContent();
        }
        return null;
    }

    @Override
    public void loadContent(String data, String id, String version) {

    }

    // TODO: some cleanup on below
    public Set<ClosesIntervalChannel> calculateSpecialPositionAround(TimelinePosition position, TimelineLength inRadius, TimelineInterval intervalToAdd, String excludeId) {
        Set<ClosesIntervalChannel> set = new TreeSet<>();
        TimelineLength clipLength = intervalToAdd.getLength();
        TimelinePosition endPosition = position.add(clipLength);
        set.addAll(findSpecialPositionAround(position, inRadius, excludeId));

        set.addAll(findSpecialPositionAround(endPosition, inRadius, excludeId)
                .stream()
                .map(a -> {
                    a.setPosition(a.getClipPosition().subtract(clipLength));
                    return a;
                })
                .collect(Collectors.toList()));

        return set;
    }

    private Set<ClosesIntervalChannel> findSpecialPositionAround(TimelinePosition position, TimelineLength length, String excludeId) {
        return channels.stream()
                .flatMap(channel -> {
                    List<TimelineInterval> spec = channel.findSpecialPositionsAround(position, length, excludeId);
                    return findClosesIntervalForChannel(spec, position, channel, length).stream();
                })
                .collect(Collectors.toCollection(TreeSet::new));
    }

    private Optional<ClosesIntervalChannel> findClosesIntervalForChannel(List<TimelineInterval> findSpecialPositionAround, TimelinePosition position, TimelineChannel channel, TimelineLength length) {
        BigDecimal minimumLength = null;
        TimelinePosition minimumPosition = null;

        for (TimelineInterval interval : findSpecialPositionAround) {
            BigDecimal startLength = interval.getStartPosition().distanceFrom(position);
            if (minimumLength == null || startLength.compareTo(minimumLength) < 0) {
                minimumLength = startLength;
                minimumPosition = interval.getStartPosition();
            }
            BigDecimal endLength = interval.getEndPosition().distanceFrom(position);
            if (minimumLength == null || endLength.compareTo(minimumLength) < 0) {
                minimumLength = endLength;
                minimumPosition = interval.getEndPosition();
            }
        }

        if (minimumLength == null || minimumLength.compareTo(length.getSeconds()) > 0) {
            return Optional.empty();
        } else {
            return Optional.of(new ClosesIntervalChannel(new TimelineLength(minimumLength), channel.getId(), minimumPosition, minimumPosition));
        }
    }

    public List<String> getAllClipIds() {
        return channels.stream()
                .flatMap(channel -> channel.getAllClipId().stream())
                .collect(Collectors.toList());
    }

    public void changeClipForEffect(StatelessEffect originalEffect, String newClipId, TimelinePosition newPosition) {
        TimelineClip originalClip = findClipForEffect(originalEffect.getId()).orElseThrow();
        TimelineClip newClip = findClipById(newClipId).orElseThrow();
        originalClip.removeEffectById(originalEffect.getId());
        newClip.addEffectAtAnyChannel(originalEffect);

        EffectMovedToDifferentClipMessage message = EffectMovedToDifferentClipMessage.builder()
                .withEffectId(originalEffect.getId())
                .withModifiedInterval(originalEffect.getInterval())
                .withNewClipId(newClipId)
                .withOriginalClipId(originalClip.getId())
                .build();

        messagingService.sendAsyncMessage(message);

        moveEffect(originalEffect.getId(), newPosition.add(newClip.getInterval().getStartPosition()), Optional.empty());
    }

}
