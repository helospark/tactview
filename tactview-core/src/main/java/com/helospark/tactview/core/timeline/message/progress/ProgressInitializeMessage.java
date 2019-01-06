package com.helospark.tactview.core.timeline.message.progress;

public class ProgressInitializeMessage {
    private String id;
    private int allJobs;

    public ProgressInitializeMessage(String id, int allJobs) {
        this.id = id;
        this.allJobs = allJobs;
    }

    public String getId() {
        return id;
    }

    public int getAllJobs() {
        return allJobs;
    }

}
