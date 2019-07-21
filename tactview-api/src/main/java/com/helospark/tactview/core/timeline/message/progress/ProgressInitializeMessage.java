package com.helospark.tactview.core.timeline.message.progress;

public class ProgressInitializeMessage {
    private String id;
    private int allJobs;
    private ProgressType type;

    public ProgressInitializeMessage(String id, int allJobs) {
        this.id = id;
        this.allJobs = allJobs;
        this.type = ProgressType.RENDER;
    }

    public ProgressInitializeMessage(String id, int allJobs, ProgressType type) {
        this.id = id;
        this.allJobs = allJobs;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public int getAllJobs() {
        return allJobs;
    }

    public ProgressType getType() {
        return type;
    }

    @Override
    public String toString() {
        return "ProgressInitializeMessage [id=" + id + ", allJobs=" + allJobs + "]";
    }

}
