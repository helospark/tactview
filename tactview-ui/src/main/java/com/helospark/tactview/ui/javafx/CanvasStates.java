package com.helospark.tactview.ui.javafx;

import java.util.HashMap;
import java.util.Map;

import com.helospark.lightdi.annotation.Component;

@Component
public class CanvasStates {
    private Map<String, CanvasStateHolder> canvases = new HashMap<>();
    private String activeCanvas;

    public void registerCanvas(String id, CanvasStateHolder canvas) {
        canvases.put(id, canvas);
        if (activeCanvas == null) {
            activeCanvas = id;
        }
    }

    public CanvasStateHolder getActiveCanvas() {
        return canvases.get(activeCanvas);
    }

    public CanvasStateHolder getCanvas(String id) {
        return canvases.get(id);
    }

    public void setActiveCanvas(String id) {
        this.activeCanvas = id;
    }

}
