package com.helospark.tactview.ui.javafx.uicomponents;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.markers.ResettableBean;
import com.helospark.tactview.core.timeline.SecondsAware;
import com.helospark.tactview.core.timeline.TimelineLength;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.ui.javafx.uicomponents.canvasdraw.domain.UiTimelineChange;
import com.helospark.tactview.ui.javafx.uicomponents.canvasdraw.domain.UiTimelineChangeType;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableDoubleValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

@Component
public class TimelineState implements ResettableBean {
    public static final BigDecimal PIXEL_PER_SECOND = new BigDecimal(10L);

    private List<Consumer<UiTimelineChange>> onChangeSubscribers = new ArrayList<>();

    private SimpleDoubleProperty hscroll = new SimpleDoubleProperty(0);
    private SimpleDoubleProperty vscroll = new SimpleDoubleProperty(0);
    private SimpleDoubleProperty zoomValue = new SimpleDoubleProperty(1.0);
    private SimpleDoubleProperty translate = new SimpleDoubleProperty(0);

    private TimelineLength timelineLength = TimelineLength.ofZero();

    private SimpleDoubleProperty linePosition = new SimpleDoubleProperty(0.0);
    private TimelineLineProperties moveSpecialPointLineProperties = new TimelineLineProperties();

    private TimelinePosition loopAProperties = null;
    private TimelinePosition loopBProperties = null;

    private ObservableList<Pane> channelHeaders = FXCollections.observableArrayList();

    private TimelinePosition playbackPosition = TimelinePosition.ofZero();

    public TimelineState() {
        vscroll.addListener(e -> notifySubscribers(UiTimelineChangeType.OTHER));
    }

    public TimelinePosition pixelsToSeconds(double xCoordinate) {
        BigDecimal position = new BigDecimal(xCoordinate)
                .divide(PIXEL_PER_SECOND, 10, RoundingMode.HALF_UP);
        return new TimelinePosition(position);
    }

    public TimelinePosition pixelsToSecondsWithZoom(double xCoordinate) {
        BigDecimal position = new BigDecimal(xCoordinate)
                .divide(PIXEL_PER_SECOND, 10, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(zoomValue.get()), 10, RoundingMode.HALF_UP);
        return new TimelinePosition(position);
    }

    public double secondsToPixels(SecondsAware length) {
        return length.getSeconds()
                .multiply(PIXEL_PER_SECOND)
                .intValue();
    }

    public double secondsToPixelsWithZoom(SecondsAware length) {
        return length.getSeconds()
                .multiply(PIXEL_PER_SECOND) // todo: zoom and scroll
                .multiply(BigDecimal.valueOf(zoomValue.get()))
                .doubleValue();
    }

    public int secondsToPixelsWidthZoomAndTranslate(SecondsAware length) {
        return length.getSeconds()
                .multiply(PIXEL_PER_SECOND) // todo: zoom and scroll
                .multiply(BigDecimal.valueOf(zoomValue.get()))
                .subtract(BigDecimal.valueOf(translate.get()))
                .intValue();
    }

    public ObservableDoubleValue getZoomValue() {
        return zoomValue;
    }

    public double getZoom() {
        return zoomValue.get();
    }

    public ObservableDoubleValue getTranslate() {
        return translate;
    }

    public double getTranslateDouble() {
        return this.pixelsToSecondsWithZoom(translate.get()).getSeconds().doubleValue();
    }

    public double getTimelineLengthDouble() {
        return timelineLength.getSeconds().doubleValue();
    }

    public SimpleDoubleProperty getReadOnlyLinePosition() {
        return linePosition;
    }

    public DoubleBinding getLinePosition() {
        DoubleBinding result = linePosition.add(0);
        return result;
    }

    public void setLinePosition(TimelinePosition position) {
        double pixels = secondsToPixels(position);
        linePosition.set(pixels);
        this.playbackPosition = position;

        notifySubscribers(UiTimelineChangeType.TIMELINE_POSITION);
    }

    public void enableSpecialPointLineProperties(TimelinePosition lineStartX, String endChannel, String startChannel) {
        moveSpecialPointLineProperties.setEnabled(true);
        moveSpecialPointLineProperties.setPosition(lineStartX);

        moveSpecialPointLineProperties.setStartChannel(startChannel);
        moveSpecialPointLineProperties.setEndChannel(endChannel);

        notifySubscribers(UiTimelineChangeType.SPECIAL_LINE_POSITION);
    }

    public TimelineLineProperties getMoveSpecialPointLineProperties() {
        return moveSpecialPointLineProperties;
    }

    public void disableSpecialPointLineProperties() {
        moveSpecialPointLineProperties.setEnabled(false);
        notifySubscribers(UiTimelineChangeType.SPECIAL_LINE_POSITION);
    }

    public void setZoom(double zoom) {
        this.zoomValue.set(zoom);
        notifySubscribers(UiTimelineChangeType.OTHER);
    }

