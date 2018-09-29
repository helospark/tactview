package com.helospark.tactview.core.timeline.message;

public class ChannelRemovedMessage {
    private String channelId;

    public ChannelRemovedMessage(String channelId) {
        this.channelId = channelId;
    }

    public String getChannelId() {
        return channelId;
    }

}
