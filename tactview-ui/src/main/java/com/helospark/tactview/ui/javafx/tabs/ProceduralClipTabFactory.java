package com.helospark.tactview.ui.javafx.tabs;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.helospark.lightdi.LightDiContext;
import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.annotation.Order;
import com.helospark.tactview.core.timeline.AddClipRequest;
import com.helospark.tactview.core.timeline.TimelineManager;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.proceduralclip.ProceduralClipFactoryChainItem;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.commands.impl.AddClipsCommand;
import com.helospark.tactview.ui.javafx.uicomponents.detailsdata.localizeddetail.LocalizedDetailDomain;
import com.helospark.tactview.ui.javafx.uicomponents.detailsdata.localizeddetail.LocalizedDetailRepository;

import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

@Component
@Order(1)
public class ProceduralClipTabFactory extends AbstractSearchableTabFactory {
    private static final String DEFAULT_URI = "classpath:/icons/effect/fallback.png";
    private LightDiContext lightDi;
    private DraggableIconFactory iconFactory;
    private LocalizedDetailRepository localizedDetailRepository;
    private UiCommandInterpreterService commandInterpreter;
    private TimelineManager timelineManager;

    public ProceduralClipTabFactory(LightDiContext lightDi, DraggableIconFactory iconFactory, LocalizedDetailRepository localizedDetailRepository, UiCommandInterpreterService commandInterpreter,
            TimelineManager timelineManager) {
        super("clips", "clip-view");
        this.lightDi = lightDi;
        this.iconFactory = iconFactory;
        this.localizedDetailRepository = localizedDetailRepository;
        this.commandInterpreter = commandInterpreter;
        this.timelineManager = timelineManager;
    }

    @Override
    protected void fillFlowPane(FlowPane tabContent, String searchData) {
        List<ProceduralClipFactoryChainItem> proceduralClips = lightDi.getListOfBeans(ProceduralClipFactoryChainItem.class);
        List<ScoredNodeHolder> icons = new ArrayList<>();

        proceduralClips.stream()
                .forEach(chainItem -> {
                    Optional<LocalizedDetailDomain> localizedDetail = localizedDetailRepository.queryData("proceduralClipFactory:" + chainItem.getProceduralClipId());

                    int score = getScore(localizedDetail, chainItem.getProceduralClipId(), chainItem.getProceduralClipName(), searchData);
                    if (score > 0) {
                        String iconUri = localizedDetail
                                .flatMap(data -> data.getIconUrl())
                                .orElse(DEFAULT_URI);
                        VBox icon = iconFactory.createIcon("clip:" + chainItem.getProceduralClipId(),
                                chainItem.getProceduralClipName(),
                                iconUri);
                        icon.setOnMouseClicked(e -> {
                            addClipOnDoubleClick(e, chainItem.getProceduralClipId());
                        });
                        icons.add(new ScoredNodeHolder(icon, score));
                    }
                });

        tabContent.getChildren().clear();
        icons.stream()
                .sorted()
                .forEach(entry -> {
                    tabContent.getChildren().add(entry.node);
                });
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
