package com.helospark.tactview.core.timeline;

public class ClosesIntervalChannel implements Comparable<ClosesIntervalChannel> {
    private TimelineLength distance;
    private String channelId;
    private TimelinePosition clipPosition;
    private TimelinePosition specialPosition;

    public ClosesIntervalChannel(TimelineLength distance, String channelId, TimelinePosition clipPosition, TimelinePosition specialPosition) {
        this.distance = distance;
        this.channelId = channelId;
        this.clipPosition = clipPosition;
        this.specialPosition = specialPosition;
    }

    public TimelineLength getDistance() {
        return distance;
    }

    public String getChannelId() {
        return channelId;
    }

    public TimelinePosition getClipPosition() {
        return clipPosition;
    }

    public TimelinePosition getSpecialPosition() {
        return specialPosition;
    }

    @Override
    public String toString() {
        return "ClosesIntervalChannel [distance=" + distance + ", channelId=" + channelId + ", clipPosition=" + clipPosition + ", specialPosition=" + specialPosition + "]";
    }

    @Override
    public int compareTo(ClosesIntervalChannel o) {
        return distance.compareTo(o.getDistance());
    }

    public void setPosition(TimelinePosition position) {
        this.clipPosition = position;
    }

}
