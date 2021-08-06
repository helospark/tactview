package com.helospark.tactview.ui.javafx.menu.defaultmenus.subtimeline;

import java.util.Optional;
import java.util.Set;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.timeline.subtimeline.ExposedDescriptorDescriptor;
import com.helospark.tactview.ui.javafx.repository.NameToIdRepository;
import com.helospark.tactview.ui.javafx.stylesheet.StylesheetAdderService;

@Component
public class SubtimelineSelectWindowOpener {
    private TimelineManagerAccessor timelineManagerAccessor;
    private NameToIdRepository nameToIdRepository;
    private StylesheetAdderService stylesheetAdderService;

    public SubtimelineSelectWindowOpener(TimelineManagerAccessor timelineManagerAccessor, NameToIdRepository nameToIdRepository, StylesheetAdderService stylesheetAdderService) {
        this.timelineManagerAccessor = timelineManagerAccessor;
        this.nameToIdRepository = nameToIdRepository;
        this.stylesheetAdderService = stylesheetAdderService;
    }

    public Optional<Set<ExposedDescriptorDescriptor>> openWindow() {
        SubtimelineSelectWindow window = new SubtimelineSelectWindow(timelineManagerAccessor, nameToIdRepository, stylesheetAdderService);
        Set<ExposedDescriptorDescriptor> result = window.open();
        if (window.isSuccessful()) {
            return Optional.of(result);
        } else {
            return Optional.empty();
        }
    }

}
