package com.helospark.tactview.core.timeline.marker.markers;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.helospark.tactview.core.timeline.marker.MarkerType;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @Type(name = "CHAPTER", value = ChapterMarker.class),
        @Type(name = "GENERAL", value = GeneralMarker.class),
        @Type(name = "INPOINT", value = InpointMarker.class),
        @Type(name = "OUTPOINT", value = OutpointMarker.class)
})
public abstract class Marker {
    private MarkerType type;

    public Marker(@JsonProperty("type") MarkerType type) {
        this.type = type;
    }

    public MarkerType getType() {
        return type;
    }

    public abstract String describe();

}
