package com.helospark.tactview.ui.javafx.uicomponents.canvasdraw.handlers;

import java.util.Map.Entry;
import java.util.Optional;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.marker.MarkerRepository;
import com.helospark.tactview.core.timeline.marker.MarkerType;
import com.helospark.tactview.core.timeline.marker.markers.ChapterMarker;
import com.helospark.tactview.core.timeline.marker.markers.GeneralMarker;
import com.helospark.tactview.core.timeline.marker.markers.InpointMarker;
import com.helospark.tactview.core.timeline.marker.markers.Marker;
import com.helospark.tactview.core.timeline.marker.markers.OutpointMarker;
import com.helospark.tactview.ui.javafx.UiTimelineManager;
import com.helospark.tactview.ui.javafx.key.CurrentlyPressedKeyRepository;
import com.helospark.tactview.ui.javafx.stylesheet.AlertDialogFactory;
import com.helospark.tactview.ui.javafx.uicomponents.TimelineState;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Window;

@Component
public class TimelineCanvasTimelineHeaderClickHandler {
    private UiTimelineManager uiTimelineManager;
    private MarkerRepository markerRepository;
    private CurrentlyPressedKeyRepository currentlyPressedKeyRepository;
    private TimelineState timelineState;
    private AlertDialogFactory dialogFactory;

    public TimelineCanvasTimelineHeaderClickHandler(UiTimelineManager uiTimelineManager, MarkerRepository markerRepository, CurrentlyPressedKeyRepository currentlyPressedKeyRepository,
            TimelineState timelineState, AlertDialogFactory dialogFactory) {
        this.uiTimelineManager = uiTimelineManager;
        this.markerRepository = markerRepository;
        this.currentlyPressedKeyRepository = currentlyPressedKeyRepository;
        this.timelineState = timelineState;
        this.dialogFactory = dialogFactory;
    }

    public void onHeaderClick(TimelineCanvasTimelineHeaderClickHandlerRequest request) {
        MouseEvent event = request.event;
        boolean isControlDown = currentlyPressedKeyRepository.isKeyDown(KeyCode.CONTROL);

        if (event.getButton().equals(MouseButton.PRIMARY) && !isControlDown) {
            uiTimelineManager.jumpAbsolute(request.position.getSeconds());
        } else if (event.getButton().equals(MouseButton.PRIMARY) && isControlDown) {
            markerRepository.addMarker(request.position, new GeneralMarker(""));
        } else if (event.getButton().equals(MouseButton.SECONDARY)) {
            for (var marker : markerRepository.getMarkers().entrySet()) {
                double markerPosition = timelineState.secondsToPixelsWithZoom(marker.getKey());
                double mousePosition = request.event.getX();

                if (Math.abs(markerPosition - mousePosition) < 5) {
                    ContextMenu contextMenu = createContextMenu(marker);
                    contextMenu.show(request.parentWindow, event.getScreenX(), event.getScreenY());
                    break;
                }
            }
        }
    }

    private ContextMenu createContextMenu(Entry<TimelinePosition, Marker> marker) {
        ContextMenu contextMenu = new ContextMenu();

        contextMenu.getItems().add(createDeleteContextMenu(marker));
        contextMenu.getItems().add(convertMarkerContextMenu(marker));

        if (marker.getValue().getType().equals(MarkerType.CHAPTER)) {
            contextMenu.getItems().add(changeChapterTextMenuItem(marker));
        }
        if (marker.getValue().getType().equals(MarkerType.GENERAL)) {
            contextMenu.getItems().add(changeGeneralTextMenuItem(marker));
        }

        return contextMenu;
    }

    private MenuItem convertMarkerContextMenu(Entry<TimelinePosition, Marker> marker) {
        Menu convertMarker = new Menu("convert marker");

        if (!marker.getValue().getType().equals(MarkerType.CHAPTER)) {
            MenuItem menuItem = new MenuItem("Chapter");
            menuItem.setOnAction(e -> {
                Optional<String> result = dialogFactory.showTextInputDialog("Add chapter", "Label of the chapter", "Chapter x");

                if (result.isPresent()) {
                    markerRepository.addMarker(marker.getKey(), new ChapterMarker(result.get()));
                }
            });
            convertMarker.getItems().add(menuItem);
        }
        if (!marker.getValue().getType().equals(MarkerType.INPOINT)) {
            MenuItem menuItem = new MenuItem("Inpoint");
            menuItem.setOnAction(e -> {
                markerRepository.addMarker(marker.getKey(), new InpointMarker());
            });
            convertMarker.getItems().add(menuItem);
        }
        if (!marker.getValue().getType().equals(MarkerType.OUTPOINT)) {
            MenuItem menuItem = new MenuItem("Outpoint");
            menuItem.setOnAction(e -> {
                markerRepository.addMarker(marker.getKey(), new OutpointMarker());
            });
            convertMarker.getItems().add(menuItem);
        }
        if (!marker.getValue().getType().equals(MarkerType.GENERAL)) {
            MenuItem menuItem = new MenuItem("General marker");
            menuItem.setOnAction(e -> {
                Optional<String> result = dialogFactory.showTextInputDialog("Add marker", "label", "Something interesting");

                if (result.isPresent()) {
                    markerRepository.addMarker(marker.getKey(), new GeneralMarker(result.get()));
                }
            });
            convertMarker.getItems().add(menuItem);
        }

        return convertMarker;
    }

    private MenuItem createDeleteContextMenu(Entry<TimelinePosition, Marker> marker) {
        MenuItem deleteMarker = new MenuItem("Delete marker");
        deleteMarker.setOnAction(e -> markerRepository.removeMarkerAt(marker.getKey()));
        return deleteMarker;
    }

    private MenuItem changeChapterTextMenuItem(Entry<TimelinePosition, Marker> marker) {
        MenuItem changeText = new MenuItem("Change chapter name");

        changeText.setOnAction(e -> {
            Optional<String> result = dialogFactory.showTextInputDialog("Change name of chapter", "Label of the chapter", "Chapter x");

            if (result.isPresent()) {
                markerRepository.addMarker(marker.getKey(), new ChapterMarker(result.get()));
            }
        });

        return changeText;
    }

    private MenuItem changeGeneralTextMenuItem(Entry<TimelinePosition, Marker> marker) {
        MenuItem changeText = new MenuItem("Change label");

        changeText.setOnAction(e -> {
            Optional<String> result = dialogFactory.showTextInputDialog("Change label", "Label of the marker", "Something interesting");

            if (result.isPresent()) {
                markerRepository.addMarker(marker.getKey(), new GeneralMarker(result.get()));
            }
        });

        return changeText;
    }

    public static class TimelineCanvasTimelineHeaderClickHandlerRequest {
        MouseEvent event;
        TimelinePosition position;
        Window parentWindow;

        private TimelineCanvasTimelineHeaderClickHandlerRequest(Builder builder) {
            this.event = builder.event;
            this.position = builder.position;
            this.parentWindow = builder.parentWindow;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static final class Builder {
            private MouseEvent event;
            private TimelinePosition position;
            private Window parentWindow;
            private Builder() {
            }

            public Builder withEvent(MouseEvent event) {
                this.event = event;
                return this;
            }

            public Builder withPosition(TimelinePosition position) {
                this.position = position;
                return this;
            }

            public Builder withParentWindow(Window parentWindow) {
                this.parentWindow = parentWindow;
                return this;
            }

            public TimelineCanvasTimelineHeaderClickHandlerRequest build() {
                return new TimelineCanvasTimelineHeaderClickHandlerRequest(this);
            }
        }

    }

}
