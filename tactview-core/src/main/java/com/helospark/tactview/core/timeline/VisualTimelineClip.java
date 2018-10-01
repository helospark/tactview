package com.helospark.tactview.core.timeline;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.helospark.tactview.core.decoder.VisualMediaMetadata;
import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;

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
        ClipFrameResult tmp;

        List<StatelessVideoEffect> actualEffects = getEffectsAt(relativePosition);

        ClipFrameResult frameResult = new ClipFrameResult(frame, width, height);
        ClipFrameResult newBufferResult = new ClipFrameResult(newBuffer, width, height);

        for (StatelessVideoEffect effect : actualEffects) {
            if (effect.isLocal()) {

                StatelessEffectRequest request = StatelessEffectRequest.builder()
                        .withClipPosition(relativePosition)
                        .withEffectPosition(relativePosition.from(effect.interval.getStartPosition()))
                        .withCurrentFrame(frameResult)
                        .build();

                effect.fillFrame(newBufferResult, request);

                // swap buffers around
                tmp = newBufferResult;
                newBufferResult = frameResult;
                frameResult = tmp;
            }
        }

        return frameResult;
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
