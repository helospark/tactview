package com.helospark.tactview.ui.javafx;

import java.util.Map;

import com.helospark.tactview.core.timeline.TimelineRenderResult.RegularRectangle;

import javafx.scene.image.Image;

public class JavaDisplayableAudioVideoFragment {
    private Image image;
    private byte[] data;
    private Map<String, RegularRectangle> clipRectangle;

    public JavaDisplayableAudioVideoFragment(Image image, byte[] data, Map<String, RegularRectangle> clipRectangle) {
        this.image = image;
        this.data = data;
        this.clipRectangle = clipRectangle;
    }

    public Image getImage() {
        return image;
    }

    public byte[] getAudioData() {
        return data;
    }

    public Map<String, RegularRectangle> getClipRectangle() {
        return clipRectangle;
    }
}
