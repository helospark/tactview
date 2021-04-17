package com.helospark.tactview.ui.javafx;

import java.math.BigDecimal;

import com.helospark.lightdi.annotation.Component;

@Component
public class UiPlaybackPreferenceRepository {
    private boolean mute;
    private boolean halfEffect;
    private BigDecimal playbackSpeedMultiplier = BigDecimal.ONE;

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

    public BigDecimal getPlaybackSpeedMultiplier() {
        return playbackSpeedMultiplier;
    }

    public void setPlaybackSpeedMultiplier(BigDecimal playbackSpeedMultiplier) {
        this.playbackSpeedMultiplier = playbackSpeedMultiplier;
    }

}
