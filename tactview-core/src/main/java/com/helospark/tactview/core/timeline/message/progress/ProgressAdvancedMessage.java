package com.helospark.tactview.core.timeline.message.progress;

public class ProgressAdvancedMessage {
    private String id;
    private int numberOfJobsDone;

    public ProgressAdvancedMessage(String id, int numberOfJobsDone) {
        this.id = id;
        this.numberOfJobsDone = numberOfJobsDone;
    }

    public String getId() {
        return id;
    }

    public int getNumberOfJobsDone() {
        return numberOfJobsDone;
    }

}
