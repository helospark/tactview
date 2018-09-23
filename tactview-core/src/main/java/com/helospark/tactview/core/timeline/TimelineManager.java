package com.helospark.tactview.core.timeline;

import static com.helospark.tactview.core.timeline.TimelineClipType.VIDEO;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.helospark.lightdi.annotation.Component;

@Component
public class TimelineManager {
    // state
    private List<StatelessVideoEffect> globalEffects;
    private ConcurrentHashMap<Integer, TimelineChannel> channels = new ConcurrentHashMap<>();

    // stateless
    private List<ClipFactory> clipFactoryChain;
    private EmptyByteBufferFactory emptyByteBufferFactory;

    public TimelineManager(List<ClipFactory> clipFactoryChain, EmptyByteBufferFactory emptyByteBufferFactory) {
        this.clipFactoryChain = clipFactoryChain;
        this.emptyByteBufferFactory = emptyByteBufferFactory;
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

    public void onResourceAdded(int channelNumber, TimelinePosition position, String filePath) {
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

}
