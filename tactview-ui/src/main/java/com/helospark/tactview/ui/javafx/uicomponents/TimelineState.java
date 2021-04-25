package com.helospark.tactview.ui.javafx.uicomponents;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.markers.ResettableBean;
import com.helospark.tactview.core.timeline.SecondsAware;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.util.messaging.MessagingService;
import com.helospark.tactview.ui.javafx.uicomponents.canvasdraw.domain.UiTimelineChange;
import com.helospark.tactview.ui.javafx.uicomponents.canvasdraw.domain.UiTimelineChangeType;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableDoubleValue;
import javafx.beans.value.ObservableIntegerValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

@Component
public class TimelineState implements ResettableBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(TimelineState.class);
    public static final BigDecimal PIXEL_PER_SECOND = new BigDecimal(10L);

    private List<Consumer<UiTimelineChange>> onChangeSubscribers = new ArrayList<>();

    private ObservableIntegerValue horizontalScrollPosition = new SimpleIntegerProperty(0);
    private Map<String, Runnable> idToRemoveRunnable = new HashMap<>();

    // ZOOM
    private SimpleDoubleProperty hscroll = new SimpleDoubleProperty(0);
    private SimpleDoubleProperty vscroll = new SimpleDoubleProperty(0);
    private SimpleDoubleProperty timelineWidthProperty = new SimpleDoubleProperty(2000.0);
    private SimpleDoubleProperty zoomValue = new SimpleDoubleProperty(1.0);
    private SimpleDoubleProperty translate = new SimpleDoubleProperty(0);

    private SimpleDoubleProperty linePosition = new SimpleDoubleProperty(0.0);
    private TimelineLineProperties moveSpecialPointLineProperties = new TimelineLineProperties();

    private TimelineLineProperties loopAProperties = new TimelineLineProperties();
    private TimelineLineProperties loopBProperties = new TimelineLineProperties();

    private ZoomableScrollPane timeLineScrollPane;

    private MessagingService messagingService;

    private ObservableList<HBox> channels = FXCollections.observableArrayList();
    private ObservableList<Pane> channelHeaders = FXCollections.observableArrayList();
    private Map<String, ObservableList<Pane>> channelToClips = new HashMap<>();
    private Map<String, ObservableList<Node>> clipsToEffects = new HashMap<>();

    private TimelinePosition playbackPosition = TimelinePosition.ofZero();

    public TimelineState(
            MessagingService messagingService) {
        this.messagingService = messagingService;
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

    public ObservableIntegerValue getHorizontalScrollPosition() {
        return horizontalScrollPosition;
    }

    public Map<String, Runnable> getIdToRemoveRunnable() {
        return idToRemoveRunnable;
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

    public SimpleDoubleProperty getReadOnlyLinePosition() {
        return linePosition;
    }

    public DoubleBinding getLinePosition() {
        DoubleBinding result = linePosition.add(0);
        return result;
    }

    public MessagingService getMessagingService() {
        return messagingService;
    }

    public ObservableList<HBox> getChannels() {
        return channels;
    }

    public ObservableList<Node> getChannelsAsNodes() {
        return (ObservableList<Node>) (Object) channels;
    }

    public Optional<Integer> findChannelIndex(String channelId) {
        for (int i = 0; i < channels.size(); ++i) {
            if (Objects.equals(channels.get(i).getUserData(), channelId)) {
                return Optional.ofNullable(i);
            }
        }
        return Optional.empty();
    }

    public Optional<HBox> findChannelById(String channelId) {
        return channels.stream()
                .filter(channel -> Objects.equals(channel.getUserData(), channelId))
                .findFirst();
    }

    public void addClipForChannel(String channelId, String clipId, Pane createClip) {
        ObservableList<Pane> channel = channelToClips.get(channelId);
        if (channel == null) {
            throw new IllegalArgumentException("Channel doesn't exist");
        }
        channel.add(createClip);
        ObservableList<Node> effects = FXCollections.observableArrayList();
        Bindings.bindContentBidirectional(effects, createClip.getChildren());
        clipsToEffects.put(clipId, effects);

        notifySubscribers(UiTimelineChangeType.OTHER);
    }

    public Optional<Pane> findClipById(String clipId) {
        return getAllClips()
                .filter(element -> Objects.equals(element.getUserData(), clipId))
                .findFirst();
    }

    public Stream<Pane> getAllClips() {
        return channelToClips.values()
                .stream()
                .flatMap(list -> list.stream());
    }

    public void removeClip(String elementId) {
        Optional<Pane> clipToRemove = findClipById(elementId);
        if (clipToRemove.isPresent()) {
            Pane actualClip = clipToRemove.get();
            Pane parent = (Pane) actualClip.getParent();
            parent.getChildren().remove(actualClip);

            notifySubscribers(UiTimelineChangeType.OTHER);
        }
    }

    public Optional<Node> removeEffect(String effectId) {
        Optional<Node> effectToRemove = findEffectById(effectId);
        if (effectToRemove.isPresent()) {
            Node actualClip = effectToRemove.get();
            Pane parent = (Pane) actualClip.getParent();
            parent.getChildren().remove(actualClip);
            notifySubscribers(UiTimelineChangeType.OTHER);
        }
        return effectToRemove;
    }

    public Optional<Node> findEffectById(String effectId) {
        return clipsToEffects.values()
                .stream()
                .flatMap(a -> a.stream())
                .filter(a -> Objects.equals(a.getUserData(), effectId))
                .findFirst();
    }

    public void setLinePosition(TimelinePosition position) {
        double pixels = secondsToPixels(position);
        linePosition.set(pixels);
        this.playbackPosition = position;

        notifySubscribers(UiTimelineChangeType.TIMELINE_POSITION);
    }

    public void addChannel(Integer index, String channelId, HBox timeline, VBox timelineTitle) {
        timeline.setUserData(channelId);
        channels.add(index, timeline);
        channelHeaders.add(index, timelineTitle);
        ObservableList<Pane> newList = FXCollections.observableArrayList();
        Bindings.bindContentBidirectional((ObservableList<Node>) (Object) newList, ((Pane) timeline.getChildren().get(0)).getChildren());
        channelToClips.put(channelId, newList);

        notifySubscribers(UiTimelineChangeType.OTHER);
    }

    public void removeChannel(String channelId) {
        Optional<Integer> channelIndex = findChannelIndex(channelId);
        if (channelIndex.isPresent()) {
            channels.remove(channelIndex.get().intValue());
            channelHeaders.remove(channelIndex.get().intValue());
        }
        channelToClips.remove(channelId);
        notifySubscribers(UiTimelineChangeType.OTHER);
    }

    public Optional<HBox> findChannelForClip(Pane group) {
        return channelToClips.entrySet()
                .stream()
                .filter(entry -> entry.getValue().contains(group))
                .findFirst()
                .flatMap(entry -> findChannelById(entry.getKey()));
    }

    public Optional<HBox> findChannelForClip(String originalClipId) {
        return channelToClips.entrySet()
                .stream()
                .filter(entry -> doesChannelContainsClip(entry, originalClipId))
                .findFirst()
                .flatMap(entry -> findChannelById(entry.getKey()));
    }

    private boolean doesChannelContainsClip(Entry<String, ObservableList<Pane>> entry, String originalClipId) {
        return entry.getValue()
                .stream()
                .filter(a -> a.getUserData().equals(originalClipId))
                .findFirst()
                .isPresent();
    }

    public void addEffectToClip(String clipId, Node createEffect) {
        ObservableList<Node> effectList = clipsToEffects.get(clipId);
        effectList.add(createEffect);
        notifySubscribers(UiTimelineChangeType.OTHER);
    }

    public void changeChannelFor(Pane clip, String newChannelId) {
        HBox originalChannel = findChannelForClip(clip).orElse(null);
        if (!originalChannel.getUserData().equals(newChannelId)) {
            // removeClip((String) clip.getUserData());
            // HBox newChannel = findChannelById(newChannelId).orElseThrow(() -> new IllegalArgumentException("New channel doesn't exist"));
            // newChannel.getChildren().add(clip);
            channelToClips.get(originalChannel.getUserData()).remove(clip);
            channelToClips.get(newChannelId).add(clip);
            notifySubscribers(UiTimelineChangeType.OTHER);
        }
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
        LOGGER.debug("zoom:" + zoom);
        this.zoomValue.set(zoom);
        notifySubscribers(UiTimelineChangeType.OTHER);
    }

    public void setTranslate(double newTranslate) {
        LOGGER.debug("translate:" + newTranslate);
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

    public SimpleDoubleProperty getTimelineWidthProperty() {
        return timelineWidthProperty;
    }

    public ZoomableScrollPane getTimeLineScrollPane() {
        return timeLineScrollPane;
    }

    public void setTimeLineScrollPane(ZoomableScrollPane timeLineScrollPane) {
        this.timeLineScrollPane = timeLineScrollPane;
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

    public Stream<Node> getAllEffects() {
        return clipsToEffects.entrySet()
                .stream()
                .flatMap(a -> a.getValue().stream());
    }

    public Stream<Node> findEffectsForClip(String clipId) {
        return Optional.ofNullable(clipsToEffects.get(clipId))
                .stream()
                .flatMap(a -> a.stream());
    }

    public void moveChannel(int originalIndex, int newIndex) {
        Platform.runLater(() -> {
            HBox originalChannel = channels.remove(originalIndex);
            channels.add(newIndex, originalChannel);

            Pane originalHeader = channelHeaders.remove(originalIndex);
            channelHeaders.add(newIndex, originalHeader);
            notifySubscribers(UiTimelineChangeType.OTHER);
        });
    }

    public TimelineLineProperties getLoopALineProperties() {
        return loopAProperties;
    }

    public TimelineLineProperties getLoopBLineProperties() {
        return loopBProperties;
    }

    public boolean loopingEnabled() {
        return false;
        //        return loopAProperties.getEnabledProperty().get() && loopBProperties.getEnabledProperty().get();
    }

    public TimelinePosition getLoopStartTime() {
        return null;
        //        return pixelsToSeconds(loopAProperties.getStartX().get());
    }

    public TimelinePosition getLoopEndTime() {
        return null;
        //        return pixelsToSeconds(loopBProperties.getStartX().get());
    }

    @Override
    public void resetDefaults() {
        zoomValue.set(1.0);
        hscroll.set(0);
        vscroll.set(0);
        timelineWidthProperty.set(2000.0);
        translate.set(0);
        linePosition.set(0.0);
        moveSpecialPointLineProperties.reset();
        loopAProperties.reset();
        loopBProperties.reset();
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

}
