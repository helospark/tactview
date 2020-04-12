package com.helospark.tactview.core.timeline;

import java.util.function.BiFunction;

import com.helospark.tactview.core.timeline.blendmode.BlendModeStrategy;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;

//TODO: eventually all public methods should be extracted here.
//for now only the ones related to rendering are extracted
public interface VisualClipAwareTimelineClip extends ITimelineClip {

    public ReadOnlyClipImage getFrame(GetFrameRequest request);

    public BlendModeStrategy getBlendModeAt(TimelinePosition position);

    public double getAlpha(TimelinePosition position);

    public BiFunction<Integer, Integer, Integer> getVerticalAlignment(TimelinePosition timelinePosition);

    public BiFunction<Integer, Integer, Integer> getHorizontalAlignment(TimelinePosition timelinePosition);

    public int getXPosition(TimelinePosition timelinePosition, double scale);

    public int getYPosition(TimelinePosition timelinePosition, double scale);
}
