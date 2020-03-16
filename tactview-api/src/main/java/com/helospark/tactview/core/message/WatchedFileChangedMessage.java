package com.helospark.tactview.core.message;

import java.io.File;

public class WatchedFileChangedMessage {
    private File changedFile;

    public WatchedFileChangedMessage(File changedFile) {
        this.changedFile = changedFile;
    }

}
