package com.helospark.tactview.ui.javafx.clip.chain;

import java.util.List;
import java.util.stream.Collectors;

import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.annotation.Order;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.ui.javafx.repository.NameToIdRepository;
import com.helospark.tactview.ui.javafx.uicomponents.window.ProjectMediaWindow;

import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;

@Component
@Order(93)
public class AddToProjectMediaMenuChainItem implements ClipContextMenuChainItem {
    private ProjectMediaWindow projectMediaWindow;
    private NameToIdRepository nameToIdRepository;
    private TimelineManagerAccessor timelineManagerAccessor;

    public AddToProjectMediaMenuChainItem(ProjectMediaWindow projectMediaWindow, NameToIdRepository nameToIdRepository,
            TimelineManagerAccessor timelineManagerAccessor) {
        this.projectMediaWindow = projectMediaWindow;
        this.nameToIdRepository = nameToIdRepository;
        this.timelineManagerAccessor = timelineManagerAccessor;
    }

    @Override
    public MenuItem createMenu(ClipContextMenuChainItemRequest request) {
        Menu menu = new Menu("Project media");
        String name = getName(request);

        MenuItem addClipItem = new MenuItem("Add clip(s) and linked");
        addClipItem.setOnAction(e -> {
            List<TimelineClip> allClips = timelineManagerAccessor.resolveClipsWithAllLinkedClip(request.getAllClips())
                    .stream()
                    .map(clip -> clip.cloneClip(CloneRequestMetadata.ofDefault()))
                    .collect(Collectors.toList());
            projectMediaWindow.addClips(allClips, name);
        });

        MenuItem addJustCurrentClipItem = new MenuItem("Add just current clip");
        TimelineClip clonedClip = request.getPrimaryClip().cloneClip(CloneRequestMetadata.ofDefault());
        addJustCurrentClipItem.setOnAction(e -> projectMediaWindow.addClips(List.of(clonedClip), name));

        menu.getItems().add(addClipItem);
        menu.getItems().add(addJustCurrentClipItem);

        return menu;
    }

    private String getName(ClipContextMenuChainItemRequest request) {
        String name = nameToIdRepository.getNameForId(request.getPrimaryClip().getId());
        if (name == null || name.equals("")) {
            return request.getPrimaryClip().getId();
        } else {
            return name;
        }
    }

    @Override
    public boolean supports(ClipContextMenuChainItemRequest request) {
        return true;
    }

}
