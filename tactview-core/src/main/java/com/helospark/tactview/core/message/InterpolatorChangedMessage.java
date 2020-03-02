package com.helospark.tactview.core.message;

public class InterpolatorChangedMessage {
    private String descriptorId;

    public InterpolatorChangedMessage(String descriptorId) {
        this.descriptorId = descriptorId;
    }

    public String getDescriptorId() {
        return descriptorId;
    }

}
