package com.helospark.tactview.ui.javafx.clip.chain;

import java.util.List;
import java.util.stream.Collectors;

import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.annotation.Order;
import com.helospark.tactview.core.timeline.LinkClipRepository;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.commands.impl.LinkClipsCommand;

import javafx.scene.control.MenuItem;

@Component
@Order(90)
public class LinkClipsChainItem implements ClipContextMenuChainItem {
    private LinkClipRepository linkClipRepository;
    private UiCommandInterpreterService commandInterpreter;

    public LinkClipsChainItem(LinkClipRepository linkClipRepository, UiCommandInterpreterService commandInterpreter) {
        this.linkClipRepository = linkClipRepository;
        this.commandInterpreter = commandInterpreter;
    }

    @Override
    public MenuItem createMenu(ClipContextMenuChainItemRequest request) {
        List<String> linkedClipIds = request.getAllClips()
                .stream()
                .map(clip -> clip.getId())
                .collect(Collectors.toList());

        MenuItem linkClipsMenuItem = new MenuItem("Link");
        linkClipsMenuItem.setOnAction(e -> {
            LinkClipsCommand linkClipsCommand = new LinkClipsCommand(linkClipRepository, linkedClipIds);

            commandInterpreter.sendWithResult(linkClipsCommand);
        });

        return linkClipsMenuItem;
    }

    @Override
    public boolean supports(ClipContextMenuChainItemRequest request) {
        return request.getAllClips().size() >= 2;
    }

}
