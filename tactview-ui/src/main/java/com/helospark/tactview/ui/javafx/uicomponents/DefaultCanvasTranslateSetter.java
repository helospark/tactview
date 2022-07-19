package com.helospark.tactview.ui.javafx.uicomponents;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.ui.javafx.CanvasStateHolder;

@Component
public class DefaultCanvasTranslateSetter {

    public void setDefaultCanvasTranslate(CanvasStateHolder canvasStateHolder, int previewWidth, int previewHeight) {
        double canvasWidth = canvasStateHolder.getCanvas().getWidth();
        double canvasHeight = canvasStateHolder.getCanvas().getHeight();
        if (canvasWidth > previewWidth) {
            canvasStateHolder.setTranslateX((canvasWidth - previewWidth) / 2.0);
        } else {
            canvasStateHolder.setTranslateX(0.0);
        }
        if (canvasHeight > previewHeight) {
            canvasStateHolder.setTranslateY((canvasHeight - previewHeight) / 2.0);
        } else {
            canvasStateHolder.setTranslateY(0.0);
        }
    }

}
