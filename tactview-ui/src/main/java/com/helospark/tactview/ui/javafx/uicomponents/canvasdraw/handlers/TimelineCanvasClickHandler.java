package com.helospark.tactview.ui.javafx.uicomponents.canvasdraw.handlers;

import java.util.Optional;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.TimelineChannel;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.ui.javafx.repository.SelectedNodeRepository;
import com.helospark.tactview.ui.javafx.uicomponents.PropertyView;
import com.helospark.tactview.ui.javafx.uicomponents.canvasdraw.TimelineCanvas;
import com.helospark.tactview.ui.javafx.uicomponents.canvasdraw.domain.TimelineUiCacheElement;
import com.helospark.tactview.ui.javafx.uicomponents.canvasdraw.domain.TimelineUiCacheType;
import com.helospark.tactview.ui.javafx.uicomponents.channelcontextmenu.ChannelContextMenuAppender;
import com.helospark.tactview.ui.javafx.uicomponents.util.ExtendsClipToMaximizeLengthService;

import javafx.scene.control.ContextMenu;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Window;

@Component
public class TimelineCanvasClickHandler {
    private TimelineManagerAccessor timelineAccessor;
    private ChannelContextMenuAppender channelContextMenuAppender;
    private SelectedNodeRepository selectedNodeRepository;
    private PropertyView propertyView;
    private ExtendsClipToMaximizeLengthService extendsClipToMaximizeLengthService;

    public TimelineCanvasClickHandler(TimelineManagerAccessor timelineAccessor, ChannelContextMenuAppender channelContextMenuAppender, SelectedNodeRepository selectedNodeRepository,
            PropertyView propertyView, ExtendsClipToMaximizeLengthService extendsClipToMaximizeLengthService) {
        this.timelineAccessor = timelineAccessor;
        this.channelContextMenuAppender = channelContextMenuAppender;
        this.selectedNodeRepository = selectedNodeRepository;
        this.propertyView = propertyView;
        this.extendsClipToMaximizeLengthService = extendsClipToMaximizeLengthService;
    }

    public boolean onClick(TimelineCanvasClickHandlerRequest request) {
        var optionalElement = request.selectedElement;
        var event = request.event;

        if (optionalElement.isPresent() && event.isStillSincePress() && event.getButton().equals(MouseButton.PRIMARY)) {
            TimelineUiCacheElement element = optionalElement.get();
            selectElementOnClick(event, element);

            if (event.getClickCount() == 2 && optionalElement.get().elementType.equals(TimelineUiCacheType.EFFECT)) {
                String effectId = optionalElement.get().elementId;
                String clipId = timelineAccessor.findClipForEffect(effectId).get().getId();
                StatelessEffect effect = timelineAccessor.findEffectById(effectId).get();
                extendsClipToMaximizeLengthService.extendEffectToClipSize(clipId, effect);
            }

            return true;
        } else if (event.isStillSincePress() && optionalElement.isEmpty()) {
            if (event.getY() > TimelineCanvas.TIMELINE_TIMESCALE_HEIGHT) {
                if (event.getButton().equals(MouseButton.PRIMARY)) {
                    selectedNodeRepository.clearAllSelectedItems();
                } else if (event.getButton().equals(MouseButton.SECONDARY)) {
                    Optional<TimelineChannel> channel = request.channelSelected;
                    if (channel.isPresent()) {
                        TimelinePosition position = request.xPosition;
                        ContextMenu contextMenu = channelContextMenuAppender.createContextMenu(channel.get().getId(), Optional.of(position));
                        contextMenu.show(request.parentWindow, event.getScreenX(), event.getScreenY());
                    }
                }
            }
            return false;
        }
        return false;
    }

    private void selectElementOnClick(MouseEvent event, TimelineUiCacheElement element) {
        if (element.elementType.equals(TimelineUiCacheType.CLIP)) {
            if (event.isControlDown()) {
                selectedNodeRepository.toggleClipSelection(element.elementId);
            } else {
                selectedNodeRepository.setOnlySelectedClip(element.elementId);
                propertyView.showClipProperties(element.elementId);
            }
        } else if (element.elementType.equals(TimelineUiCacheType.EFFECT)) {
            if (event.isControlDown()) {
                selectedNodeRepository.toggleClipSelection(element.elementId);
            } else {
                selectedNodeRepository.setOnlySelectedEffect(element.elementId);
                propertyView.showEffectProperties(element.elementId);
            }
        }
    }

    public static class TimelineCanvasClickHandlerRequest {
        Optional<TimelineUiCacheElement> selectedElement;
        MouseEvent event;
        TimelinePosition xPosition;
        Optional<TimelineChannel> channelSelected;
        Window parentWindow;

        private TimelineCanvasClickHandlerRequest(Builder builder) {
            this.selectedElement = builder.selectedElement;
            this.event = builder.event;
            this.xPosition = builder.xPosition;
            this.channelSelected = builder.channelSelected;
            this.parentWindow = builder.parentWindow;
        }

        public static Builder builder() {
            return new Builder();
        }
        public static final class Builder {
            private Optional<TimelineUiCacheElement> selectedElement = Optional.empty();
            private MouseEvent event;
            private TimelinePosition xPosition;
            private Optional<TimelineChannel> channelSelected = Optional.empty();
            private Window parentWindow;
            private Builder() {
            }

            public Builder withSelectedElement(Optional<TimelineUiCacheElement> selectedElement) {
                this.selectedElement = selectedElement;
                return this;
            }

            public Builder withEvent(MouseEvent event) {
                this.event = event;
                return this;
            }

            public Builder withXPosition(TimelinePosition xPosition) {
                this.xPosition = xPosition;
                return this;
            }

            public Builder withChannelSelected(Optional<TimelineChannel> channelSelected) {
                this.channelSelected = channelSelected;
                return this;
            }

            public Builder withParentWindow(Window parentWindow) {
                this.parentWindow = parentWindow;
                return this;
            }

            public TimelineCanvasClickHandlerRequest build() {
                return new TimelineCanvasClickHandlerRequest(this);
            }
        }

    }

}
