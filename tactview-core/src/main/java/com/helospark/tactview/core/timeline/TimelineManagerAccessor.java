package com.helospark.tactview.core.timeline;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.repository.ProjectRepository;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.save.SaveLoadContributor;
import com.helospark.tactview.core.save.SaveMetadata;
import com.helospark.tactview.core.timeline.effect.CreateEffectRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.longprocess.LongProcessRequestor;
import com.helospark.tactview.core.timeline.message.AbstractKeyframeChangedMessage;
import com.helospark.tactview.core.timeline.message.ChannelAddedMessage;
import com.helospark.tactview.core.timeline.message.ChannelMovedMessage;
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
public class TimelineManagerAccessor implements SaveLoadContributor, TimelineManagerAccessorInterface {

    private static final BigDecimal TWO = BigDecimal.valueOf(2L);

    @Slf4j
    private Logger logger;

    // stateless
    private final MessagingService messagingService;
    private final ClipFactoryChain clipFactoryChain;
    private final EffectFactoryChain effectFactoryChain;
    private final LinkClipRepository linkClipRepository;
    private final TimelineChannelsState timelineChannelsState;
    private final LongProcessRequestor longProcessRequestor;
    private final ProjectRepository projectRepository;

    public TimelineManagerAccessor(MessagingService messagingService, ClipFactoryChain clipFactoryChain, EffectFactoryChain effectFactoryChain, LinkClipRepository linkClipRepository,
            TimelineChannelsState timelineChannelsState, LongProcessRequestor longProcessRequestor, ProjectRepository projectRepository) {
        this.messagingService = messagingService;
        this.clipFactoryChain = clipFactoryChain;
        this.effectFactoryChain = effectFactoryChain;
        this.linkClipRepository = linkClipRepository;
        this.timelineChannelsState = timelineChannelsState;
        this.longProcessRequestor = longProcessRequestor;
        this.projectRepository = projectRepository;
    }

    @PostConstruct
    public void postConstruct() {
        longProcessRequestor.setTimelineManagerAccessor(this);

        messagingService.register(AbstractKeyframeChangedMessage.class, message -> {
            createNewChannelWhenClipLengthChangeCausesIntersectingClips(message);
        });
    }

    private void createNewChannelWhenClipLengthChangeCausesIntersectingClips(AbstractKeyframeChangedMessage message) {
        TimelineClip affectedClip = null;
        TimelineChannel affectedChannel = null;
        for (var channel : getChannels()) {
            for (var clip : channel.getAllClips()) {
                if (message.getDescriptorId().equals(clip.getTimeScaleProviderId())) {
                    affectedClip = clip;
                    affectedChannel = channel;
                    break;
                }
                if (affectedClip != null) {
                    break;
                }
            }
        }
        if (affectedClip != null && affectedChannel.areIntervalsIntersecting()) {
            Integer channelIndex = findChannelIndex(affectedChannel.getId()).get();
            TimelineChannel newChannel = createChannel(channelIndex);

            MoveClipRequest moveClipRequest = MoveClipRequest.builder()
                    .withAdditionalClipIds(List.of())
                    .withClipId(affectedClip.getId())
                    .withEnableJumpingToSpecialPosition(false)
                    .withMoreMoveExpected(false)
                    .withNewChannelId(newChannel.getId())
                    .withNewPosition(affectedClip.getInterval().getStartPosition())
                    .build();

            moveClip(moveClipRequest);
        }
    }

