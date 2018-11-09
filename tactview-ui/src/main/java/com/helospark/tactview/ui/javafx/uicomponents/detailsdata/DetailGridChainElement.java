package com.helospark.tactview.ui.javafx.uicomponents.detailsdata;

import java.util.Map;

import com.helospark.tactview.core.timeline.TimelineClip;

import javafx.scene.Node;

public interface DetailGridChainElement {

    public void updateMap(Map<String, Node> mapToUpdate, TimelineClip clip);

    public boolean supports(TimelineClip clip);

}
