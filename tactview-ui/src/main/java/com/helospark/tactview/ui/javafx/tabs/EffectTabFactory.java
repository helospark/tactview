package com.helospark.tactview.ui.javafx.tabs;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.helospark.lightdi.LightDiContext;
import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.annotation.Order;
import com.helospark.tactview.core.timeline.effect.EffectFactory;
import com.helospark.tactview.ui.javafx.uicomponents.detailsdata.localizeddetail.LocalizedDetailDomain;
import com.helospark.tactview.ui.javafx.uicomponents.detailsdata.localizeddetail.LocalizedDetailRepository;

import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

@Component
@Order(0)
public class EffectTabFactory extends AbstractSearchableTabFactory {
    private static final String DEFAULT_URI = "classpath:/icons/effect/fallback.png";
    private LightDiContext lightDi;
    private DraggableIconFactory iconFactory;
    private LocalizedDetailRepository localizedDetailRepository;

    public EffectTabFactory(LightDiContext lightDi, DraggableIconFactory iconFactory, LocalizedDetailRepository localizedDetailRepository) {
        super("effects", "effect-view");
        this.lightDi = lightDi;
        this.iconFactory = iconFactory;
        this.localizedDetailRepository = localizedDetailRepository;
    }

    @Override
    protected void fillFlowPane(FlowPane effectTabContent, String searchData) {
        List<EffectFactory> effects = lightDi.getListOfBeans(EffectFactory.class);

        List<ScoredNodeHolder> icons = new ArrayList<>();

        effects.stream()
                .forEach(factory -> {
                    Optional<LocalizedDetailDomain> localizedDetail = localizedDetailRepository.queryData(factory.getEffectId());
                    int score = getScore(localizedDetail, factory.getId(), factory.getEffectName(), searchData);
                    if (score > 0) {
                        String iconUri = localizedDetail
                                .flatMap(data -> data.getIconUrl())
                                .orElse(DEFAULT_URI);
                        VBox icon = iconFactory.createIcon("effect:" + factory.getEffectId(),
                                factory.getEffectName(),
                                iconUri);
                        icons.add(new ScoredNodeHolder(icon, score));
                    }
                });

        effectTabContent.getChildren().clear();
        icons.stream()
                .sorted()
                .forEach(entry -> {
                    effectTabContent.getChildren().add(entry.node);
                });
    }
}