    public void setTranslate(double newTranslate) {
        this.translate.set(newTranslate);
        notifySubscribers(UiTimelineChangeType.OTHER);
    }

    public ObservableList<Node> getChannelTitlesAsNodes() {
        return (ObservableList<Node>) (Object) channelHeaders;
    }

    public void onShownLocationChange(Runnable runnable) {
        translate.addListener(newValue -> runnable.run());
        zoomValue.addListener(newValue -> runnable.run());
        channelHeaders.addListener(new InvalidationListener() {

            @Override
            public void invalidated(Observable observable) {
                runnable.run();
            }
        });
        runnable.run();
    }

    public SimpleDoubleProperty getHscroll() {
        return hscroll;
    }

    public double getChannelTitlesWidth() {
        if (channelHeaders.isEmpty()) {
            return 150;
        } else {
            return channelHeaders.get(0).getWidth();
        }
    }

    public TimelinePosition getTimeAtLeftSide() {
        double translatedCurrentValue = translate.get();
        double zoomedCurrentValue = zoomValue.get();

        BigDecimal position = new BigDecimal(translatedCurrentValue)
                .divide(new BigDecimal(zoomedCurrentValue), 10, RoundingMode.HALF_UP)
                .divide(PIXEL_PER_SECOND, 10, RoundingMode.HALF_UP);

        return new TimelinePosition(position);
    }

    public void horizontalScroll(double scrollStrength) {
        double newScroll = hscroll.get() + scrollStrength;
        if (newScroll >= 0 && newScroll < 1.0) {
            hscroll.set(newScroll);
            notifySubscribers(UiTimelineChangeType.OTHER);
        }
    }

    public SimpleDoubleProperty getVscroll() {
        return vscroll;
    }

    public void verticalScroll(double scrollStrength) {
        double newScroll = vscroll.get() + scrollStrength;
        if (newScroll >= 0 && newScroll < 1.0) {
            vscroll.set(newScroll);
            notifySubscribers(UiTimelineChangeType.OTHER);
        }
    }

    public Optional<TimelinePosition> getLoopALineProperties() {
        return Optional.ofNullable(loopAProperties);
    }

    public Optional<TimelinePosition> getLoopBLineProperties() {
        return Optional.ofNullable(loopBProperties);
    }

    public void setLoopAProperties(TimelinePosition loopAProperties) {
        this.loopAProperties = loopAProperties;
        notifySubscribers(UiTimelineChangeType.OTHER);
    }

    public void setLoopBProperties(TimelinePosition loopBProperties) {
        this.loopBProperties = loopBProperties;
        notifySubscribers(UiTimelineChangeType.OTHER);
    }

    public boolean loopingEnabled() {
        return loopAProperties != null && loopBProperties != null;
    }

    @Override
    public void resetDefaults() {
        zoomValue.set(1.0);
        hscroll.set(0);
        vscroll.set(0);
        translate.set(0);
        linePosition.set(0.0);
        moveSpecialPointLineProperties.reset();
        loopAProperties = null;
        loopBProperties = null;
        notifySubscribers(UiTimelineChangeType.OTHER);
        //        timeLineScrollPane.reset();
    }

    private void notifySubscribers(UiTimelineChangeType type) {
        UiTimelineChange change = new UiTimelineChange(type);
        onChangeSubscribers.stream()
                .forEach(a -> a.accept(change));
    }

    public void subscribe(Consumer<UiTimelineChange> r) {
        onChangeSubscribers.add(r);
    }

    public void setNormalizedVScroll(double normalizedScroll) {
        if (normalizedScroll < 0) {
            normalizedScroll = 0.0;
        }
        if (normalizedScroll > 1.0) {
            normalizedScroll = 1.0;
        }
        vscroll.set(normalizedScroll);

        notifySubscribers(UiTimelineChangeType.OTHER);
    }

    public TimelinePosition getPlaybackPosition() {
        return playbackPosition;
    }

    public void addChannelHeader(String channelId, VBox timelineTitle, int index) {
        channelHeaders.add(index, timelineTitle);
        timelineTitle.setUserData(channelId);
        notifySubscribers(UiTimelineChangeType.OTHER);
    }

    public void moveChannel(int originalIndex, int newIndex) {
        Pane originalHeader = channelHeaders.remove(originalIndex);
        channelHeaders.add(newIndex, originalHeader);
        notifySubscribers(UiTimelineChangeType.OTHER);
    }

    public void removeChannel(String channelId) {
        for (int i = 0; i < channelHeaders.size(); ++i) {
            if (channelHeaders.get(i).getUserData().equals(channelId)) {
                channelHeaders.remove(i);
                notifySubscribers(UiTimelineChangeType.OTHER);
                break;
            }
        }
    }

    public void setVisibleLength(TimelineLength timelineLength) {
        this.timelineLength = timelineLength;
    }

    public TimelineLength getTimelineLength() {
        return timelineLength;
    }
}
