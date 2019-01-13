package com.helospark.tactview.core.timeline;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashSet;
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

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.decoder.framecache.GlobalMemoryManagerAccessor;
import com.helospark.tactview.core.repository.ProjectRepository;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.save.SaveLoadContributor;
import com.helospark.tactview.core.timeline.blendmode.BlendModeStrategy;
import com.helospark.tactview.core.timeline.effect.CreateEffectRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.transition.AbstractVideoTransitionEffect;
import com.helospark.tactview.core.timeline.framemerge.FrameBufferMerger;
import com.helospark.tactview.core.timeline.framemerge.RenderFrameData;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.timeline.message.ChannelAddedMessage;
import com.helospark.tactview.core.timeline.message.ChannelRemovedMessage;
import com.helospark.tactview.core.timeline.message.ChannelSettingUpdatedMessage;
import com.helospark.tactview.core.timeline.message.ClipAddedMessage;
import com.helospark.tactview.core.timeline.message.ClipDescriptorsAdded;
import com.helospark.tactview.core.timeline.message.ClipMovedMessage;
import com.helospark.tactview.core.timeline.message.ClipRemovedMessage;
import com.helospark.tactview.core.timeline.message.ClipResizedMessage;
import com.helospark.tactview.core.timeline.message.EffectAddedMessage;
import com.helospark.tactview.core.timeline.message.EffectChannelChangedMessage;
import com.helospark.tactview.core.timeline.message.EffectDescriptorsAdded;
import com.helospark.tactview.core.timeline.message.EffectMovedMessage;
import com.helospark.tactview.core.timeline.message.EffectRemovedMessage;
import com.helospark.tactview.core.timeline.message.EffectResizedMessage;
import com.helospark.tactview.core.util.logger.Slf4j;
import com.helospark.tactview.core.util.messaging.EffectMovedToDifferentClipMessage;
import com.helospark.tactview.core.util.messaging.MessagingService;

@Component
public class TimelineManager implements SaveLoadContributor {
    private ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private Object fullLock = new Object();

    // state
    private List<StatelessVideoEffect> globalEffects;
    private CopyOnWriteArrayList<TimelineChannel> channels = new CopyOnWriteArrayList<>();

    @Slf4j
    private Logger logger;

    // stateless
    private MessagingService messagingService;
    private ClipFactoryChain clipFactoryChain;
    private FrameBufferMerger frameBufferMerger;
    private AudioBufferMerger audioBufferMerger;
    private ProjectRepository projectRepository;
    private EffectFactoryChain effectFactoryChain;
    private LinkClipRepository linkClipRepository;

    public TimelineManager(FrameBufferMerger frameBufferMerger,
            EffectFactoryChain effectFactoryChain, MessagingService messagingService, ClipFactoryChain clipFactoryChain,
            AudioBufferMerger audioBufferMerger, ProjectRepository projectRepository, LinkClipRepository linkClipRepository) {
        this.effectFactoryChain = effectFactoryChain;
        this.messagingService = messagingService;
        this.clipFactoryChain = clipFactoryChain;
        this.frameBufferMerger = frameBufferMerger;
        this.audioBufferMerger = audioBufferMerger;
        this.projectRepository = projectRepository;
        this.linkClipRepository = linkClipRepository;
    }

    public TimelineClip addClip(AddClipRequest request) {
        String channelId = request.getChannelId();
        List<TimelineClip> clips = clipFactoryChain.createClips(request);

        Integer channelIndex = findChannelIndex(channelId).orElseThrow(() -> new IllegalArgumentException("Channel doesn't exist"));
        for (var clip : clips) {
            linkClipRepository.linkClips(clip.getId(), clips);
            if (channelIndex >= channels.size()) {
                createChannel(channelIndex);
            }
            if (!channels.get(channelIndex).canAddResourceAt(clip.getInterval().getStartPosition(), clip.getInterval().getLength())) {
                createChannel(channelIndex);
            }
            TimelineChannel channelToAddResourceTo = channels.get(channelIndex);
            addClip(channelToAddResourceTo, clip);
            ++channelIndex;
        }
        return clips.get(0);
    }

