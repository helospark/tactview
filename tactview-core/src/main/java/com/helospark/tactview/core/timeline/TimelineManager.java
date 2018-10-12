package com.helospark.tactview.core.timeline;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.decoder.framecache.GlobalMemoryManagerAccessor;
import com.helospark.tactview.core.timeline.effect.CreateEffectRequest;
import com.helospark.tactview.core.timeline.effect.EffectFactory;
import com.helospark.tactview.core.timeline.message.ChannelAddedMessage;
import com.helospark.tactview.core.timeline.message.ChannelRemovedMessage;
import com.helospark.tactview.core.timeline.message.ClipAddedMessage;
import com.helospark.tactview.core.timeline.message.ClipDescriptorsAdded;
import com.helospark.tactview.core.timeline.message.ClipMovedMessage;
import com.helospark.tactview.core.timeline.message.ClipRemovedMessage;
import com.helospark.tactview.core.timeline.message.ClipResizedMessage;
import com.helospark.tactview.core.timeline.message.EffectAddedMessage;
import com.helospark.tactview.core.timeline.message.EffectMovedMessage;
import com.helospark.tactview.core.util.messaging.MessagingService;

@Component
public class TimelineManager {
    // state
    private List<StatelessVideoEffect> globalEffects;
    private CopyOnWriteArrayList<TimelineChannel> channels = new CopyOnWriteArrayList<>();

    // stateless
    private List<EffectFactory> effectFactoryChain;
    private MessagingService messagingService;
    private ClipFactoryChain clipFactoryChain;
    private FrameBufferMerger frameBufferMerger;

    public TimelineManager(FrameBufferMerger frameBufferMerger,
            List<EffectFactory> effectFactoryChain, MessagingService messagingService, ClipFactoryChain clipFactoryChain) {
        this.effectFactoryChain = effectFactoryChain;
        this.messagingService = messagingService;
        this.clipFactoryChain = clipFactoryChain;
        this.frameBufferMerger = frameBufferMerger;
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
        TimelinePosition position = request.getPosition();
        TimelineClip clip = clipFactoryChain.createClip(request);
        TimelineChannel channelToAddResourceTo = findChannelWithId(channelId).orElseThrow(() -> new IllegalArgumentException("Channel doesn't exist"));
        if (channelToAddResourceTo.canAddResourceAt(clip.getInterval())) {
            channelToAddResourceTo.addResource(clip);
        } else {
            throw new IllegalArgumentException("Cannot add clip");
        }
        messagingService.sendMessage(new ClipAddedMessage(clip.getId(), channelToAddResourceTo.getId(), position, clip, clip.isResizable()));
        messagingService.sendMessage(new ClipDescriptorsAdded(clip.getId(), clip.getDescriptors()));

        return clip;
    }

    private Optional<TimelineChannel> findChannelWithId(String channelId) {
        return channels.stream()
                .filter(channel -> channel.getId().equals(channelId))
                .findFirst();
    }

    public ByteBuffer getFrames(TimelineManagerFramesRequest request) {
        return getSingleFrame(request); // todo: multiple frames
    }

    public ByteBuffer getSingleFrame(TimelineManagerFramesRequest request) {
        List<ClipFrameResult> frames = channels
                .parallelStream()
                .map(channel -> channel.getDataAt(request.getPosition()))
                .flatMap(Optional::stream)
                .filter(clip -> clip instanceof VisualTimelineClip) // audio separate?
                .map(clip -> (VisualTimelineClip) clip)
                .map(clip -> {
                    GetFrameRequest frameRequest = GetFrameRequest.builder()
                            .withScale(request.getScale())
                            .withPosition(request.getPosition())
                            .withExpectedWidth(request.getPreviewWidth())
                            .withExpectedHeight(request.getPreviewHeight())
                            .build();

                    ClipFrameResult frameResult = clip.getFrame(frameRequest);
                    ClipFrameResult expandedFrame = expandFrame(frameResult, clip, request.getPosition(), request.getPreviewWidth(), request.getPreviewHeight());
                    GlobalMemoryManagerAccessor.memoryManager.returnBuffer(frameResult.getBuffer());
                    return expandedFrame;
                })
                .collect(Collectors.toList());
        ClipFrameResult finalImage = frameBufferMerger.alphaMergeFrames(frames, request.getPreviewWidth(), request.getPreviewHeight());

        frames.stream()
                .forEach(a -> GlobalMemoryManagerAccessor.memoryManager.returnBuffer(a.getBuffer()));

        ClipFrameResult finalResult = executeGlobalEffectsOn(finalImage);
        return finalResult.getBuffer();
    }

