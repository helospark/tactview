package com.helospark.tactview.core.timeline;

import static com.helospark.tactview.core.timeline.TimelineClipType.VIDEO;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.helospark.tactview.core.decoder.MediaDataRequest;
import com.helospark.tactview.core.decoder.MediaMetadata;

public class VideoClip extends TimelineClip {
    private MediaMetadata mediaMetadata;
    private VideoSource backingSource;
    private TimelinePosition startPosition;

    private List<NonIntersectingIntervalList<StatelessVideoEffect>> effectChannels = new ArrayList<>();

    public VideoClip(MediaMetadata mediaMetadata, VideoSource backingSource, TimelinePosition startPosition, TimelineLength length) {
        super(new TimelineInterval(startPosition, length), VIDEO);
        this.mediaMetadata = mediaMetadata;
        this.backingSource = backingSource;
        this.startPosition = startPosition;
    }

    public ByteBuffer getFrame(TimelinePosition position, int width, int height) {
        TimelinePosition relativePosition = position.from(startPosition);
        MediaDataRequest request = MediaDataRequest.builder()
                .withFile(new File(backingSource.backingFile))
                .withHeight(height)
                .withWidth(width)
                .withMetadata(mediaMetadata)
                .withStart(relativePosition)
                .withNumberOfFrames(1)
                .build();
        ByteBuffer frame = backingSource.decoder.readFrames(request).getVideoFrames().get(0);
        ByteBuffer newBuffer = ByteBuffer.allocateDirect(frame.capacity()); // move to cache
        ByteBuffer tmp;

        List<StatelessVideoEffect> actualEffects = getEffectsAt(relativePosition);

        for (StatelessVideoEffect effect : actualEffects) {
            effect.fillFrame(newBuffer, frame);

            // swap buffers around
            tmp = newBuffer;
            newBuffer = frame;
            frame = tmp;
        }

        return frame;
    }

    private List<StatelessVideoEffect> getEffectsAt(TimelinePosition position) {
        return effectChannels.stream()
                .map(effectChannel -> effectChannel.getElementWithIntervalContainingPoint(position))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

    }

    public void addEffect(StatelessVideoEffect effect) {
        NonIntersectingIntervalList<StatelessVideoEffect> newList = new NonIntersectingIntervalList<>();
        effectChannels.add(newList);
        newList.addInterval(effect);
    }
}
