package com.helospark.tactview.ui.javafx.uicomponents.domain;

import com.helospark.tactview.core.timeline.TimelinePosition;

public class ChapterInformation {
    private TimelinePosition position;
    private String name;

    public ChapterInformation(TimelinePosition position, String name) {
        this.position = position;
        this.name = name;
    }

    public TimelinePosition getPosition() {
        return position;
    }

    public String getName() {
        return name;
    }

}
