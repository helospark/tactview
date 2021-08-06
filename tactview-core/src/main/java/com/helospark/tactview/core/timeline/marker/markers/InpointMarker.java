package com.helospark.tactview.core.timeline.marker.markers;

import com.helospark.tactview.core.timeline.marker.MarkerType;

public class InpointMarker extends Marker {

    public InpointMarker() {
        super(MarkerType.INPOINT);
    }

    @Override
    public String describe() {
        return "Inpoint";
    }
}
