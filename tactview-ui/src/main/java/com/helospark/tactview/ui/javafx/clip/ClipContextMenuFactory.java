package com.helospark.tactview.ui.javafx.clip;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineManager;
import com.helospark.tactview.ui.javafx.clip.chain.ClipContextMenuChainItem;
import com.helospark.tactview.ui.javafx.clip.chain.ClipContextMenuChainItemRequest;
import com.helospark.tactview.ui.javafx.repository.SelectedNodeRepository;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

@Component
public class ClipContextMenuFactory {
    private List<ClipContextMenuChainItem> chainItems;
    private SelectedNodeRepository selectedNodeRepository;
    private TimelineManager timelineManager;

    public ClipContextMenuFactory(List<ClipContextMenuChainItem> chainItems, SelectedNodeRepository selectedNodeRepository, TimelineManager timelineManager) {
        this.chainItems = chainItems;
        this.selectedNodeRepository = selectedNodeRepository;
        this.timelineManager = timelineManager;
    }

    public Optional<ContextMenu> createContextMenuForSelectedClips() {
        Optional<TimelineClip> primarySelectedClip = selectedNodeRepository.getPrimarySelectedClipId()
                .flatMap(id -> timelineManager.findClipById(id));

        if (!primarySelectedClip.isPresent()) {
            return Optional.empty();
        }

        TimelineClip primaryClip = primarySelectedClip.get();

        List<TimelineClip> allClips = selectedNodeRepository.getSelectedClipIds()
                .stream()
                .flatMap(id -> timelineManager.findClipById(id).stream())
                .collect(Collectors.toList());

        ClipContextMenuChainItemRequest request = new ClipContextMenuChainItemRequest(primaryClip, allClips);
        List<MenuItem> items = chainItems.stream()
                .filter(item -> item.supports(request))
                .map(item -> item.createMenu(request))
                .collect(Collectors.toList());

        ContextMenu contextMenu = new ContextMenu();
        contextMenu.getItems().addAll(items);

        return Optional.of(contextMenu);
    }

}
