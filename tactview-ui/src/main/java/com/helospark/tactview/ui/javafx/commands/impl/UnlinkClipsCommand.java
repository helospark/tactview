package com.helospark.tactview.ui.javafx.commands.impl;

import java.util.List;

import com.helospark.tactview.core.timeline.LinkClipRepository;
import com.helospark.tactview.ui.javafx.commands.UiCommand;

public class UnlinkClipsCommand implements UiCommand {
    private LinkClipRepository linkClipRepository;

    private List<String> selectedClips;

    private List<List<String>> originallyLinked;

    public UnlinkClipsCommand(LinkClipRepository linkClipRepository, List<String> selectedClips) {
        this.linkClipRepository = linkClipRepository;
        this.selectedClips = selectedClips;
    }

    @Override
    public void execute() {
        linkClipRepository.unlinkAllConnectedClips(selectedClips);
    }

    @Override
    public void revert() {
        for (var entry : originallyLinked) {
            linkClipRepository.linkClips(entry);
        }
    }

}
