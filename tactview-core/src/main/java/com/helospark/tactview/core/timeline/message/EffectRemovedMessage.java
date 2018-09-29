package com.helospark.tactview.core.timeline.message;

public class EffectRemovedMessage {
    private String effectId;
    private String clipId;

    public EffectRemovedMessage(String effectId, String clipId) {
        this.effectId = effectId;
        this.clipId = clipId;
    }

    public String getEffectId() {
        return effectId;
    }

    public String getClipId() {
        return clipId;
    }

}
