package com.helospark.tactview.core.timeline.blendmode;

public enum BlendMode {
    NORMAL("normal", new NormalBlendModeStrategy()),
    OVERLAY("overlay", new OverlayBlendModeStrategy()),
    SCREEN("screen", new ScreenBlendModeStrategy());

    String id;
    BlendModeStrategy strategy;

    private BlendMode(String id, BlendModeStrategy strategy) {
        this.id = id;
        this.strategy = strategy;
    }

    public BlendModeStrategy getStrategy() {
        return strategy;
    }

    public String getId() {
        return id;
    }

}
