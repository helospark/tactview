package com.helospark.tactview.ui.javafx.uicomponents.canvasdraw.handlers;

import java.util.Optional;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.ui.javafx.clip.ClipContextMenuFactory;
import com.helospark.tactview.ui.javafx.effect.EffectContextMenuFactory;
import com.helospark.tactview.ui.javafx.repository.SelectedNodeRepository;
import com.helospark.tactview.ui.javafx.uicomponents.canvasdraw.domain.TimelineUiCacheElement;
import com.helospark.tactview.ui.javafx.uicomponents.canvasdraw.domain.TimelineUiCacheType;

import javafx.scene.control.ContextMenu;
import javafx.scene.input.ContextMenuEvent;
import javafx.stage.Window;

@Component
public class TimelineCanvasContextMenuRequestedHandler {
    private SelectedNodeRepository selectedNodeRepository;
    private ClipContextMenuFactory clipContextMenuFactory;
    private TimelineManagerAccessor timelineAccessor;
    private EffectContextMenuFactory effectContextMenuFactory;

    public TimelineCanvasContextMenuRequestedHandler(SelectedNodeRepository selectedNodeRepository, ClipContextMenuFactory clipContextMenuFactory, TimelineManagerAccessor timelineAccessor,
            EffectContextMenuFactory effectContextMenuFactory) {
        this.selectedNodeRepository = selectedNodeRepository;
        this.clipContextMenuFactory = clipContextMenuFactory;
        this.timelineAccessor = timelineAccessor;
        this.effectContextMenuFactory = effectContextMenuFactory;
    }

    public void onContextMenuRequested(TimelineCanvasContextMenuRequestedHandlerRequest request) {
        var optionalElement = request.selectedElement;
        var event = request.event;
        var parentWindow = request.parentWindow;
        if (optionalElement.isPresent()) {
            TimelineUiCacheElement element = optionalElement.get();
            if (element.elementType.equals(TimelineUiCacheType.CLIP)) {
                if (selectedNodeRepository.getPrimarySelectedClip().isEmpty()) {
                    selectedNodeRepository.setOnlySelectedClip(element.elementId);
                }
                Optional<ContextMenu> contextMenu = clipContextMenuFactory.createContextMenuForSelectedClips();
                if (contextMenu.isPresent()) {
                    contextMenu.get().show(parentWindow, event.getScreenX(), event.getScreenY());
                    event.consume();
                }
            } else {
                if (selectedNodeRepository.getPrimarySelectedClip().isEmpty()) {
                    selectedNodeRepository.setOnlySelectedEffect(element.elementId);
                }
                StatelessEffect effect = timelineAccessor.findEffectById(element.elementId).get();
                ContextMenu contextMenu = effectContextMenuFactory.createContextMenuForEffect(effect);
                contextMenu.show(parentWindow, event.getScreenX(), event.getScreenY());
                event.consume();
            }
        }
    }

    public static class TimelineCanvasContextMenuRequestedHandlerRequest {
        Optional<TimelineUiCacheElement> selectedElement;
        Window parentWindow;
        ContextMenuEvent event;
        private TimelineCanvasContextMenuRequestedHandlerRequest(Builder builder) {
            this.selectedElement = builder.selectedElement;
            this.parentWindow = builder.parentWindow;
            this.event = builder.event;
        }

        public static Builder builder() {
            return new Builder();
        }
        public static final class Builder {
            private Optional<TimelineUiCacheElement> selectedElement = Optional.empty();
            private Window parentWindow;
            private ContextMenuEvent event;
            private Builder() {
            }

            public Builder withSelectedElement(Optional<TimelineUiCacheElement> selectedElement) {
                this.selectedElement = selectedElement;
                return this;
            }

            public Builder withParentWindow(Window parentWindow) {
                this.parentWindow = parentWindow;
                return this;
            }

            public Builder withEvent(ContextMenuEvent event) {
                this.event = event;
                return this;
            }

            public TimelineCanvasContextMenuRequestedHandlerRequest build() {
                return new TimelineCanvasContextMenuRequestedHandlerRequest(this);
            }
        }

    }

}
