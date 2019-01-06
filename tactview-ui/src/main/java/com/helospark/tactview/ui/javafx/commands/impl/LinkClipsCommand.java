package com.helospark.tactview.ui.javafx.commands.impl;

import java.util.List;

import com.helospark.tactview.core.timeline.LinkClipRepository;
import com.helospark.tactview.ui.javafx.commands.UiCommand;

public class LinkClipsCommand implements UiCommand {
    private LinkClipRepository linkClipRepository;

    private List<String> linkedClipIds;

    public LinkClipsCommand(LinkClipRepository linkClipRepository, List<String> linkedClipIds) {
        this.linkClipRepository = linkClipRepository;
        this.linkedClipIds = linkedClipIds;
    }

    @Override
    public void execute() {
        linkClipRepository.linkClips(linkedClipIds);
    }

    @Override
    public void revert() {
        linkClipRepository.unlinkClips(linkedClipIds);
    }

}
