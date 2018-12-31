package com.helospark.tactview.ui.javafx.tabs;

import java.util.List;

import com.helospark.lightdi.LightDiContext;
import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.annotation.Order;
import com.helospark.tactview.core.timeline.effect.EffectFactory;
import com.helospark.tactview.ui.javafx.uicomponents.detailsdata.localizeddetail.LocalizedDetailRepository;

import javafx.geometry.Orientation;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

@Component
@Order(0)
public class EffectTabFactory implements TabFactory {
    private static final String DEFAULT_URI = "classpath:/icons/effect/fallback.png";
    private LightDiContext lightDi;
    private DraggableIconFactory iconFactory;
    private LocalizedDetailRepository localizedDetailRepository;

    public EffectTabFactory(LightDiContext lightDi, DraggableIconFactory iconFactory, LocalizedDetailRepository localizedDetailRepository) {
        this.lightDi = lightDi;
        this.iconFactory = iconFactory;
        this.localizedDetailRepository = localizedDetailRepository;
    }

    @Override
    public Tab createTabContent() {
        FlowPane effectTabContent = new FlowPane(Orientation.HORIZONTAL, 5, 5);
        // leftHBox.setPrefWidth(scene.getWidth() - 300);

        List<EffectFactory> effects = lightDi.getListOfBeans(EffectFactory.class);

        effects.stream()
                .forEach(factory -> {
                    String iconUri = localizedDetailRepository.queryData(factory.getEffectId())
                            .flatMap(data -> data.getIconUrl())
                            .orElse(DEFAULT_URI);
                    VBox icon = iconFactory.createIcon("effect:" + factory.getEffectId(),
                            factory.getEffectName(),
                            iconUri);
                    effectTabContent.getChildren().add(icon); //"file:/home/black/Pictures/effect_1_50.png"
                });
        Tab effectTab = new Tab();
        effectTab.setText("effects");

        ScrollPane effectScrollPane = new ScrollPane(effectTabContent);
        effectScrollPane.setFitToWidth(true);
        effectScrollPane.setId("effect-view");
        effectTab.setContent(effectScrollPane);

        return effectTab;
    }

}
