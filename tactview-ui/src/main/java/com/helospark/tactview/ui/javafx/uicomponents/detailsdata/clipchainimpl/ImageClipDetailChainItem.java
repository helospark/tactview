package com.helospark.tactview.ui.javafx.uicomponents.detailsdata.clipchainimpl;

import java.util.Map;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.decoder.VisualMediaMetadata;
import com.helospark.tactview.core.timeline.clipfactory.ImageClip;

import javafx.scene.Node;
import javafx.scene.control.Label;

@Component
public class ImageClipDetailChainItem extends TypeBasedClipDetailGridChainElement<ImageClip> {

    public ImageClipDetailChainItem() {
        super(ImageClip.class);
    }

    @Override
    protected void updateMapInternal(Map<String, Node> mapToUpdate, ImageClip clip) {
        mapToUpdate.put("file", new Label(clip.getMediaSource().backingFile));
        mapToUpdate.put("info", createMediaInfo(clip.getMediaMetadata()));
    }

    private Node createMediaInfo(VisualMediaMetadata visualMediaMetadata) {
        return new Label(visualMediaMetadata.getWidth() + " × " + visualMediaMetadata.getHeight());
    }

}
