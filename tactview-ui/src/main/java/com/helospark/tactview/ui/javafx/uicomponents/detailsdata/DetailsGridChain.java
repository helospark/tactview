package com.helospark.tactview.ui.javafx.uicomponents.detailsdata;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.ui.javafx.uicomponents.detailsdata.localizeddetail.LocalizedDetailRepositoryChain;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;

@Component
public class DetailsGridChain {
    private TimelineManagerAccessor timelineManager;
    private LocalizedDetailRepositoryChain localizedDetailRepository;
    private List<ClipDetailGridChainElement> clipDetailChainElements;
    private List<EffectDetailGridChainElement> effectDetailChainElements;

    public DetailsGridChain(TimelineManagerAccessor timelineManager, LocalizedDetailRepositoryChain localizedDetailRepository,
            List<ClipDetailGridChainElement> chainElements, List<EffectDetailGridChainElement> effectDetailChainElements) {
        this.timelineManager = timelineManager;
        this.localizedDetailRepository = localizedDetailRepository;
        this.clipDetailChainElements = chainElements;
        this.effectDetailChainElements = effectDetailChainElements;
    }

    public GridPane createDetailsGridForClip(String id) {
        Map<String, Node> result = new LinkedHashMap<>();
        TimelineClip clip = timelineManager.findClipById(id).orElseThrow();

        result.put("type", new Label(clip.getClass().getSimpleName()));

        localizedDetailRepository.queryDetailForClip(clip)
                .ifPresent(a -> {
                    Label text = new Label(a);
                    text.getStyleClass().add("description-text-area");
                    text.setWrapText(true);
                    result.put("description", text);
                });

        clipDetailChainElements.stream()
                .filter(a -> a.supports(clip))
                .forEach(a -> a.updateMap(result, clip));

        GridPane gridPane = createGridPaneFromMap(result);

        return gridPane;
    }

    public GridPane createDetailsGridForEffect(String id) {
        Map<String, Node> result = new LinkedHashMap<>();
        StatelessEffect effect = timelineManager.findEffectById(id).orElseThrow();

        result.put("type", new Label(effect.getClass().getSimpleName()));

        localizedDetailRepository.queryDetail(effect.getFactoryId())
                .ifPresent(a -> {
                    Label text = new Label(a);
                    text.getStyleClass().add("description-text-area");
                    text.setWrapText(true);
                    result.put("description", text);
                });

        effectDetailChainElements.stream()
                .filter(a -> a.supports(effect))
                .forEach(a -> a.updateMap(result, effect));

        GridPane gridPane = createGridPaneFromMap(result);

        return gridPane;
    }

    private GridPane createGridPaneFromMap(Map<String, Node> result) {
        GridPane gridPane = new GridPane();
        ColumnConstraints column = new ColumnConstraints();
        column.setPrefWidth(80);
        ColumnConstraints column2 = new ColumnConstraints();
        column2.setPercentWidth(100);
        gridPane.getColumnConstraints().addAll(column, column2);
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
