package com.helospark.tactview.core.timeline.marker.markers;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.helospark.tactview.core.timeline.marker.MarkerType;

public class GeneralMarker extends Marker {
    private String name;

    public GeneralMarker(@JsonProperty("name") String name) {
        super(MarkerType.GENERAL);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String describe() {
        return "Marker: \"" + name + "\"";
    }

}
