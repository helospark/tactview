package com.helospark.tactview.ui.javafx.uicomponents.util;

import java.util.Optional;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.ClosesIntervalChannel;
import com.helospark.tactview.ui.javafx.uicomponents.TimelineLineProperties;
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

        double lineStartX = timelineState.secondsToPixels(specialPosition.getSpecialPosition());
        double lineEndX = timelineState.secondsToPixels(specialPosition.getSpecialPosition());

        int lineStartY = (int) specialPositionChannel.getLayoutY();
        int lineEndY = (int) (currentChannel.getLayoutY() + currentChannel.getHeight());

        TimelineLineProperties properties = timelineState.getMoveSpecialPointLineProperties();
        properties.setStartX(lineStartX);
        properties.setStartY(lineStartY);
        properties.setEndX(lineEndX);
        properties.setEndY(lineEndY);
        properties.setEnabledProperty(true);
    }

    public void drawSpecialPositionLineForClip(ClosesIntervalChannel specialPosition, String channelId) {
        Optional<Integer> optionalSpecialChannelIndex = timelineState.findChannelIndex(specialPosition.getChannelId());
        Optional<Integer> optionalCurrentChannelIndex = timelineState.findChannelIndex(channelId);

        if (optionalSpecialChannelIndex.isEmpty() || optionalCurrentChannelIndex.isEmpty()) {
            return;
        }
        int specialChannelIndex = optionalSpecialChannelIndex.get();
        int currentChannelIndex = optionalCurrentChannelIndex.get();

        int upperIndex = Math.min(specialChannelIndex, currentChannelIndex);
        int lowerIndex = Math.max(specialChannelIndex, currentChannelIndex);

        HBox specialPositionChannel = timelineState.getChannels().get(upperIndex);
        HBox currentChannel = timelineState.getChannels().get(lowerIndex);

        double lineStartX = timelineState.secondsToPixels(specialPosition.getSpecialPosition());
        double lineEndX = timelineState.secondsToPixels(specialPosition.getSpecialPosition());

        int lineStartY = (int) (specialPositionChannel.getLayoutY());
        int lineEndY = (int) (currentChannel.getLayoutY() + currentChannel.getHeight());

        TimelineLineProperties properties = timelineState.getMoveSpecialPointLineProperties();
        properties.setStartX(lineStartX);
        properties.setStartY(lineStartY);
        properties.setEndX(lineEndX);
        properties.setEndY(lineEndY);
        properties.setEnabledProperty(true);
    }

}
