package com.helospark.tactview.ui.javafx.tabs;

import java.util.List;

import com.helospark.lightdi.LightDiContext;
import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.proceduralclip.ProceduralClipFactoryChainItem;
import com.helospark.tactview.ui.javafx.uicomponents.detailsdata.localizeddetail.LocalizedDetailRepository;

import javafx.geometry.Orientation;
import javafx.scene.control.Tab;
import javafx.scene.layout.FlowPane;

@Component
public class ProceduralClipTabFactory implements TabFactory {
    private static final String DEFAULT_URI = "classpath:/icons/effect/fallback.png";
    private LightDiContext lightDi;
    private DraggableIconFactory iconFactory;
    private LocalizedDetailRepository localizedDetailRepository;

    public ProceduralClipTabFactory(LightDiContext lightDi, DraggableIconFactory iconFactory, LocalizedDetailRepository localizedDetailRepository) {
        this.lightDi = lightDi;
        this.iconFactory = iconFactory;
        this.localizedDetailRepository = localizedDetailRepository;
    }

    @Override
    public Tab createTabContent() {
        FlowPane proceduralClipTabContent = new FlowPane(Orientation.HORIZONTAL, 5, 5);
        List<ProceduralClipFactoryChainItem> proceduralClips = lightDi.getListOfBeans(ProceduralClipFactoryChainItem.class);
        proceduralClips.stream()
                .forEach(chainItem -> {
                    String iconUri = localizedDetailRepository.queryData("proceduralClipFactory:" + chainItem.getProceduralClipId())
                            .flatMap(data -> data.getIconUrl())
                            .orElse(DEFAULT_URI);
                    proceduralClipTabContent.getChildren().add(iconFactory.createIcon("clip:" + chainItem.getProceduralClipId(),
                            chainItem.getProceduralClipName(),
                            iconUri));
                });
        Tab proceduralClipTab = new Tab();
        proceduralClipTab.setText("clips");
        proceduralClipTab.setContent(proceduralClipTabContent);

        return proceduralClipTab;
    }

}
