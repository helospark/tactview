package com.helospark.tactview.ui.javafx.uicomponents.quicktoolbar.impl;

import static com.helospark.tactview.ui.javafx.commands.impl.CreateChannelCommand.LAST_INDEX;
import static javafx.geometry.Orientation.VERTICAL;

import java.util.List;

import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;

import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.annotation.Order;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.commands.impl.CreateChannelCommand;
import com.helospark.tactview.ui.javafx.uicomponents.quicktoolbar.QuickToolbarMenuElement;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.Tooltip;

@Component
@Order(10)
public class NewChannelQuickToolbarElement implements QuickToolbarMenuElement {
    private UiCommandInterpreterService commandInterpreter;
    private TimelineManagerAccessor timelineManager;

    public NewChannelQuickToolbarElement(UiCommandInterpreterService commandInterpreter, TimelineManagerAccessor timelineManager) {
        this.commandInterpreter = commandInterpreter;
        this.timelineManager = timelineManager;
    }

    @Override
    public List<Node> getQuickMenuBarElements() {
        Button addChannelButton = new Button("Channel", new Glyph("FontAwesome", FontAwesome.Glyph.PLUS));
        addChannelButton.setTooltip(new Tooltip("Add new channel"));
        addChannelButton.setOnMouseClicked(event -> {
            commandInterpreter.sendWithResult(new CreateChannelCommand(timelineManager, LAST_INDEX));
        });
        return List.of(addChannelButton, new Separator(VERTICAL));
    }

}
