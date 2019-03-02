package com.helospark.tactview.core.timeline.clipfactory.sequence;

import java.io.File;

public class FileHolder implements Comparable<FileHolder> {
    File file;
    int frameIndex;

    public FileHolder(File file, int frameIndex) {
        this.file = file;
        this.frameIndex = frameIndex;
    }

    @Override
    public int compareTo(FileHolder other) {
        return Integer.compare(frameIndex, other.frameIndex);
    }

    public File getFile() {
        return file;
    }

    public int getFrameIndex() {
        return frameIndex;
    }

}