package com.helospark.tactview.core.timeline.longprocess;

public interface LongProcessVisualImagePushAware {

    public default void beginToPushLongImages() {

    }

    public default void endToPushLongImages() {

    }

    public void longProcessImage(LongProcessImagePushRequest pushRequest);

}
