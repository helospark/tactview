package com.helospark.tactview.core.timeline.marker.markers;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.helospark.tactview.core.timeline.marker.MarkerType;

public class ChapterMarker extends Marker {
    private String name;

    public ChapterMarker(@JsonProperty("name") String name) {
        super(MarkerType.CHAPTER);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String describe() {
        return "Chapter: \"" + name + "\"";
    }

}
