package com.helospark.tactview.ui.javafx.uicomponents.detailsdata.effectchainimpl;

import java.util.Map;

import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.ui.javafx.uicomponents.detailsdata.ClipDetailGridChainElement;

import javafx.scene.Node;

public abstract class TypeBasedEffectDetailGridChainElement<T> implements ClipDetailGridChainElement {
    private Class<T> clazz;

    public TypeBasedEffectDetailGridChainElement(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public void updateMap(Map<String, Node> mapToUpdate, TimelineClip clip) {
        updateMapInternal(mapToUpdate, (T) clip);
    }

    protected abstract void updateMapInternal(Map<String, Node> mapToUpdate, T clip);

    @Override
    public boolean supports(TimelineClip clip) {
        return clip.getClass().isAssignableFrom(clazz);
    }

}
