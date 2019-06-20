package com.helospark.tactview.core.timeline.longprocess;

public interface LongProcessAudibleImagePushAware {

    public default void beginLongPush() {

    }

    public default void endToPushLongImages() {

    }

    public default void abortedLongImagePush() {

    }

    public void longProcessImage(LongProcessAudioPushRequest pushRequest);

}
