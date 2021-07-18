package com.helospark.tactview.ui.javafx.uicomponents.quicktoolbar.impl;

import static javafx.geometry.Orientation.VERTICAL;

import java.util.List;

import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;

import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.annotation.Order;
import com.helospark.tactview.core.timeline.LinkClipRepository;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.UiTimelineManager;
import com.helospark.tactview.ui.javafx.commands.impl.CutClipCommand;
import com.helospark.tactview.ui.javafx.uicomponents.quicktoolbar.QuickToolbarMenuElement;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.Tooltip;

@Component
@Order(20)
public class CutQuickToolbarElement implements QuickToolbarMenuElement {
    private UiCommandInterpreterService commandInterpreter;
    private TimelineManagerAccessor timelineManager;
    private UiTimelineManager uiTimelineManager;
    private LinkClipRepository linkClipRepository;

    public CutQuickToolbarElement(UiCommandInterpreterService commandInterpreter, TimelineManagerAccessor timelineManager, UiTimelineManager uiTimelineManager, LinkClipRepository linkClipRepository) {
        this.commandInterpreter = commandInterpreter;
        this.timelineManager = timelineManager;
        this.uiTimelineManager = uiTimelineManager;
        this.linkClipRepository = linkClipRepository;
    }

    @Override
    public List<Node> getQuickMenuBarElements() {
        Button cutAllClipsButton = new Button("", new Glyph("FontAwesome", FontAwesome.Glyph.CUT));
        cutAllClipsButton.setTooltip(new Tooltip("Cut all clips at cursor position"));

        cutAllClipsButton.setOnMouseClicked(event -> {
            TimelinePosition currentPosition = uiTimelineManager.getCurrentPosition();
            List<String> intersectingClips = timelineManager.findIntersectingClips(currentPosition);

            if (intersectingClips.size() > 0) {
                CutClipCommand command = CutClipCommand.builder()
                        .withClipIds(intersectingClips)
                        .withGlobalTimelinePosition(currentPosition)
                        .withLinkedClipRepository(linkClipRepository)
                        .withTimelineManager(timelineManager)
                        .build();
                commandInterpreter.sendWithResult(command);
            }
        });
        return List.of(cutAllClipsButton, new Separator(VERTICAL));
    }

}
