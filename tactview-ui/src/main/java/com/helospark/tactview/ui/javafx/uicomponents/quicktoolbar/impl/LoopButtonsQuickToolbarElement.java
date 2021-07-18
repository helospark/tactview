package com.helospark.tactview.ui.javafx.uicomponents.quicktoolbar.impl;

import static javafx.geometry.Orientation.VERTICAL;

import java.util.List;

import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;

import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.annotation.Order;
import com.helospark.tactview.ui.javafx.UiTimelineManager;
import com.helospark.tactview.ui.javafx.uicomponents.TimelineState;
import com.helospark.tactview.ui.javafx.uicomponents.quicktoolbar.QuickToolbarMenuElement;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.Tooltip;

@Component
@Order(30)
public class LoopButtonsQuickToolbarElement implements QuickToolbarMenuElement {
    private static final String LOOP_BUTTON_ENABLED_CLASS = "loop-button-enabled";
    private static final String LOOP_BUTTON_CLASS = "loop-button";
    private UiTimelineManager uiTimelineManager;
    private TimelineState timelineState;

    public LoopButtonsQuickToolbarElement(UiTimelineManager uiTimelineManager, TimelineState timelineState) {
        this.uiTimelineManager = uiTimelineManager;
        this.timelineState = timelineState;
    }

    @Override
    public List<Node> getQuickMenuBarElements() {

        Button eraserMarkerButton = new Button("", new Glyph("FontAwesome", FontAwesome.Glyph.ERASER));
        eraserMarkerButton.setTooltip(new Tooltip("Erase loop markers"));
        eraserMarkerButton.disableProperty().set(true);
        eraserMarkerButton.getStyleClass().add(LOOP_BUTTON_CLASS);
        eraserMarkerButton.setOnMouseClicked(event -> {
            timelineState.setLoopBProperties(null);
            timelineState.setLoopAProperties(null);
            disableClearMarker(eraserMarkerButton);
        });

        Button addAMarkerButton = new Button("A", new Glyph("FontAwesome", FontAwesome.Glyph.RETWEET));
        addAMarkerButton.setTooltip(new Tooltip("Loop start time"));
        addAMarkerButton.getStyleClass().add(LOOP_BUTTON_CLASS);
        addAMarkerButton.setOnAction(event -> {
            if (timelineState.getLoopBLineProperties().isPresent() && timelineState.getLoopBLineProperties().get().isLessOrEqualToThan(uiTimelineManager.getCurrentPosition())) {
                timelineState.setLoopBProperties(null);
            }
            timelineState.setLoopAProperties(uiTimelineManager.getCurrentPosition());
            enableClearMarker(eraserMarkerButton);
        });

        Button addBMarkerButton = new Button("B", new Glyph("FontAwesome", FontAwesome.Glyph.RETWEET));
        addBMarkerButton.setTooltip(new Tooltip("Loop end time"));
        addBMarkerButton.getStyleClass().add(LOOP_BUTTON_CLASS);
        addBMarkerButton.setOnMouseClicked(event -> {
            if (timelineState.getLoopALineProperties().isPresent() && timelineState.getLoopALineProperties().get().isGreaterOrEqualToThan(uiTimelineManager.getCurrentPosition())) {
                timelineState.setLoopAProperties(null);
            }
            timelineState.setLoopBProperties(uiTimelineManager.getCurrentPosition());
            enableClearMarker(eraserMarkerButton);
        });

        return List.of(addAMarkerButton, addBMarkerButton, eraserMarkerButton, new Separator(VERTICAL));
    }

    private void disableClearMarker(Button eraserMarkerButton) {
        eraserMarkerButton.getStyleClass().remove(LOOP_BUTTON_ENABLED_CLASS);
        eraserMarkerButton.disableProperty().set(true);
    }

    private void enableClearMarker(Button eraserMarkerButton) {
        eraserMarkerButton.getStyleClass().add(LOOP_BUTTON_ENABLED_CLASS);
        eraserMarkerButton.disableProperty().set(false);
    }

}
