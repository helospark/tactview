package com.helospark.tactview.core.timeline.longprocess;

import java.util.Optional;

public class LongProcessDescriptor {
    public String jobId;
    public String clipId;
    public Optional<String> effectId;
    public Runnable runnable;
    private boolean aborted;

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public void setClipId(String clipId) {
        this.clipId = clipId;
    }

    public void setEffectId(Optional<String> effectId) {
        this.effectId = effectId;
    }

    public void setRunnable(Runnable runnable) {
        this.runnable = runnable;
    }

    public void setAborted(boolean aborted) {
        this.aborted = aborted;
    }

    public boolean isAborted() {
        return aborted;
    }

}
