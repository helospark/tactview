package com.helospark.tactview.ui.javafx.uicomponents.canvasdraw.handlers;

import java.util.List;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.ui.javafx.repository.DragRepository;
import com.helospark.tactview.ui.javafx.repository.DragRepository.DragDirection;
import com.helospark.tactview.ui.javafx.repository.drag.ClipDragInformation;
import com.helospark.tactview.ui.javafx.uicomponents.EffectDragInformation;
import com.helospark.tactview.ui.javafx.uicomponents.canvasdraw.domain.TimelineUiCacheElement;
import com.helospark.tactview.ui.javafx.uicomponents.canvasdraw.domain.TimelineUiCacheType;

import javafx.scene.input.MouseEvent;

@Component
public class TimelineCanvasElementClickHandler {
    private TimelineManagerAccessor timelineAccessor;
    private DragRepository dragRepository;

    public TimelineCanvasElementClickHandler(TimelineManagerAccessor timelineAccessor, DragRepository dragRepository) {
        this.timelineAccessor = timelineAccessor;
        this.dragRepository = dragRepository;
    }

    public void onElementClick(MouseEvent event, double currentX, TimelineUiCacheElement element) {
        boolean isResizing = isResizing(element, event.getX());

        if (element.elementType.equals(TimelineUiCacheType.CLIP)) {
            TimelineClip clip = timelineAccessor.findClipById(element.elementId).get();
            String channelId = timelineAccessor.findChannelForClipId(element.elementId).get().getId();
            double clipPositionAsDouble = clip.getGlobalInterval().getStartPosition().getSeconds().doubleValue();
            if (isResizing) {
                boolean resizingLeft = isResizingLeft(element, event.getX());
                TimelinePosition originalPosition = resizingLeft ? clip.getGlobalInterval().getStartPosition() : clip.getGlobalInterval().getEndPosition();
                List<String> clipIds = timelineAccessor.findLinkedClipsWithSameInterval(element.elementId);
                ClipDragInformation clipDragInformation = new ClipDragInformation(originalPosition, clipIds, channelId, currentX - clipPositionAsDouble);
                dragRepository.onClipResizing(clipDragInformation, resizingLeft ? DragDirection.LEFT : DragDirection.RIGHT);
            } else {
                ClipDragInformation clipDragInformation = new ClipDragInformation(clip.getGlobalInterval().getStartPosition(), List.of(element.elementId), channelId, currentX - clipPositionAsDouble);
                dragRepository.onClipDragged(clipDragInformation);
            }
        } else {
            TimelineClip clip = timelineAccessor.findClipForEffect(element.elementId).get();
            StatelessEffect effect = timelineAccessor.findEffectById(element.elementId).get();
            if (isResizing) {
                boolean resizingLeft = isResizingLeft(element, event.getX());
                TimelinePosition originalPosition = resizingLeft ? effect.getGlobalInterval().getStartPosition() : effect.getGlobalInterval().getEndPosition();
                EffectDragInformation effectDragInformation = new EffectDragInformation(clip.getId(), effect.getId(), originalPosition, currentX);
                dragRepository.onEffectResized(effectDragInformation, resizingLeft ? DragDirection.LEFT : DragDirection.RIGHT);
            } else {
                TimelinePosition originalPosition = effect.getGlobalInterval().getStartPosition();
                double clipStartSecondDouble = effect.getGlobalInterval().getStartPosition().getSeconds().doubleValue();
                EffectDragInformation effectDragInformation = new EffectDragInformation(clip.getId(), effect.getId(), originalPosition, currentX - clipStartSecondDouble);
                dragRepository.onEffectDragged(effectDragInformation);
            }

        }
    }

    public boolean isResizing(TimelineUiCacheElement element, double x) {
        return isResizable(element) && (isResizingLeft(element, x) || isResizingRight(element, x));
    }

    private boolean isResizable(TimelineUiCacheElement element) {
        if (element.elementType.equals(TimelineUiCacheType.CLIP)) {
            return timelineAccessor.findClipById(element.elementId).map(a -> a.isResizable()).orElse(false);
        }
        if (element.elementType.equals(TimelineUiCacheType.EFFECT)) {
            return timelineAccessor.findEffectById(element.elementId).map(a -> true).orElse(false);
        }
        return false;
    }

    public boolean isResizingLeft(TimelineUiCacheElement element, double x) {
        return x - element.rectangle.topLeftX < 10;
    }

    public boolean isResizingRight(TimelineUiCacheElement element, double x) {
        return element.rectangle.topLeftX + element.rectangle.width - x < 10;
    }

}
