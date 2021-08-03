package com.helospark.tactview.core.timeline.subtimeline;

import java.util.Optional;

import com.helospark.tactview.core.timeline.TimelineClip;

public interface ClipContainingClip {

    public Optional<TimelineClip> findClipById(String id);

}
