package com.helospark.tactview.ui.javafx.uicomponents.detailsdata.clipchainimpl;

import java.util.Map;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.decoder.VideoMetadata;
import com.helospark.tactview.core.decoder.VisualMediaMetadata;
import com.helospark.tactview.core.timeline.VideoClip;

import javafx.scene.Node;
import javafx.scene.control.Label;

@Component
public class VideoClipDetailChainItem extends TypeBasedClipDetailGridChainElement<VideoClip> {

    public VideoClipDetailChainItem() {
        super(VideoClip.class);
    }

    @Override
    protected void updateMapInternal(Map<String, Node> mapToUpdate, VideoClip clip) {
        VisualMediaMetadata metadata = clip.getMediaMetadata();
        mapToUpdate.put("file", new Label(clip.getBackingSource().backingFile));
        mapToUpdate.put("info", createMediaInfo(metadata));
        if (metadata instanceof VideoMetadata) {
            mapToUpdate.put("fps", new Label(String.valueOf(((VideoMetadata) metadata).getFps())));
        }
    }

    private Node createMediaInfo(VisualMediaMetadata mediaMetadata) {
        return new Label(mediaMetadata.getWidth() + " × " + mediaMetadata.getHeight());
    }

}
