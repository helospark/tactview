package com.helospark.tactview.core.timeline;

import java.util.List;

public class VideoClip extends TimelineClip {
    private double fps;
    private VideoSource backingSource;
    private List<StatelessVideoEffect> effects;
}
