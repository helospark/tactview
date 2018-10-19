package com.helospark.tactview.core.timeline;

public class TimelineInterval {
    private TimelinePosition startPosition;
    private TimelineLength length;
    private TimelinePosition endPosition;

    public TimelineInterval(TimelinePosition startPosition, TimelineLength length) {
        this.startPosition = startPosition;
        this.length = length;
        this.endPosition = startPosition.add(length);
    }

    public TimelineInterval(TimelinePosition startPosition, TimelinePosition endPosition) {
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.length = TimelineLength.getLength(startPosition, endPosition);
    }

    public static TimelineInterval ofPoint(TimelinePosition position) {
        return new TimelineInterval(position, TimelineLength.ofZero());
    }

    public TimelinePosition getEndPosition() {
        return endPosition;
    }

    public TimelinePosition getStartPosition() {
        return startPosition;
    }

    public TimelineLength getLength() {
        return length;
    }

    public boolean contains(TimelinePosition position) {
        boolean isLargerThanStart = position.getSeconds().compareTo(startPosition.getSeconds()) >= 0;
        boolean isSmallerThanEnd = position.getSeconds().compareTo(endPosition.getSeconds()) <= 0;
        return isLargerThanStart && isSmallerThanEnd;
    }

    public TimelineInterval butWithStartPosition(TimelinePosition newStartPosition) {
        return new TimelineInterval(newStartPosition, this.endPosition);
    }

    public TimelineInterval butWithEndPosition(TimelinePosition newEndPosition) {
        return new TimelineInterval(this.startPosition, newEndPosition);
    }

    @Override
    public String toString() {
        return "TimelineInterval [startPosition=" + startPosition + ", length=" + length + ", endPosition=" + endPosition + "]";
    }

    public TimelineInterval butMoveStartPostionTo(TimelinePosition offsetStartPosition) {
        return new TimelineInterval(offsetStartPosition, offsetStartPosition.add(length));
    }

}
