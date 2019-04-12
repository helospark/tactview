package com.helospark.tactview.ui.javafx;

import com.helospark.lightdi.annotation.Component;

@Component
public class UiPlaybackPreferenceRepository {
    private boolean mute;
    private boolean halfEffect;

    public boolean isMute() {
        return mute;
    }

    public void setMute(boolean mute) {
        this.mute = mute;
    }

    public boolean isHalfEffect() {
        return halfEffect;
    }

    public void setHalfEffect(boolean halfEffect) {
        this.halfEffect = halfEffect;
    }

}
