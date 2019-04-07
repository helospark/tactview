package com.helospark.tactview.ui.javafx;

import static com.helospark.tactview.ui.javafx.commands.impl.CreateChannelCommand.LAST_INDEX;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.save.DirtyRepository;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.ui.javafx.commands.impl.CreateChannelCommand;

@Component
public class UiInitializer {
    private UiCommandInterpreterService commandInterpreter;
    private TimelineManagerAccessor timelineManager;
    private DirtyRepository dirtyRepository;

    public UiInitializer(UiCommandInterpreterService commandInterpreter, TimelineManagerAccessor timelineManager, DirtyRepository dirtyRepository) {
        this.commandInterpreter = commandInterpreter;
        this.timelineManager = timelineManager;
        this.dirtyRepository = dirtyRepository;
    }

    public void initialize() {
        try {
            for (int i = 0; i < 8; ++i) {
                commandInterpreter.sendWithResult(new CreateChannelCommand(timelineManager, LAST_INDEX)).get();
            }

            dirtyRepository.setDirty(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