    public void addClip(TimelineChannel channelToAddResourceTo, TimelineClip clip) {
        synchronized (channelToAddResourceTo.getFullChannelLock()) {
            if (channelToAddResourceTo.canAddResourceAt(clip.getInterval())) {
                channelToAddResourceTo.addResource(clip);
            } else {
                throw new IllegalArgumentException("Cannot add clip");
            }
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
                        Map<String, ReadOnlyClipImage> requiredClips = visualClip.getClipDependency(request.getPosition())
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

                        ReadOnlyClipImage frameResult = visualClip.getFrame(frameRequest);
                        ReadOnlyClipImage expandedFrame = expandFrame(frameResult, visualClip, request);

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
                } else if (clip instanceof AudibleTimelineClip && request.isNeedSound()) {
                    AudibleTimelineClip audibleClip = (AudibleTimelineClip) clip;

                    futures.add(CompletableFuture.supplyAsync(() -> {
                        AudioRequest audioRequest = AudioRequest.builder()
                                .withApplyEffects(true)
                                .withPosition(request.getPosition())
                                .withLength(new TimelineLength(BigDecimal.valueOf(1).divide(projectRepository.getFps(), 100, RoundingMode.HALF_DOWN)))
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

    private AudioFrameResult renderAudio(List<String> renderOrder, Map<String, AudioFrameResult> audioToFrames) {
        List<AudioFrameResult> audioFrames = renderOrder.stream()
                .filter(clipId -> {
                    TimelineChannel channelContainingCurrentClip = findChannelForClipId(clipId).get();
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
                    TimelineChannel channelContainingCurrentClip = findChannelForClipId(clipId).get();
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

    public ClipImage expandFrame(ReadOnlyClipImage frameResult, VisualTimelineClip clip, TimelineManagerFramesRequest request) {
        int previewHeight = request.getPreviewHeight();
        int previewWidth = request.getPreviewWidth();
        TimelinePosition timelinePosition = request.getPosition();
        ByteBuffer outputBuffer = GlobalMemoryManagerAccessor.memoryManager.requestBuffer(previewHeight * previewWidth * 4);
        ByteBuffer inputBuffer = frameResult.getBuffer();

        int anchorOffsetX = clip.getHorizontalAlignment(timelinePosition).apply(frameResult.getWidth(), previewWidth);
        int anchorOffsetY = clip.getVerticalAlignment(timelinePosition).apply(frameResult.getHeight(), previewHeight);

        double scale = request.getScale();

        int requestedXPosition = anchorOffsetX + clip.getXPosition(timelinePosition, scale);
        int requestedYPosition = anchorOffsetY + clip.getYPosition(timelinePosition, scale);

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
        return new ClipImage(outputBuffer, previewWidth, previewHeight);
    }

    private ReadOnlyClipImage executeGlobalEffectsOn(ReadOnlyClipImage finalImage) {
        return finalImage; // todo: do implementation
    }

    public StatelessEffect addEffectForClip(String id, String effectId, TimelinePosition position) {
        TimelineClip clipById = findClipById(id).get();
        StatelessEffect effect = createEffect(effectId, position, clipById);
        addEffectForClip(clipById, effect);
        return effect;
    }

    public void addEffectForClip(TimelineClip clipById, StatelessEffect effect) {
        int newEffectChannelId = clipById.addEffectAtAnyChannel(effect);
        messagingService.sendAsyncMessage(new EffectDescriptorsAdded(effect.getId(), effect.getValueProviders(), effect));
        messagingService.sendMessage(new EffectAddedMessage(effect.getId(), clipById.getId(), effect.interval.getStartPosition(), effect, newEffectChannelId, effect.getGlobalInterval()));
        effect.notifyAfterInitialized();
        // TODO: keyframes
    }

    private StatelessEffect createEffect(String effectId, TimelinePosition position, TimelineClip clipById) {
        CreateEffectRequest request = new CreateEffectRequest(position, effectId, clipById.getType());
        return effectFactoryChain.createEffect(request);
    }

    public void removeClip(String clipId) {
        TimelineClip originalClip = findClipById(clipId).orElseThrow(() -> new IllegalArgumentException("No such clip"));
        TimelineInterval originalInterval = originalClip.getGlobalInterval();

        synchronized (originalClip.getFullClipLock()) {
            originalClip.getEffects()
                    .stream()
                    .forEach(effect -> removeEffect(originalClip, effect.getId()));

            TimelineChannel channel = findChannelForClipId(clipId)
                    .orElseThrow(() -> new IllegalArgumentException("No channel contains " + clipId));
            channel.removeClip(clipId);
        }
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
        createChannel(index, channelToInsert);
        return channelToInsert;
    }

    private void createChannel(int index, TimelineChannel channelToInsert) {
        synchronized (fullLock) {
            if (index >= 0 && index < channels.size()) {
                channels.add(index, channelToInsert);
            } else {
                channels.add(channelToInsert);
            }
        }
        messagingService.sendMessage(new ChannelAddedMessage(channelToInsert.getId(), channels.indexOf(channelToInsert), channelToInsert.isDisabled(), channelToInsert.isMute()));
    }

    public void removeChannel(String channelId) {
        synchronized (fullLock) {
            TimelineChannel channel = findChannelWithId(channelId).orElseThrow();

            channel.getAllClipId()
                    .stream()
                    .forEach(clipId -> removeClip(clipId));

            findChannelIndex(channelId)
                    .ifPresent(index -> {
                        channels.remove(index.intValue());
                        messagingService.sendMessage(new ChannelRemovedMessage(channelId));
                    });
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

        TimelineClip clipToMove = findClipById(clipId).orElseThrow(() -> new IllegalArgumentException("Cannot find clip"));
        TimelineInterval originalInterval = clipToMove.getGlobalInterval();

        Set<String> linkedClipIds = new HashSet<>(linkClipRepository.getLinkedClips(clipId));
        linkedClipIds.add(clipToMove.getId());

        for (String additionalClipId : moveClipRequest.additionalClipIds) {
            linkedClipIds.addAll(linkClipRepository.getLinkedClips(additionalClipId));
            linkedClipIds.add(additionalClipId);
        }

        List<TimelineClip> linkedClips = linkedClipIds
                .stream()
                .map(a -> findClipById(a))
                .filter(a -> a.isPresent())
                .map(a -> a.get())
                .collect(Collectors.toList());

        Optional<ClosesIntervalChannel> specialPositionUsed = Optional.empty();

        if (moveClipRequest.enableJumpingToSpecialPosition) {
            List<String> ignoredIds = new ArrayList<>();
            ignoredIds.add(clipToMove.getId());
            ignoredIds.addAll(linkedClipIds);
            clipToMove.getEffects()
                    .stream()
                    .map(effect -> effect.getId())
                    .forEach(effectId -> ignoredIds.add(effectId));

            specialPositionUsed = calculateSpecialPositionAround(newPosition, moveClipRequest.maximumJump, clipToMove.getInterval(), ignoredIds)
                    .stream()
                    .filter(a -> {
                        TimelinePosition relativeMove = originalInterval.getStartPosition().subtract(a.getClipPosition());
                        boolean allClipsCanBePlaced = allLinkedClipsCanBeMoved(linkedClips, relativeMove);
                        return allClipsCanBePlaced;
                    })
                    .sorted()
                    .findFirst();
            if (specialPositionUsed.isPresent()) {
                newPosition = specialPositionUsed.get().getClipPosition();
            }
        }

        TimelinePosition relativeMove = newPosition.subtract(originalInterval.getStartPosition());
        int relativeChannelMove = findChannelIndex(newChannelId).orElseThrow() - findChannelIndex(originalChannel.getId()).orElseThrow();
        Optional<ClosesIntervalChannel> finalSpecialPositionUsed = specialPositionUsed;

        synchronized (fullLock) {
            if (!originalChannel.getId().equals(newChannelId)) {

                boolean canMove = linkedClips.stream()
                        .allMatch(clip -> {
                            int currentIndex = findChannelIndexForClipId(clip.getId()).get() + relativeChannelMove;

                            if (currentIndex < channels.size()) {
                                TimelineChannel movedChannel = channels.get(currentIndex);
                                TimelineInterval clipCurrentInterval = clip.getInterval();
                                TimelineInterval clipNewInterval = clipCurrentInterval.butAddOffset(relativeMove);
                                return movedChannel.canAddResourceAtExcluding(clipNewInterval, linkedClipIds);
                            } else {
                                return true;
                            }
                        });

                if (canMove) {
                    linkedClips.stream()
                            .forEach(clip -> {
                                int currentIndex = findChannelIndexForClipId(clip.getId()).get() + relativeChannelMove;

                                for (int i = channels.size(); i < currentIndex; ++i) {
                                    createChannel(i);
                                }
                            });

                    linkedClips.stream()
                            .forEach(clip -> {
                                int originalIndex = findChannelIndexForClipId(clip.getId()).get();
                                int currentIndex = originalIndex + relativeChannelMove;

                                TimelineChannel originalMovedChannel = channels.get(originalIndex);
                                TimelineChannel newMovedChannel = channels.get(currentIndex);

                                TimelineInterval clipCurrentInterval = clip.getInterval();
                                TimelineInterval clipNewPosition = clipCurrentInterval.butAddOffset(relativeMove);

                                originalMovedChannel.removeClip(clip.getId());
                                clip.setInterval(clipNewPosition);
                                newMovedChannel.addResource(clip);

                                messagingService.sendAsyncMessage(new ClipMovedMessage(clip.getId(), clipNewPosition.getStartPosition(), newMovedChannel.getId(), finalSpecialPositionUsed,
                                        clipCurrentInterval, clip.getGlobalInterval()));
                            });
                }

            } else {
                boolean canAddResource = linkedClips
                        .stream()
                        .allMatch(clip -> {
                            TimelineChannel channel = findChannelForClipId(clip.getId()).orElseThrow();
                            return channel.canAddResourceAtExcluding(clip.getInterval().butAddOffset(relativeMove), clip.getId());
                        });

                if (canAddResource) {
                    linkedClips
                            .stream()
                            .forEach(clip -> {
                                TimelineChannel channel = findChannelForClipId(clip.getId()).orElseThrow();
                                TimelineInterval clipCurrentInterval = clip.getInterval();
                                TimelinePosition clipNewPosition = clipCurrentInterval.getStartPosition().add(relativeMove);
                                channel.moveClip(clip.getId(), clipNewPosition);
                                messagingService.sendAsyncMessage(
                                        new ClipMovedMessage(clip.getId(), clipNewPosition, channel.getId(), finalSpecialPositionUsed, clipCurrentInterval, clip.getGlobalInterval()));
                            });

                }

            }
        }
        return true;
    }

    private boolean allLinkedClipsCanBeMoved(List<TimelineClip> linkedClips, TimelinePosition relativeMove) {
        boolean clipItemMatch = linkedClips.stream()
                .allMatch(clip -> {
                    TimelineChannel currentChannel = findChannelForClipId(clip.getId()).orElseThrow();
                    TimelineInterval newClipInterval = clip.getInterval().butAddOffset(relativeMove);
                    return currentChannel.canAddResourceAtExcluding(newClipInterval, clip.getId());
                });

        return clipItemMatch;
    }

    public boolean moveEffect(String effectId, TimelinePosition globalNewPosition, Optional<TimelineLength> maximumJumpToSpecialPositions) {
        TimelineClip currentClip = findClipForEffect(effectId).orElseThrow(() -> new IllegalArgumentException("Clip not found"));
        StatelessEffect effect = currentClip.getEffect(effectId).orElseThrow(() -> new IllegalArgumentException("Effect not found"));
        TimelineInterval interval = effect.getInterval();

        Optional<ClosesIntervalChannel> specialPosition = Optional.empty();
        if (maximumJumpToSpecialPositions.isPresent()) {
            specialPosition = calculateSpecialPositionAround(globalNewPosition, maximumJumpToSpecialPositions.get(), effect.getGlobalInterval(), List.of(effectId))
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
                    removeEffect(clip, effectId);
                });
    }

    private void removeEffect(TimelineClip clip, String effectId) {
        StatelessEffect removedElement = clip.removeEffectById(effectId);
        messagingService.sendAsyncMessage(new EffectRemovedMessage(removedElement.getId(), clip.getId(), removedElement.getGlobalInterval()));
    }

    public Optional<StatelessEffect> findEffectById(String effectId) {
        return findClipForEffect(effectId).flatMap(clip -> clip.getEffect(effectId));
    }

    public void resizeClip(ResizeClipRequest resizeEffectRequest) {
        TimelineClip clip = resizeEffectRequest.getClip();
        boolean left = resizeEffectRequest.isLeft();
        TimelinePosition globalPosition = resizeEffectRequest.getPosition();
        boolean useSpecialPoints = resizeEffectRequest.isUseSpecialPoints();

        TimelineInterval originalInterval = clip.getInterval();
        TimelineChannel channel = findChannelForClipId(clip.getId()).orElseThrow(() -> new IllegalArgumentException("No such channel"));

        Optional<ClosesIntervalChannel> specialPointUsed = Optional.empty();
        if (useSpecialPoints) {
            List<String> excludedIds = new ArrayList<>();
            excludedIds.add(clip.getId());
            clip.getEffects()
                    .stream()
                    .map(effect -> effect.getId())
                    .forEach(effectId -> excludedIds.add(effectId));

            specialPointUsed = findSpecialPositionAround(globalPosition, resizeEffectRequest.getMaximumJumpLength(), excludedIds)
                    .stream()
                    .findFirst();
            if (specialPointUsed.isPresent()) {
                globalPosition = specialPointUsed.get().getSpecialPosition();
            }
        }

        boolean success = channel.resizeClip(clip, left, globalPosition);
        if (success) {
            TimelineClip renewedClip = findClipById(clip.getId()).orElseThrow(() -> new IllegalArgumentException("No such clip"));
            ClipResizedMessage clipResizedMessage = ClipResizedMessage.builder()
                    .withClipId(clip.getId())
                    .withOriginalInterval(originalInterval)
                    .withNewInterval(renewedClip.getInterval())
                    .withSpecialPointUsed(specialPointUsed)
                    .build();
            messagingService.sendMessage(clipResizedMessage);
        }
    }

    public void resizeEffect(ResizeEffectRequest resizeEffectRequest) {
        StatelessEffect effect = resizeEffectRequest.getEffect();
        boolean left = resizeEffectRequest.isLeft();
        TimelinePosition globalPosition = resizeEffectRequest.getGlobalPosition();
        boolean useSpecialPoints = resizeEffectRequest.isUseSpecialPoints();

        TimelineClip clip = findClipForEffect(effect.getId()).orElseThrow(() -> new IllegalArgumentException("No such clip"));
        TimelineInterval originalInterval = clip.getInterval();

        TimelinePosition newPosition = globalPosition;

        Optional<ClosesIntervalChannel> specialPointUsed = Optional.empty();
        if (useSpecialPoints) {
            specialPointUsed = findSpecialPositionAround(globalPosition, resizeEffectRequest.getMaximumJumpLength(), List.of(effect.getId()))
                    .stream()
                    .findFirst();
            if (specialPointUsed.isPresent()) {
                newPosition = specialPointUsed.get().getSpecialPosition();
            }
        }

        boolean success = clip.resizeEffect(effect, left, newPosition);

        if (success) {
            StatelessEffect renewedClip = findEffectById(effect.getId()).orElseThrow(() -> new IllegalArgumentException("No such effect"));
            EffectResizedMessage clipResizedMessage = EffectResizedMessage.builder()
                    .withClipId(clip.getId())
                    .withEffectId(renewedClip.getId())
                    .withNewInterval(renewedClip.getInterval())
                    .withOriginalInterval(originalInterval)
                    .withSpecialPositionUsed(specialPointUsed)
                    .build();
            messagingService.sendMessage(clipResizedMessage);
        }
    }

    public List<TimelineClip> cutClip(String clipId, TimelinePosition globalTimelinePosition) {
        TimelineChannel channel = findChannelForClipId(clipId).orElseThrow(() -> new IllegalArgumentException("No such channel"));
        TimelineClip clip = findClipById(clipId).orElseThrow(() -> new IllegalArgumentException("Cannot find clip"));

        synchronized (channel.getFullChannelLock()) {
            List<TimelineClip> cuttedParts = clip.createCutClipParts(globalTimelinePosition);

            removeClip(clipId);
            addClip(channel, cuttedParts.get(0));
            addClip(channel, cuttedParts.get(1));
            return cuttedParts;
        }
    }

    @Override
    public void generateSavedContent(Map<String, Object> generatedContent) {
        List<Object> channelContent = new ArrayList<>();
        for (TimelineChannel channel : channels) {
            channelContent.add(channel.generateSavedContent());
        }
        generatedContent.put("channels", channelContent);
    }

    @Override
    public void loadFrom(JsonNode tree, LoadMetadata loadMetadata) {
        for (var channelToRemove : channels) {
            removeChannel(channelToRemove.getId());
        }
        JsonNode savedChannels = tree.get("channels");

        int i = 0;
        for (var savedChannel : savedChannels) {
            TimelineChannel createdChannel = new TimelineChannel(savedChannel);
            createChannel(i++, createdChannel);

            JsonNode clips = savedChannel.get("clips");
            for (var savedClip : clips) {
                TimelineClip restoredClip = clipFactoryChain.restoreClip(savedClip, loadMetadata);

                JsonNode savedEffectChannels = savedClip.get("effectChannels");
                int channelId = 0;
                for (var savedEffectChannel : savedEffectChannels) {
                    for (var savedEffect : savedEffectChannel) {
                        StatelessEffect restoredEffect = effectFactoryChain.restoreEffect(savedEffect, loadMetadata);
                        restoredClip.addEffectAtChannel(channelId, restoredEffect);
                    }
                    ++channelId;
                }
                addClip(createdChannel, restoredClip);
            }
        }

    }

    // TODO: some cleanup on below
    public Set<ClosesIntervalChannel> calculateSpecialPositionAround(TimelinePosition position, TimelineLength inRadius, TimelineInterval intervalToAdd, List<String> ignoredIds) {
        Set<ClosesIntervalChannel> set = new TreeSet<>();
        TimelineLength clipLength = intervalToAdd.getLength();
        TimelinePosition endPosition = position.add(clipLength);
        set.addAll(findSpecialPositionAround(position, inRadius, ignoredIds));

        set.addAll(findSpecialPositionAround(endPosition, inRadius, ignoredIds)
                .stream()
                .map(a -> {
                    a.setPosition(a.getClipPosition().subtract(clipLength));
                    return a;
                })
                .collect(Collectors.toList()));

        return set;
    }

    private Set<ClosesIntervalChannel> findSpecialPositionAround(TimelinePosition position, TimelineLength length, List<String> ignoredIds) {
        return channels.stream()
                .flatMap(channel -> {
                    List<TimelineInterval> spec = channel.findSpecialPositionsAround(position, length, ignoredIds);
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
        synchronized (fullLock) {
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

    public Optional<Integer> findChannelIndexForClipId(String clipId) {
        return findChannelForClipId(clipId)
                .flatMap(a -> findChannelIndex(a.getId()));
    }

    private Optional<Integer> findChannelIndex(String channelId) {
        for (int i = 0; i < channels.size(); ++i) {
            if (channels.get(i).getId().equals(channelId)) {
                return Optional.of(i);
            }
        }
        return Optional.empty();
    }

    public void addExistingClip(AddExistingClipRequest request) {
        TimelineChannel channel = request.getChannel();
        TimelineClip clip = request.getClipToAdd();
        TimelinePosition position = channel.findPositionWhereIntervalWithLengthCanBeInserted(clip.getInterval().getLength());
        clip.setInterval(clip.getInterval().butMoveStartPostionTo(position));
        addClip(request.getChannel(), clip);
    }

    public void addExistingEffect(AddExistingEffectRequest request) {
        addEffectForClip(request.getClipToAdd(), request.getEffect());
    }

    public TimelinePosition findEndPosition() {
        TimelinePosition endPosition = TimelinePosition.ofZero();
        for (var channel : channels) {
            TimelinePosition channelEndPosition = channel.findMaximumEndPosition();
            if (channelEndPosition.isGreaterThan(endPosition)) {
                endPosition = channelEndPosition;
            }
        }
        return endPosition;
    }

    public void moveEffectToChannel(TimelineClip clip, String effectId, int newChannelIndex) {
        StatelessEffect effect = clip.getEffect(effectId).get();
        Integer currentIndex = clip.getEffectChannelIndex(effectId).get();
        int moveDirection = (currentIndex < newChannelIndex ? 1 : 0);
        synchronized (clip.getFullClipLock()) {
            clip.removeEffectById(effectId);
            if (clip.canAddEffectAt(newChannelIndex, effect.getInterval())) {
                NonIntersectingIntervalList<StatelessEffect> channel = clip.getChannelByIndex(newChannelIndex).get();
                channel.addInterval(effect);
                messagingService.sendAsyncMessage(new EffectChannelChangedMessage(effect.getId(), newChannelIndex, effect.getInterval()));
            } else {
                NonIntersectingIntervalList<StatelessEffect> newChannel = clip.addEffectChannel(newChannelIndex + moveDirection);
                newChannel.addInterval(effect);
                for (int i = 0; i < clip.getEffectChannels().size(); ++i) {
                    if (clip.getEffectChannels().get(i).size() == 0) {
                        clip.getEffectChannels().remove(i);
                        --i;
                    }
                }
                for (int i = 0; i < clip.getEffectChannels().size(); ++i) {
                    for (var newEffect : clip.getEffectChannels().get(i)) {
                        messagingService.sendAsyncMessage(new EffectChannelChangedMessage(newEffect.getId(), i, newEffect.getInterval()));
                    }
                }
            }
        }
    }

    public int findEffectChannel(String id) {
        return findClipForEffect(id)
                .flatMap(a -> a.getEffectChannelIndex(id))
                .orElse(-1);
    }

    public int getNumberOfEffectChannels(String id) {
        return findClipForEffect(id)
                .map(a -> a.getEffectChannels().size())
                .orElse(-1);
    }

    public boolean muteChannel(String channelId, boolean isMute) {
        TimelineChannel channel = findChannelWithId(channelId).get();
        if (channel.isMute() != isMute) {
            channel.setMute(isMute);
            messagingService.sendAsyncMessage(new ChannelSettingUpdatedMessage(new TimelineInterval(TimelinePosition.ofZero(), channel.findMaximumEndPosition())));
            return true;
        }
        return false;
    }

    public boolean disableChannel(String channelId, boolean isDisable) {
        TimelineChannel channel = findChannelWithId(channelId).get();
        if (channel.isDisabled() != isDisable) {
            channel.setDisabled(isDisable);
            messagingService.sendAsyncMessage(new ChannelSettingUpdatedMessage(new TimelineInterval(TimelinePosition.ofZero(), channel.findMaximumEndPosition())));
            return true;
        }
        return false;
    }

    public List<String> findIntersectingClips(TimelinePosition currentPosition) {
        return channels
                .stream()
                .map(channel -> channel.getDataAt(currentPosition))
                .flatMap(Optional::stream)
                .map(clip -> clip.getId())
                .collect(Collectors.toList());
    }

    public CopyOnWriteArrayList<TimelineChannel> getChannels() {
        return channels;
    }

}
