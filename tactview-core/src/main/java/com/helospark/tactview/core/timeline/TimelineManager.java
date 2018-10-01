package com.helospark.tactview.core.timeline;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.effect.CreateEffectRequest;
import com.helospark.tactview.core.timeline.effect.EffectFactory;
import com.helospark.tactview.core.timeline.message.ChannelAddedMessage;
import com.helospark.tactview.core.timeline.message.ChannelRemovedMessage;
import com.helospark.tactview.core.timeline.message.ClipAddedMessage;
import com.helospark.tactview.core.timeline.message.ClipRemovedMessage;
import com.helospark.tactview.core.timeline.message.EffectAddedMessage;
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
        if (!findChannelForId(channelId).isPresent()) {
            return false;
        }
        TimelineChannel channel = findChannelForId(channelId).get();
        return channel.canAddResourceAt(position, length);
    }

    public TimelineClip addResource(String channelId, TimelinePosition position, String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IllegalArgumentException(filePath + " does not exists");
        }
        TimelineClip clip = clipFactoryChain.createClip(file, position);
        TimelineChannel channelToAddResourceTo = findChannelWithId(channelId).orElseThrow(() -> new IllegalArgumentException("Channel doesn't exist"));
        if (channelToAddResourceTo.canAddResourceAt(clip.getInterval())) {
            channelToAddResourceTo.addResource(clip);
        } else {
            throw new IllegalArgumentException("Cannot add clip");
        }
        messagingService.sendAsyncMessage(new ClipAddedMessage(clip.getId(), channelToAddResourceTo.getId(), position, clip));
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
                .map(clip -> clip.getFrame(request.getPosition(), request.getScale()))
                .map(clip -> expandFrame(clip, request.getPreviewWidth(), request.getPreviewHeight()))
                .collect(Collectors.toList());
        ClipFrameResult finalImage = frameBufferMerger.alphaMergeFrames(frames, request.getPreviewWidth(), request.getPreviewHeight());
        ClipFrameResult finalResult = executeGlobalEffectsOn(finalImage);
        return finalResult.getBuffer();
    }

    private ClipFrameResult expandFrame(ClipFrameResult clip, Integer previewWidth, Integer previewHeight) {
        ByteBuffer outputBuffer = ByteBuffer.allocateDirect(previewHeight * previewWidth * 4);
        ByteBuffer inputBuffer = clip.getBuffer();
        int widthTo = Math.min(clip.getWidth(), previewWidth);
        int heightTo = Math.min(clip.getHeight(), previewHeight);
        int numberOfBytesInARow = widthTo * 4;
        byte[] tmpBuffer = new byte[numberOfBytesInARow];

        for (int i = 0; i < heightTo; ++i) {
            inputBuffer.position(i * clip.getWidth() * 4);
            inputBuffer.get(tmpBuffer, 0, numberOfBytesInARow);

            outputBuffer.position(i * previewWidth * 4);
            outputBuffer.put(tmpBuffer, 0, numberOfBytesInARow);
        }
        return new ClipFrameResult(outputBuffer, previewWidth, previewHeight);
    }

    private ClipFrameResult executeGlobalEffectsOn(ClipFrameResult finalImage) {
        return finalImage; // todo: do implementation
    }

    public StatelessEffect addEffectForClip(String id, String effectId, TimelineInterval timelineInterval) {
        VideoClip clipById = (VideoClip) findClipById(id).get();
        StatelessVideoEffect effect = (StatelessVideoEffect) createEffect(effectId, timelineInterval); // sound?
        clipById.addEffect(effect);
        messagingService.sendAsyncMessage(new EffectAddedMessage(effect.getId(), clipById.getId(), timelineInterval.getStartPosition(), effect));
        return effect;
    }

    private StatelessEffect createEffect(String effectId, TimelineInterval timelineInterval) {
        CreateEffectRequest request = new CreateEffectRequest(timelineInterval, effectId);
        return effectFactoryChain.stream()
                .filter(effectFactory -> effectFactory.doesSupport(request))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No factory for " + effectId))
                .createEffect(request);
    }

    public void removeResource(String clipId) {
        TimelineChannel channel = findChannelForId(clipId)
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

    private Optional<TimelineChannel> findChannelForId(String id) {
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
        boolean success = findChannelForId(channelId)
                .map(channelToRemove -> channels.remove(channelToRemove))
                .orElse(false);
        if (success) {
            messagingService.sendAsyncMessage(new ChannelRemovedMessage(channelId));
        }
    }

}
