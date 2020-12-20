package com.helospark.tactview.ui.javafx.uicomponents;

import javafx.scene.image.Image;

public class ImageReferenceHolder {
    public static final Image TRANSPARENT_5x5 = loadImage("/icons/transparent_5x5.png");

    private static Image loadImage(String path) {
        try {
            return new Image(ImageReferenceHolder.class.getResourceAsStream(path));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
