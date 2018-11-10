package com.helospark.tactview.ui.javafx.uicomponents.detailsdata;

import java.util.Map;

import com.helospark.tactview.core.timeline.StatelessEffect;

import javafx.scene.Node;

public interface EffectDetailGridChainElement {

    public void updateMap(Map<String, Node> mapToUpdate, StatelessEffect clip);

    public boolean supports(StatelessEffect clip);

}
