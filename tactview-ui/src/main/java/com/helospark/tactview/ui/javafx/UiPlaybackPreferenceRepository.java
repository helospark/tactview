package com.helospark.tactview.ui.javafx;

import com.helospark.lightdi.annotation.Component;

@Component
public class UiPlaybackPreferenceRepository {
    private boolean mute;

    public boolean isMute() {
        return mute;
    }

    public void setMute(boolean mute) {
        this.mute = mute;
    }

}
