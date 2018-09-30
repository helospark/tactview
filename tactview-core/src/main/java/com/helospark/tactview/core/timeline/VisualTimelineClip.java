package com.helospark.tactview.core.timeline;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.helospark.tactview.core.decoder.VisualMediaMetadata;

public abstract class VisualTimelineClip extends TimelineClip {
    private List<NonIntersectingIntervalList<StatelessVideoEffect>> effectChannels = new ArrayList<>();
    private VisualMediaMetadata mediaMetadata;

    public VisualTimelineClip(VisualMediaMetadata visualMediaMetadata, TimelineInterval interval, TimelineClipType type) {
        super(interval, type);
        this.mediaMetadata = visualMediaMetadata;
    }

    public ClipFrameResult getFrame(TimelinePosition position, double scale) {
        int width = (int) (mediaMetadata.getWidth() * scale);
        int height = (int) (mediaMetadata.getHeight() * scale);
        TimelinePosition relativePosition = position.from(getInterval().getStartPosition());
        ByteBuffer frame = requestFrame(relativePosition, width, height);
        ByteBuffer newBuffer = ByteBuffer.allocateDirect(frame.capacity()); // move to cache
        ByteBuffer tmp;

        List<StatelessVideoEffect> actualEffects = getEffectsAt(relativePosition);

        for (StatelessVideoEffect effect : actualEffects) {
            if (effect.isLocal()) {
                effect.fillFrame(newBuffer, frame);

                // swap buffers around
                tmp = newBuffer;
                newBuffer = frame;
                frame = tmp;
            }
        }

        return new ClipFrameResult(frame, width, height);
    }

    public abstract ByteBuffer requestFrame(TimelinePosition position, int width, int height);

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

    public List<NonIntersectingIntervalList<StatelessVideoEffect>> getEffectChannels() {
        return effectChannels;
    }

    public abstract VisualMediaMetadata getMediaMetadata();

}
