package com.helospark.tactview.ui.javafx;

import static com.helospark.tactview.ui.javafx.commands.impl.CreateChannelCommand.LAST_INDEX;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.TimelineManager;
import com.helospark.tactview.ui.javafx.commands.impl.CreateChannelCommand;

@Component
public class UiInitializer {
    private UiCommandInterpreterService commandInterpreter;
    private TimelineManager timelineManager;

    public UiInitializer(UiCommandInterpreterService commandInterpreter, TimelineManager timelineManager) {
        this.commandInterpreter = commandInterpreter;
        this.timelineManager = timelineManager;
    }

    public void initialize() {
        try {
            for (int i = 0; i < 3; ++i) {
                commandInterpreter.sendWithResult(new CreateChannelCommand(timelineManager, LAST_INDEX)).get();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
