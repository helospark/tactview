package com.helospark.tactview.api.commands;

import com.helospark.tactview.api.UiCommand;

public class VideoClipAddedCommand implements UiCommand {
    private String fileLocation;
    private long time;

    public VideoClipAddedCommand(String fileLocation, long time) {
        this.fileLocation = fileLocation;
        this.time = time;
    }


}
