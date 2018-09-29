package com.helospark.tactview.core.timeline.message;

public class ChannelAddedMessage {
    private String channelId;
    private Integer index;

    public ChannelAddedMessage(String channelId, Integer index) {
        this.channelId = channelId;
        this.index = index;
    }

    public String getChannelId() {
        return channelId;
    }

    public Integer getIndex() {
        return index;
    }

}
