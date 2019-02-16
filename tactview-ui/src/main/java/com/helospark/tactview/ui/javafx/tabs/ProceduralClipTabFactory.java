package com.helospark.tactview.ui.javafx.tabs;

import java.util.List;

import com.helospark.lightdi.LightDiContext;
import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.annotation.Order;
import com.helospark.tactview.core.timeline.AddClipRequest;
import com.helospark.tactview.core.timeline.TimelineManager;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.proceduralclip.ProceduralClipFactoryChainItem;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.commands.impl.AddClipsCommand;
import com.helospark.tactview.ui.javafx.uicomponents.detailsdata.localizeddetail.LocalizedDetailRepository;

import javafx.geometry.Orientation;
import javafx.scene.control.Tab;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

@Component
@Order(1)
public class ProceduralClipTabFactory implements TabFactory {
    private static final String DEFAULT_URI = "classpath:/icons/effect/fallback.png";
    private LightDiContext lightDi;
    private DraggableIconFactory iconFactory;
    private LocalizedDetailRepository localizedDetailRepository;
    private UiCommandInterpreterService commandInterpreter;
    private TimelineManager timelineManager;

    public ProceduralClipTabFactory(LightDiContext lightDi, DraggableIconFactory iconFactory, LocalizedDetailRepository localizedDetailRepository, UiCommandInterpreterService commandInterpreter,
            TimelineManager timelineManager) {
        this.lightDi = lightDi;
        this.iconFactory = iconFactory;
        this.localizedDetailRepository = localizedDetailRepository;
        this.commandInterpreter = commandInterpreter;
        this.timelineManager = timelineManager;
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
                    VBox icon = iconFactory.createIcon("clip:" + chainItem.getProceduralClipId(),
                            chainItem.getProceduralClipName(),
                            iconUri);
                    icon.setOnMouseClicked(e -> {
                        addClipOnDoubleClick(e, chainItem.getProceduralClipId());
                    });
                    proceduralClipTabContent.getChildren().add(icon);
                });
        Tab proceduralClipTab = new Tab();
        proceduralClipTab.setText("clips");
        proceduralClipTab.setContent(proceduralClipTabContent);

        return proceduralClipTab;
    }

    private void addClipOnDoubleClick(MouseEvent e, String id) {
        TimelinePosition maxPosition = timelineManager.findEndPosition();

        AddClipRequest clipRequest = AddClipRequest.builder()
                .withChannelId(timelineManager.getAllChannelIds().get(0))
                .withPosition(maxPosition)
                .withProceduralClipId(id)
                .build();
        if (e.getClickCount() == 2) {
            commandInterpreter.sendWithResult(new AddClipsCommand(clipRequest, timelineManager));
            e.consume();
        }
    }

}
