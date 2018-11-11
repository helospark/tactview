package com.helospark.tactview.ui.javafx;

import javafx.scene.image.Image;

public class JavaDisplayableAudioVideoFragment {
    private Image image;
    private byte[] data;

    public JavaDisplayableAudioVideoFragment(Image image, byte[] data) {
        this.image = image;
        this.data = data;
    }

    public Image getImage() {
        return image;
    }

    public byte[] getData() {
        return data;
    }

}
