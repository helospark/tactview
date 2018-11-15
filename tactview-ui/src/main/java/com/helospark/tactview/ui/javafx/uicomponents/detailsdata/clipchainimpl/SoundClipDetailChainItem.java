package com.helospark.tactview.ui.javafx.uicomponents.detailsdata.clipchainimpl;

import java.util.Map;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.SoundClip;

import javafx.scene.Node;
import javafx.scene.control.Label;

@Component
public class SoundClipDetailChainItem extends TypeBasedClipDetailGridChainElement<SoundClip> {

    public SoundClipDetailChainItem() {
        super(SoundClip.class);
    }

    @Override
    protected void updateMapInternal(Map<String, Node> mapToUpdate, SoundClip clip) {
        mapToUpdate.put("file", new Label(clip.getBackingSource().backingFile));
        mapToUpdate.put("samples", new Label(String.valueOf(clip.getMediaMetadata().getSampleRate()) + " / second"));
        mapToUpdate.put("bits", new Label(String.valueOf(clip.getMediaMetadata().getBytesPerSample() * 8)));
        mapToUpdate.put("channels", new Label(String.valueOf(clip.getMediaMetadata().getChannels())));
    }

}
