package com.helospark.tactview.ui.javafx.uicomponents;

import com.helospark.tactview.core.timeline.TimelinePosition;

import javafx.scene.Node;

public class EffectDragInformation {
    private Node effectNode;
    private String clipId;
    private String effectId;
    private TimelinePosition originalPosition;
    private double anchorPointX;

    public EffectDragInformation(Node effectNode, String clipId, String effectId, TimelinePosition originalPosition, double anchorPointX) {
        this.effectNode = effectNode;
        this.clipId = clipId;
        this.effectId = effectId;
        this.originalPosition = originalPosition;
        this.anchorPointX = anchorPointX;
    }

    public Node getEffectNode() {
        return effectNode;
    }

    public String getClipId() {
        return clipId;
    }

    public String getEffectId() {
        return effectId;
    }

    public TimelinePosition getOriginalPosition() {
        return originalPosition;
    }

    public double getAnchorPointX() {
        return anchorPointX;
    }

}
