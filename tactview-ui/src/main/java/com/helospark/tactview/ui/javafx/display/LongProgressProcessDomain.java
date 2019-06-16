package com.helospark.tactview.ui.javafx.display;

public class LongProgressProcessDomain {
    int allJobs;
    int finishedJobs;
    int lastDisplayUpdatePercent;

    public LongProgressProcessDomain(int allJobs) {
        this.allJobs = allJobs;
        this.finishedJobs = 0;
        this.lastDisplayUpdatePercent = 0;
    }

}