    private ClipFrameResult expandFrame(ClipFrameResult frameResult, VisualTimelineClip clip, TimelinePosition timelinePosition, Integer previewWidth, Integer previewHeight) {
        ByteBuffer outputBuffer = GlobalMemoryManagerAccessor.memoryManager.requestBuffer(previewHeight * previewWidth * 4);
        ByteBuffer inputBuffer = frameResult.getBuffer();

        int requestedXPosition = clip.getXPosition(timelinePosition);
        int requestedYPosition = clip.getYPosition(timelinePosition);

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
        VisualTimelineClip clipById = (VisualTimelineClip) findClipById(id).get();
        StatelessVideoEffect effect = (StatelessVideoEffect) createEffect(effectId, position); // sound?
        clipById.addEffect(effect);
        messagingService.sendAsyncMessage(new EffectAddedMessage(effect.getId(), clipById.getId(), position, effect));
        return effect;
    }

    private StatelessEffect createEffect(String effectId, TimelinePosition position) {
        CreateEffectRequest request = new CreateEffectRequest(position, effectId);
        return effectFactoryChain.stream()
                .filter(effectFactory -> effectFactory.doesSupport(request))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No factory for " + effectId))
                .createEffect(request);
    }

    public void removeResource(String clipId) {
        TimelineChannel channel = findChannelForClipId(clipId)
                .orElseThrow(() -> new IllegalArgumentException("No channel contains " + clipId));
        channel.removeClip(clipId);
        messagingService.sendAsyncMessage(new ClipRemovedMessage(clipId));
    }

    public Optional<TimelineClip> findClipById(String id) {
        return channels
                .stream()
                .map(channel -> channel.findClipById(id))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

    private Optional<TimelineChannel> findChannelForClipId(String id) {
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

    public boolean moveClip(String clipId, TimelinePosition newPosition, String newChannelId) {
        TimelineChannel originalChannel = findChannelForClipId(clipId).orElseThrow(() -> new IllegalArgumentException("Cannot find clip " + clipId));
        TimelineChannel newChannel = findChannelWithId(newChannelId).orElseThrow(() -> new IllegalArgumentException("Cannot find channel " + newChannelId));

        if (!originalChannel.equals(newChannel)) {
            TimelineClip clipToMove = findClipById(clipId).orElseThrow(() -> new IllegalArgumentException("Cannot find clip"));
            // todo: some atomity would be nice here
            if (newChannel.canAddResourceAt(clipToMove.getInterval())) {
                originalChannel.removeClip(clipId);
                newChannel.addResource(clipToMove);

                messagingService.sendAsyncMessage(new ClipMovedMessage(clipId, newPosition, newChannelId));
            }
        } else {
            boolean success = originalChannel.moveClip(clipId, newPosition);

            if (success) {
                messagingService.sendAsyncMessage(new ClipMovedMessage(clipId, newPosition, newChannelId));
            }
        }
        return true;
    }

    public boolean moveEffect(String effectId, TimelinePosition newPosition, String newClipId, int newEffectChannel) {
        TimelineClip currentClip = findClipForEffect(effectId).orElseThrow(() -> new IllegalArgumentException("Clip not found"));
        StatelessEffect effect = currentClip.getEffect(effectId).orElseThrow(() -> new IllegalArgumentException("Effect not found"));
        if (currentClip.getId().equals(newClipId)) {
            TimelineInterval interval = effect.getInterval();
            boolean success = currentClip.moveEffect(effect, newPosition, newEffectChannel);

            EffectMovedMessage message = EffectMovedMessage.builder()
                    .withEffectId(effectId)
                    .withOriginalClipId(currentClip.getId())
                    .withNewClipId(newClipId)
                    .withOldPosition(interval.getStartPosition())
                    .withNewPosition(newPosition)
                    .withNewChannelIndex(newEffectChannel)
                    .build();

            messagingService.sendAsyncMessage(message);

            return success;
        } else {
            TimelineClip newClip = findClipById(newClipId).orElseThrow(() -> new IllegalArgumentException("Clip not found"));
            TimelineInterval newInterval = new TimelineInterval(newPosition, effect.getInterval().getWidth());
            if (newClip.canAddEffectAt(newEffectChannel, newInterval)) {
                //                currentClip.removeEffect(effect);
                //                effect.setInterval(newInterval);
                //                newClip.addEffect(effect);
            }
        }
        return true;
    }

    public void removeEffect(String effectId) {
        findClipForEffect(effectId)
                .ifPresent(clip -> clip.removeEffectById(effectId));
    }

    public Optional<StatelessEffect> findEffectById(String effectId) {
        return findClipForEffect(effectId).flatMap(clip -> clip.getEffect(effectId));
    }

    public void resizeClip(TimelineClip clip, boolean left, TimelinePosition position) {
        TimelineChannel channel = findChannelForClipId(clip.getId()).orElseThrow(() -> new IllegalArgumentException("No such channel"));
        boolean success = channel.resizeClip(clip, left, position);
        if (success) {
            TimelineClip renewedClip = findClipById(clip.getId()).orElseThrow(() -> new IllegalArgumentException("No such clip"));
            ClipResizedMessage clipResizedMessage = ClipResizedMessage.builder()
                    .withClipId(clip.getId())
                    .withNewInterval(renewedClip.getInterval())
                    .build();
            messagingService.sendAsyncMessage(clipResizedMessage);
        }
    }

}
