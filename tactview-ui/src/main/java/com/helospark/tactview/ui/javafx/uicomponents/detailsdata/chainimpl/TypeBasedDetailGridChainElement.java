package com.helospark.tactview.ui.javafx.uicomponents.detailsdata.chainimpl;

import java.util.Map;

import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.ui.javafx.uicomponents.detailsdata.DetailGridChainElement;

import javafx.scene.Node;

public abstract class TypeBasedDetailGridChainElement<T> implements DetailGridChainElement {
    private Class<T> clazz;

    public TypeBasedDetailGridChainElement(Class<T> clazz) {
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
