package com.helospark.tactview.ui.javafx.uicomponents.detailsdata.clipchainimpl;

import java.util.Map;

import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.ui.javafx.uicomponents.detailsdata.ClipDetailGridChainElement;

import javafx.scene.Node;

public abstract class TypeBasedClipDetailGridChainElement<T> implements ClipDetailGridChainElement {
    private Class<T> clazz;

    public TypeBasedClipDetailGridChainElement(Class<T> clazz) {
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