    public TimelineClip addClip(AddClipRequest request) {
        String channelId = request.getChannelId();
        List<TimelineClip> clips = clipFactoryChain.createClips(request);

        Integer channelIndex = findChannelIndex(channelId).orElseThrow(() -> new IllegalArgumentException("Channel doesn't exist"));
        for (var clip : clips) {
            linkClipRepository.linkClips(clip.getId(), clips);
            if (channelIndex >= timelineChannelsState.channels.size()) {
                createChannel(channelIndex);
            }
            if (!timelineChannelsState.channels.get(channelIndex).canAddResourceAt(clip.getInterval().getStartPosition(), clip.getInterval().getLength())) {
                createChannel(channelIndex);
            }
            TimelineChannel channelToAddResourceTo = timelineChannelsState.channels.get(channelIndex);
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
                List<TimelineInterval> intersectingIntervals = channelToAddResourceTo.getAllClips()
                        .computeIntersectingIntervals(clip.getInterval())
                        .stream()
                        .map(a -> a.getInterval())
                        .collect(Collectors.toList());
                throw new IllegalArgumentException("Cannot add clip with interval {} " + clip.getInterval() + " because it intesects with " + intersectingIntervals);
            }
        }
        sendClipAndEffectMessages(channelToAddResourceTo, clip);

    }

    private void sendClipAndEffectMessages(TimelineChannel channelToAddResourceTo, TimelineClip clip) {
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

    public Optional<TimelineChannel> findChannelWithId(String channelId) {
        return timelineChannelsState.channels.stream()
                .filter(channel -> channel.getId().equals(channelId))
                .findFirst();
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
    }

    private StatelessEffect createEffect(String effectId, TimelinePosition position, TimelineClip clipById) {
        CreateEffectRequest request = new CreateEffectRequest(position, effectId, clipById.getType(), clipById.getInterval());
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

    @Override
    public Optional<TimelineClip> findClipById(String id) {
        return timelineChannelsState.channels
                .stream()
                .map(channel -> channel.findClipById(id))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

    public Optional<TimelineChannel> findChannelForClipId(String id) {
        return timelineChannelsState.channels
                .stream()
                .filter(channel -> channel.findClipById(id).isPresent())
                .findFirst();
    }

    public TimelineChannel createChannel(int index) {
        TimelineChannel channelToInsert = new TimelineChannel();
        createChannel(index, channelToInsert);
        return channelToInsert;
    }

    public void createChannel(int index, TimelineChannel channelToInsert) {
        synchronized (timelineChannelsState.fullLock) {
            if (index >= 0 && index < timelineChannelsState.channels.size()) {
                timelineChannelsState.channels.add(index, channelToInsert);
            } else {
                timelineChannelsState.channels.add(channelToInsert);
            }
        }
        messagingService.sendMessage(new ChannelAddedMessage(channelToInsert.getId(), timelineChannelsState.channels.indexOf(channelToInsert), channelToInsert.isDisabled(), channelToInsert.isMute()));

        channelToInsert.getAllClipId()
                .stream()
                .flatMap(clipId -> channelToInsert.findClipById(clipId).stream())
                .forEach(clip -> sendClipAndEffectMessages(channelToInsert, clip));
    }

    public TimelineChannel removeChannel(String channelId) {
        synchronized (timelineChannelsState.fullLock) {
            TimelineChannel channel = findChannelWithId(channelId).orElseThrow();

            channel.getAllClipId()
                    .stream()
                    .forEach(clipId -> removeClip(clipId));

            findChannelIndex(channelId)
                    .ifPresent(index -> {
                        timelineChannelsState.channels.remove(index.intValue());
                        messagingService.sendMessage(new ChannelRemovedMessage(channelId));
                    });
            return channel;
        }
    }

    @Override
    public Optional<TimelineClip> findClipForEffect(String effectId) {
        return timelineChannelsState.channels.stream()
                .map(channel -> channel.findClipContainingEffect(effectId))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

    public boolean moveClip(MoveClipRequest moveClipRequest) {
        String clipId = moveClipRequest.clipId;
        TimelinePosition newPosition = moveClipRequest.newPosition;
        String newChannelId = moveClipRequest.newChannelId;

        if (moveClipRequest.enableJumpingToSpecialPosition) { // maybe another flag could be used for frame based jumping
            newPosition = getNewPositionBasedOnFPS(newPosition);
        }

        TimelineChannel originalChannel = findChannelForClipId(clipId).orElseThrow(() -> new IllegalArgumentException("Cannot find clip " + clipId));

        TimelineClip clipToMove = findClipById(clipId).orElseThrow(() -> new IllegalArgumentException("Cannot find clip"));
        TimelineInterval originalInterval = clipToMove.getInterval();

        Set<String> linkedClipIds = fillWithAllTransitiveLinkedClips(moveClipRequest);

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

            synchronized (timelineChannelsState.fullLock) { // allLinkedClipsCanBeMoved requires state modification
                specialPositionUsed = calculateSpecialPositionAround(newPosition, moveClipRequest.maximumJump, clipToMove.getInterval(), ignoredIds, moveClipRequest.additionalSpecialPositions)
                        .stream()
                        .filter(a -> {
                            TimelinePosition relativeMove = originalInterval.getStartPosition().subtract(a.getClipPosition());
                            boolean allClipsCanBePlaced = allLinkedClipsCanBeMoved(linkedClips, relativeMove);
                            return allClipsCanBePlaced;
                        })
                        .sorted()
                        .findFirst();
            }
            if (specialPositionUsed.isPresent()) {
                newPosition = specialPositionUsed.get().getClipPosition();
            }
        }

        TimelinePosition relativeMove = newPosition.subtract(originalInterval.getStartPosition());
        int relativeChannelMove = findChannelIndex(newChannelId).orElseThrow() - findChannelIndex(originalChannel.getId()).orElseThrow();
        Optional<ClosesIntervalChannel> finalSpecialPositionUsed = specialPositionUsed;

        synchronized (timelineChannelsState.fullLock) {
            if (!originalChannel.getId().equals(newChannelId)) {

                boolean canMove = linkedClips.stream()
                        .allMatch(clip -> {
                            int currentIndex = findChannelIndexForClipId(clip.getId()).get() + relativeChannelMove;

                            if (currentIndex < timelineChannelsState.channels.size()) {
                                TimelineChannel movedChannel = timelineChannelsState.channels.get(currentIndex);
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

                                for (int i = timelineChannelsState.channels.size(); i < currentIndex; ++i) {
                                    createChannel(i);
                                }
                            });

                    linkedClips.stream()
                            .forEach(clip -> {
                                int originalIndex = findChannelIndexForClipId(clip.getId()).get();
                                int currentIndex = originalIndex + relativeChannelMove;

                                TimelineChannel originalMovedChannel = timelineChannelsState.channels.get(originalIndex);
                                TimelineChannel newMovedChannel = timelineChannelsState.channels.get(currentIndex);

                                TimelineInterval clipCurrentInterval = clip.getUnmodifiedInterval();
                                TimelineInterval clipNewPosition = clip.getUnmodifiedInterval().butAddOffset(relativeMove);

                                originalMovedChannel.removeClip(clip.getId());
                                clip.setInterval(clipNewPosition);
                                newMovedChannel.addResource(clip);

                                messagingService.sendAsyncMessage(new ClipMovedMessage(clip.getId(), clipNewPosition.getStartPosition(), newMovedChannel.getId(), finalSpecialPositionUsed,
                                        clipCurrentInterval, clip.getGlobalInterval(), moveClipRequest.moreMoveExpected));
                            });
                } else {
                    return false;
                }

            } else {
                boolean canAddResource = linkedClips
                        .stream()
                        .allMatch(clip -> {
                            TimelineChannel channel = findChannelForClipId(clip.getId()).orElseThrow();
                            return channel.canAddResourceAtExcluding(clip.getInterval().butAddOffset(relativeMove), linkedClipIds);
                        });

                if (canAddResource) {
                    List<TimelineChannel> channelForClip = new ArrayList<>();
                    linkedClips
                            .stream()
                            .forEach(clip -> {
                                TimelineChannel channel = findChannelForClipId(clip.getId()).orElseThrow();
                                channel.removeClip(clip.getId());
                                channelForClip.add(channel);
                            });

                    for (int i = 0; i < linkedClips.size(); ++i) {
                        TimelineClip clip = linkedClips.get(i);
                        TimelineChannel channel = channelForClip.get(i);
                        TimelineInterval clipCurrentInterval = clip.getUnmodifiedInterval();
                        TimelinePosition clipNewPosition = clipCurrentInterval.getStartPosition().add(relativeMove);
                        clip.setInterval(new TimelineInterval(clipNewPosition, clipCurrentInterval.getLength()));
                        channel.addResource(clip);
                        messagingService.sendMessage(
                                new ClipMovedMessage(clip.getId(), clipNewPosition, channel.getId(), finalSpecialPositionUsed, clipCurrentInterval, clip.getGlobalInterval(),
                                        moveClipRequest.moreMoveExpected));
                    }

                } else {
                    return false;
                }

            }
        }
        return true;
    }

    private TimelinePosition getNewPositionBasedOnFPS(TimelinePosition newPosition) {
        BigDecimal differenceBetweenFramePosition = newPosition.getSeconds().remainder(projectRepository.getFrameTime());
        if (differenceBetweenFramePosition.compareTo(projectRepository.getFrameTime().divide(TWO)) < 0) {
            newPosition = new TimelinePosition(newPosition.getSeconds().subtract(differenceBetweenFramePosition));
        } else {
            newPosition = new TimelinePosition(newPosition.getSeconds().add(projectRepository.getFrameTime().subtract(differenceBetweenFramePosition)));
        }
        return newPosition;
    }

    private Set<String> fillWithAllTransitiveLinkedClips(MoveClipRequest moveClipRequest) {
        Set<String> linkedClipIds = new HashSet<>(linkClipRepository.getLinkedClips(moveClipRequest.clipId));
        linkedClipIds.add(moveClipRequest.clipId);
        linkedClipIds.addAll(moveClipRequest.additionalClipIds);

        int i = 0;
        do {
            Set<String> newLinkedClips = new HashSet<>(linkedClipIds);
            for (String clipId : linkedClipIds) {
                newLinkedClips.addAll(linkClipRepository.getLinkedClips(clipId));
                newLinkedClips.add(clipId);
            }
            if (newLinkedClips.size() == linkedClipIds.size()) {
                break;
            }
            linkedClipIds = newLinkedClips;
            ++i;
        } while (i < 10);
        return linkedClipIds;
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

    public boolean moveEffect(MoveEffectRequest request) {
        TimelinePosition globalNewPosition = request.getGlobalNewPosition();
        TimelineClip currentClip = findClipForEffect(request.getEffectId()).orElseThrow(() -> new IllegalArgumentException("Clip not found"));
        StatelessEffect effect = currentClip.getEffect(request.getEffectId()).orElseThrow(() -> new IllegalArgumentException("Effect not found"));
        TimelineInterval interval = effect.getInterval();

        Optional<ClosesIntervalChannel> specialPosition = Optional.empty();
        if (request.getMaximumJumpToSpecialPositions().isPresent()) {
            specialPosition = calculateSpecialPositionAround(globalNewPosition, request.getMaximumJumpToSpecialPositions().get(), effect.getGlobalInterval(), List.of(request.getEffectId()),
                    request.getAdditionalSpecialPositions())
                            .stream()
                            .findFirst();
            globalNewPosition = specialPosition.map(a -> a.getClipPosition()).orElse(globalNewPosition);
        }

        int newChannel = currentClip.moveEffect(effect, globalNewPosition);

        EffectMovedMessage message = EffectMovedMessage.builder()
                .withEffectId(request.getEffectId())
                .withOriginalClipId(currentClip.getId())
                .withOldPosition(interval.getStartPosition())
                .withNewPosition(effect.getInterval().getStartPosition())
                .withNewChannelIndex(newChannel)
                .withOriginalInterval(interval)
                .withMoreMoveExpected(request.isMoreMoveExpected())
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

        removeEmptyEffectChannel(clip);
    }

    private void removeEmptyEffectChannel(TimelineClip clip) {
        List<NonIntersectingIntervalList<StatelessEffect>> effectChannels = clip.getEffectChannels();
        for (int i = 0; i < effectChannels.size(); ++i) {
            if (effectChannels.get(i).size() == 0) {
                for (int j = i + 1; j < effectChannels.size(); ++j) {
                    int newChannelIndex = j - 1;
                    effectChannels.get(j)
                            .stream()
                            .forEach(statelessEffect -> {
                                messagingService.sendAsyncMessage(new EffectChannelChangedMessage(statelessEffect.id, newChannelIndex, statelessEffect.interval));
                            });
                }
                effectChannels.remove(i);
                --i;
            }
        }
    }

    @Override
    public Optional<StatelessEffect> findEffectById(String effectId) {
        return findClipForEffect(effectId).flatMap(clip -> clip.getEffect(effectId));
    }

    public void resizeClip(ResizeClipRequest resizeClipRequest) {
        TimelineClip clip = resizeClipRequest.getClip();
        boolean left = resizeClipRequest.isLeft();
        TimelinePosition globalPosition = resizeClipRequest.getPosition();
        boolean useSpecialPoints = resizeClipRequest.isUseSpecialPoints();

        TimelineLength minimumSize = resizeClipRequest.getMinimumSize().orElse(null);
        if (minimumSize != null && getIntervalWhenResizedTo(clip, left, globalPosition).getLength().lessThanOrEqual(minimumSize)) {
            if (left) {
                globalPosition = clip.getInterval().getEndPosition().subtract(minimumSize);
            } else {
                globalPosition = clip.getInterval().getStartPosition().add(minimumSize);
            }
        }

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

            specialPointUsed = findSpecialPositionAround(globalPosition, resizeClipRequest.getMaximumJumpLength(), excludedIds)
                    .stream()
                    .filter(a -> {
                        if (minimumSize == null) {
                            return true;
                        }
                        TimelinePosition newPosition = a.getSpecialPosition();
                        TimelineInterval interval = getIntervalWhenResizedTo(clip, left, newPosition);
                        return interval.getLength().greaterThan(minimumSize);
                    })
                    .findFirst();
            if (specialPointUsed.isPresent()) {
                globalPosition = specialPointUsed.get().getSpecialPosition();
            }
        }

        // resize full size effects
        List<StatelessEffect> effectsToResize = clip.getEffects()
                .stream()
                .filter(effect -> {
                    return effect.getGlobalInterval().getLength().isEquals(clip.getInterval().getLength());
                })
                .collect(Collectors.toList());

        boolean success = channel.resizeClip(clip, left, globalPosition);
        if (success) {
            TimelineClip renewedClip = findClipById(clip.getId()).orElseThrow(() -> new IllegalArgumentException("No such clip"));
            ClipResizedMessage clipResizedMessage = ClipResizedMessage.builder()
                    .withClipId(clip.getId())
                    .withOriginalInterval(originalInterval)
                    .withNewInterval(renewedClip.getInterval())
                    .withSpecialPointUsed(specialPointUsed)
                    .withMoreResizeExpected(resizeClipRequest.isMoreResizeExpected())
                    .build();
            messagingService.sendMessage(clipResizedMessage);

            effectsToResize.stream()
                    .forEach(effect -> {
                        TimelineInterval originalEffectInterval = effect.getInterval();
                        clip.resizeEffect(effect, false, clip.getInterval().getEndPosition());
                        EffectResizedMessage effectResizedMessage = EffectResizedMessage.builder()
                                .withClipId(clip.getId())
                                .withEffectId(effect.getId())
                                .withNewInterval(effect.getInterval())
                                .withOriginalInterval(originalEffectInterval)
                                .withSpecialPositionUsed(Optional.empty())
                                .withMoreResizeExpected(false)
                                .build();
                        messagingService.sendMessage(effectResizedMessage);
                    });

        }
    }

    private TimelineInterval getIntervalWhenResizedTo(IntervalAware clip, boolean left, TimelinePosition newPosition) {
        TimelineInterval interval;
        if (left) {
            interval = clip.getGlobalInterval().butWithStartPosition(newPosition);
        } else {
            interval = clip.getGlobalInterval().butWithEndPosition(newPosition);
        }
        return interval;
    }

    public void resizeEffect(ResizeEffectRequest resizeEffectRequest) {
        StatelessEffect effect = resizeEffectRequest.getEffect();
        boolean left = resizeEffectRequest.isLeft();
        TimelineClip clip = findClipForEffect(effect.getId()).orElseThrow(() -> new IllegalArgumentException("No such clip"));
        TimelinePosition globalPosition = resizeEffectRequest.getGlobalPosition();
        boolean useSpecialPoints = resizeEffectRequest.isUseSpecialPoints();

        TimelineLength minimumSize = resizeEffectRequest.getMinimumLength().orElse(null);
        if (minimumSize != null && getIntervalWhenResizedTo(effect, left, globalPosition).getLength().lessThanOrEqual(minimumSize)) {
            if (left) {
                globalPosition = effect.getInterval().getEndPosition().subtract(minimumSize);
            } else {
                globalPosition = effect.getInterval().getStartPosition().add(minimumSize);
            }
        }

        TimelineInterval originalInterval = clip.getGlobalInterval();

        TimelinePosition newPosition = globalPosition;

        Optional<ClosesIntervalChannel> specialPointUsed = Optional.empty();
        if (useSpecialPoints) {
            specialPointUsed = findSpecialPositionAround(globalPosition, resizeEffectRequest.getMaximumJumpLength(), List.of(effect.getId()))
                    .stream()
                    .filter(a -> {
                        if (minimumSize == null) {
                            return true;
                        }
                        TimelineInterval interval = getIntervalWhenResizedTo(clip, left, a.getSpecialPosition());
                        return interval.getLength().greaterThan(minimumSize);
                    })
                    .findFirst();
            if (specialPointUsed.isPresent()) {
                TimelinePosition specialPosition = specialPointUsed.get().getSpecialPosition();

                if (!wouldResultInZeroLength(effect, left, specialPosition)) {
                    newPosition = specialPosition;
                } else {
                    specialPointUsed = Optional.empty();
                }
            }
        }

        if (resizeEffectRequest.getAllowResizeToDisplaceOtherEffects()) {
            Integer effectChannelIndex = clip.getEffectChannelIndex(resizeEffectRequest.getEffect().getId()).get();
            NonIntersectingIntervalList<StatelessEffect> newChannel = clip.addEffectChannel(effectChannelIndex);
            newChannel.addInterval(clip.removeEffectById(resizeEffectRequest.getEffect().getId()));
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
                    .withMoreResizeExpected(resizeEffectRequest.isMoreResizeExpected())
                    .build();
            messagingService.sendMessage(clipResizedMessage);
            optimizeEffectChannels(clip);
        }
    }

    private boolean wouldResultInZeroLength(StatelessEffect effect, boolean left, TimelinePosition specialPosition) {
        return (left && specialPosition.equals(effect.getInterval().getEndPosition()) ||
                !left && specialPosition.equals(effect.getInterval().getStartPosition()));
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
    public void generateSavedContent(Map<String, Object> generatedContent, SaveMetadata saveMetadata) {
        List<Object> channelContent = new ArrayList<>();
        for (TimelineChannel channel : timelineChannelsState.channels) {
            channelContent.add(channel.generateSavedContent(saveMetadata));
        }
        generatedContent.put("channels", channelContent);
    }

    @Override
    public void loadFrom(JsonNode tree, LoadMetadata loadMetadata) {
        for (var channelToRemove : timelineChannelsState.channels) {
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
    public Set<ClosesIntervalChannel> calculateSpecialPositionAround(TimelinePosition position, TimelineLength inRadius, TimelineInterval intervalToAdd, List<String> ignoredIds,
            List<TimelinePosition> additionalPositions) {
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

        for (TimelinePosition additionalPosition : additionalPositions) {
            BigDecimal startDistance = additionalPosition.distanceFrom(position);
            BigDecimal endDistance = additionalPosition.distanceFrom(endPosition);

            if (startDistance.compareTo(endDistance) < 0) {
                if (startDistance.compareTo(inRadius.getSeconds()) < 0) {
                    for (TimelineChannel channel : getChannels()) {
                        set.add(new ClosesIntervalChannel(new TimelineLength(startDistance), channel.getId(), additionalPosition, additionalPosition));
                    }
                }
            } else {
                if (endDistance.compareTo(inRadius.getSeconds()) < 0) {
                    for (TimelineChannel channel : getChannels()) {
                        set.add(new ClosesIntervalChannel(new TimelineLength(endDistance), channel.getId(), additionalPosition.subtract(clipLength), additionalPosition));
                    }
                }
            }
        }

        return set;
    }

    private Set<ClosesIntervalChannel> findSpecialPositionAround(TimelinePosition position, TimelineLength length, List<String> ignoredIds) {
        return timelineChannelsState.channels.stream()
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
        return timelineChannelsState.channels.stream()
                .flatMap(channel -> channel.getAllClipId().stream())
                .collect(Collectors.toList());
    }

    public List<String> getAllChannelIds() {
        return timelineChannelsState.channels.stream()
                .map(channel -> channel.getId())
                .collect(Collectors.toList());
    }

    public void changeClipForEffect(StatelessEffect originalEffect, String newClipId, TimelinePosition newPosition) {
        TimelineClip originalClip = findClipForEffect(originalEffect.getId()).orElseThrow();
        TimelineClip newClip = findClipById(newClipId).orElseThrow();
        synchronized (timelineChannelsState.fullLock) {
            originalClip.removeEffectById(originalEffect.getId());
            newClip.addEffectAtAnyChannel(originalEffect);

            EffectMovedToDifferentClipMessage message = EffectMovedToDifferentClipMessage.builder()
                    .withEffectId(originalEffect.getId())
                    .withModifiedInterval(originalEffect.getInterval())
                    .withNewClipId(newClipId)
                    .withOriginalClipId(originalClip.getId())
                    .build();

            messagingService.sendAsyncMessage(message);

            MoveEffectRequest moveEffectRequest = MoveEffectRequest.builder()
                    .withEffectId(originalEffect.getId())
                    .withGlobalNewPosition(newPosition.add(newClip.getInterval().getStartPosition()))
                    .withMoreMoveExpected(false)
                    .withMaximumJumpToSpecialPositions(Optional.empty())
                    .build();

            moveEffect(moveEffectRequest);
        }
    }

    @Override
    public Optional<Integer> findChannelIndexForClipId(String clipId) {
        return findChannelForClipId(clipId)
                .flatMap(a -> findChannelIndex(a.getId()));
    }

    private Optional<Integer> findChannelIndex(String channelId) {
        for (int i = 0; i < timelineChannelsState.channels.size(); ++i) {
            if (timelineChannelsState.channels.get(i).getId().equals(channelId)) {
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

    public void addExistingEffect(TimelineClip clipToAdd, StatelessEffect effect) {
        addEffectForClip(clipToAdd, effect);
    }

    public TimelinePosition findEndPosition() {
        TimelinePosition endPosition = TimelinePosition.ofZero();
        for (var channel : timelineChannelsState.channels) {
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
                optimizeEffectChannels(clip);
            }
        }
    }

    private void optimizeEffectChannels(TimelineClip clip) {
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
        return findIntersectingClipsData(currentPosition)
                .stream()
                .map(clip -> clip.getId())
                .collect(Collectors.toList());
    }

    public List<TimelineClip> findIntersectingClipsData(TimelinePosition currentPosition) {
        return timelineChannelsState.channels
                .stream()
                .map(channel -> channel.getDataAt(currentPosition))
                .flatMap(Optional::stream)
                .collect(Collectors.toList());
    }

    public CopyOnWriteArrayList<TimelineChannel> getChannels() {
        return timelineChannelsState.channels;
    }

    public int findMaximumVideoBitRate() {
        return timelineChannelsState.channels
                .stream()
                .map(channel -> channel.findMaximumVideoBitRate())
                .max(Integer::compareTo)
                .orElse(0);
    }

    public int findMaximumAudioBitRate() {
        return timelineChannelsState.channels
                .stream()
                .map(channel -> channel.findMaximumAudioBitRate())
                .max(Integer::compareTo)
                .orElse(0);
    }

    public Optional<TimelineClip> findFirstClipToLeft(String clipId) {
        TimelineChannel channel = findChannelForClipId(clipId).get();
        return channel.findFirstClipToLeft(clipId);
    }

    public Optional<TimelineClip> findFirstClipToRight(String clipId) {
        TimelineChannel channel = findChannelForClipId(clipId).get();
        return channel.findFirstClipToRight(clipId);
    }

    public void moveChannel(MoveChannelRequest moveChannelRequest) {
        synchronized (timelineChannelsState.fullLock) {
            int originalIndex = moveChannelRequest.getOriginalIndex();
            int newIndex = moveChannelRequest.getNewIndex();

            if (newIndex >= 0 && newIndex < getChannels().size()) {
                TimelineChannel originalChannel = timelineChannelsState.channels.remove(originalIndex);

                timelineChannelsState.channels.add(newIndex, originalChannel);

                List<TimelineInterval> affectedIntervals = originalChannel.getAllClipId().stream()
                        .flatMap(clipId -> findClipById(clipId).stream())
                        .map(clip -> clip.getInterval())
                        .collect(Collectors.toList());

                messagingService.sendAsyncMessage(new ChannelMovedMessage(newIndex, originalIndex, affectedIntervals));
            } else {
                logger.info("Trying to move channel " + originalIndex + " -> " + newIndex + " but it is outside of range");
            }
        }
    }

    public Optional<Integer> findChannelIndexByChannelId(String channelId) {
        List<String> channels = this.getAllChannelIds();
        for (int i = 0; i < channels.size(); ++i) {
            if (channels.get(i).equals(channelId)) {
                return Optional.of(i);
            }
        }
        return Optional.empty();
    }

}
