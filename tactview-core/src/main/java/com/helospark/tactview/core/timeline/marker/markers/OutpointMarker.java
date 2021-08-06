package com.helospark.tactview.core.timeline.marker.markers;

import com.helospark.tactview.core.timeline.marker.MarkerType;

public class OutpointMarker extends Marker {

    public OutpointMarker() {
        super(MarkerType.OUTPOINT);
    }

    @Override
    public String describe() {
        return "Outpoint";
    }

}
