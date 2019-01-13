package com.helospark.tactview.core.timeline.message;

public class ChannelAddedMessage {
    private String channelId;
    private Integer index;
    private boolean disabled;
    private boolean mute;

    public ChannelAddedMessage(String channelId, Integer index, boolean disabled, boolean mute) {
        this.channelId = channelId;
        this.index = index;
        this.disabled = disabled;
        this.mute = mute;
    }

    public String getChannelId() {
        return channelId;
    }

    public Integer getIndex() {
        return index;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public boolean isMute() {
        return mute;
    }

    @Override
    public String toString() {
        return "ChannelAddedMessage [channelId=" + channelId + ", index=" + index + ", disabled=" + disabled + ", mute=" + mute + "]";
    }

}
