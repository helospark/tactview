package com.helospark.tactview.ui.javafx.tabs;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.helospark.lightdi.LightDiContext;
import com.helospark.tactview.core.timeline.AddClipRequest;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.commands.impl.AddClipsCommand;
import com.helospark.tactview.ui.javafx.uicomponents.detailsdata.localizeddetail.LocalizedDetailDomain;
import com.helospark.tactview.ui.javafx.uicomponents.detailsdata.localizeddetail.LocalizedDetailRepositoryChain;

import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

public abstract class AbstractProceduralClipTabFactory<T> extends AbstractSearchableTabFactory {
    private static final String DEFAULT_URI = "classpath:/icons/effect/fallback.png";
    private final LightDiContext lightDi;
    private final DraggableIconFactory iconFactory;
    private final LocalizedDetailRepositoryChain localizedDetailRepository;
    private final UiCommandInterpreterService commandInterpreter;
    private final TimelineManagerAccessor timelineManager;

    private final Class<T> factoryClass;

    public AbstractProceduralClipTabFactory(
            String name,
            String id,
            LightDiContext lightDi,
            DraggableIconFactory iconFactory,
            LocalizedDetailRepositoryChain localizedDetailRepository,
            UiCommandInterpreterService commandInterpreter,
            TimelineManagerAccessor timelineManager,
            Class<T> factoryClass) {
        super(name, id);
        this.lightDi = lightDi;
        this.iconFactory = iconFactory;
        this.localizedDetailRepository = localizedDetailRepository;
        this.commandInterpreter = commandInterpreter;
        this.timelineManager = timelineManager;

        this.factoryClass = factoryClass;
    }

    @Override
    protected void fillFlowPane(FlowPane tabContent, String searchData) {
        List<T> proceduralClips = lightDi.getListOfBeans(factoryClass);
        List<ScoredNodeHolder> icons = new ArrayList<>();

        proceduralClips.stream()
                .forEach(chainItem -> {
                    ProceduralFactoryInfo factoryInfo = getInfoFor(chainItem);

                    Optional<LocalizedDetailDomain> localizedDetail = localizedDetailRepository.queryData(factoryInfo.getProceduralClipId());

                    int score = getScore(localizedDetail, factoryInfo.getProceduralClipId(), factoryInfo.getProceduralClipName(), searchData);
                    if (score > 0) {
                        String iconUri = localizedDetail
                                .flatMap(data -> data.getIconUrl())
                                .orElse(DEFAULT_URI);
                        Optional<String> description = localizedDetail.map(a -> a.getDescription());
                        VBox icon = iconFactory.createIcon("clip:" + factoryInfo.getProceduralClipId(),
                                factoryInfo.getProceduralClipName(),
                                iconUri,
                                description);
                        icon.setOnMouseClicked(e -> {
                            addClipOnDoubleClick(e, factoryInfo.getProceduralClipId());
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

    protected abstract ProceduralFactoryInfo getInfoFor(T factory);

    protected static class ProceduralFactoryInfo {
        String proceduralClipId;
        String proceduralClipName;

        public ProceduralFactoryInfo(String proceduralClipId, String proceduralClipName) {
            this.proceduralClipId = proceduralClipId;
            this.proceduralClipName = proceduralClipName;
        }

        public String getProceduralClipId() {
            return proceduralClipId;
        }

        public String getProceduralClipName() {
            return proceduralClipName;
        }

    }
}
