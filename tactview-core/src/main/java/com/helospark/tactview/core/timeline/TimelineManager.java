package com.helospark.tactview.core.timeline;

import static com.helospark.tactview.core.timeline.TimelineClipType.VIDEO;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.effect.EffectFactory;

@Component
public class TimelineManager {
    // state
    private List<StatelessVideoEffect> globalEffects;
    private ConcurrentHashMap<Integer, TimelineChannel> channels = new ConcurrentHashMap<>();

    // stateless
    private List<ClipFactory> clipFactoryChain;
    private List<EffectFactory> effectFactoryChain;
    private EmptyByteBufferFactory emptyByteBufferFactory;

    public TimelineManager(List<ClipFactory> clipFactoryChain, EmptyByteBufferFactory emptyByteBufferFactory,
            List<EffectFactory> effectFactoryChain) {
        this.clipFactoryChain = clipFactoryChain;
        this.emptyByteBufferFactory = emptyByteBufferFactory;
        this.effectFactoryChain = effectFactoryChain;
    }

    public boolean canAddClipAt(int channelNumber, TimelinePosition position, TimelineLength length) {
        if (channelNumber < 0) {
            throw new IllegalArgumentException("Channel must be greater than 0");
        }
        if (channels.containsKey(channelNumber)) {
            return true;
        }
        TimelineChannel channel = channels.get(channelNumber);
        return channel.canAddResourceAt(position, length);
    }

    public TimelineClip addResource(int channelNumber, TimelinePosition position, String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IllegalArgumentException(filePath + " does not exists");
        }
        TimelineClip clip = clipFactoryChain.stream()
                .filter(a -> a.doesSupport(file))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No clip factory found for " + file))
                .createClip(file, position);
        TimelineChannel channelToAddResourceTo = channels.computeIfAbsent(channelNumber, key -> new TimelineChannel());
        if (channelToAddResourceTo.canAddResourceAt(clip.getInterval())) {
            channelToAddResourceTo.addResource(clip);
        } else {
            throw new IllegalArgumentException("Cannot add clip");
        }
        return clip;
    }

    public ByteBuffer getFrames(TimelineManagerFramesRequest request) {
        return getSingleFrame(request); // todo: multiple frames
    }

    public ByteBuffer getSingleFrame(TimelineManagerFramesRequest request) {
        List<ByteBuffer> frames = channels.values()
                .parallelStream()
                .map(channel -> channel.getDataAt(request.getPosition()))
                .flatMap(Optional::stream)
                .filter(clip -> clip.getType().equals(VIDEO)) // audio separate?
                .map(clip -> (VideoClip) clip)
                .map(clip -> clip.getFrame(request.getPosition(), request.getPreviewWidth(), request.getPreviewHeight()))
                .collect(Collectors.toList());
        ByteBuffer finalImage = alphaMergeFrames(frames, request.getPreviewWidth(), request.getPreviewHeight());
        return executeGlobalEffectsOn(finalImage);
    }

    private ByteBuffer alphaMergeFrames(List<ByteBuffer> frames, Integer width, Integer height) {
        if (frames.size() > 0) {
            return frames.get(0); // todo: do implementation
        } else {
            return emptyByteBufferFactory.createEmptyByteImage(width, height);
        }
    }

    private ByteBuffer executeGlobalEffectsOn(ByteBuffer finalImage) {
        return finalImage; // todo: do implementation
    }

    public StatelessEffect addEffectForClip(String id, String effectId, TimelineInterval timelineInterval) {
        VideoClip clipById = (VideoClip) findClipById(id).get();
        StatelessVideoEffect effect = (StatelessVideoEffect) createEffect(effectId, timelineInterval); // sound?
        clipById.addEffect(effect);
        return effect;
    }

    private StatelessEffect createEffect(String effectId, TimelineInterval timelineInterval) {
        return effectFactoryChain.stream()
                .filter(effectFactory -> effectFactory.doesSupport(effectId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No factory for " + effectId))
                .createEffect(effectId, timelineInterval);
    }

    private Optional<TimelineClip> findClipById(String id) {
        return channels.values()
                .stream()
                .map(channel -> channel.findClipById(id))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

}
