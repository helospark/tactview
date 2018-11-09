package com.helospark.tactview.ui.javafx.uicomponents.detailsdata;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineManager;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

@Component
public class DetailsGridChain {
    private TimelineManager timelineManager;
    private ClassToDetailRepository classToDetailRepository;
    private List<DetailGridChainElement> chainElements;

    public DetailsGridChain(TimelineManager timelineManager, ClassToDetailRepository classToDetailRepository,
            List<DetailGridChainElement> chainElements) {
        this.timelineManager = timelineManager;
        this.classToDetailRepository = classToDetailRepository;
        this.chainElements = chainElements;
    }

    public GridPane createDetailsGridForClip(String id) {
        Map<String, Node> result = new LinkedHashMap<>();
        TimelineClip clip = timelineManager.findClipById(id).orElseThrow();

        result.put("type", new Label(clip.getClass().getSimpleName()));

        classToDetailRepository.queryDetail(clip.getClass())
                .ifPresent(a -> result.put("description", new Label(a)));

        chainElements.stream()
                .filter(a -> a.supports(clip))
                .forEach(a -> a.updateMap(result, clip));

        GridPane gridPane = new GridPane();
        gridPane.getStyleClass().add("effect-detail-grid");
        int row = 0;
        for (var entry : result.entrySet()) {
            gridPane.add(new Label(entry.getKey()), 0, row);
            gridPane.add(entry.getValue(), 1, row);
            ++row;
        }

        return gridPane;
    }

}
