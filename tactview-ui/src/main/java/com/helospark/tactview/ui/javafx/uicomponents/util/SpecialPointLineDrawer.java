package com.helospark.tactview.ui.javafx.uicomponents.util;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.ClosesIntervalChannel;
import com.helospark.tactview.ui.javafx.uicomponents.MoveSpecialPointLineProperties;
import com.helospark.tactview.ui.javafx.uicomponents.TimelineState;

import javafx.scene.layout.HBox;

@Component
public class SpecialPointLineDrawer {
    private TimelineState timelineState;

    public SpecialPointLineDrawer(TimelineState timelineState) {
        this.timelineState = timelineState;
    }

    public void drawSpecialPointLineForEffect(ClosesIntervalChannel specialPosition, String originalClipId) {
        HBox specialPositionChannel = timelineState.findChannelById(specialPosition.getChannelId()).orElseThrow();
        HBox currentChannel = timelineState.findChannelForClip(originalClipId).orElseThrow();

        int lineStartX = timelineState.secondsToPixels(specialPosition.getSpecialPosition());
        int lineEndX = timelineState.secondsToPixels(specialPosition.getSpecialPosition());

        int lineStartY = (int) specialPositionChannel.getLayoutY();
        int lineEndY = (int) (currentChannel.getLayoutY() + currentChannel.getHeight());

        MoveSpecialPointLineProperties properties = timelineState.getMoveSpecialPointLineProperties();
        properties.setStartX(lineStartX);
        properties.setStartY(lineStartY);
        properties.setEndX(lineEndX);
        properties.setEndY(lineEndY);
        properties.setEnabledProperty(true);
    }

    public void drawSpecialPositionLineForClip(ClosesIntervalChannel specialPosition, String channelId) {
        HBox specialPositionChannel = timelineState.findChannelById(specialPosition.getChannelId()).orElseThrow();
        HBox currentChannel = timelineState.findChannelById(channelId).orElseThrow();

        int lineStartX = timelineState.secondsToPixels(specialPosition.getSpecialPosition());
        int lineEndX = timelineState.secondsToPixels(specialPosition.getSpecialPosition());

        int lineStartY = (int) specialPositionChannel.getLayoutY();
        int lineEndY = (int) (currentChannel.getLayoutY() + currentChannel.getHeight());

        MoveSpecialPointLineProperties properties = timelineState.getMoveSpecialPointLineProperties();
        properties.setStartX(lineStartX);
        properties.setStartY(lineStartY);
        properties.setEndX(lineEndX);
        properties.setEndY(lineEndY);
        properties.setEnabledProperty(true);
    }

}
