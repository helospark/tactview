package com.helospark.tactview.ui.javafx.tabs;

import com.helospark.lightdi.LightDiContext;
import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.annotation.Order;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.timeline.effect.TimelineProceduralClipType;
import com.helospark.tactview.core.timeline.proceduralclip.ProceduralClipFactoryChainItem;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.uicomponents.detailsdata.localizeddetail.LocalizedDetailRepositoryChain;

@Component
@Order(1)
public class ProceduralClipTabFactory extends AbstractProceduralClipTabFactory<ProceduralClipFactoryChainItem> {

    public ProceduralClipTabFactory(LightDiContext lightDi, DraggableIconFactory iconFactory, LocalizedDetailRepositoryChain localizedDetailRepository, UiCommandInterpreterService commandInterpreter,
            TimelineManagerAccessor timelineManager) {
        super("video clips", "clip-view",
                lightDi,
                iconFactory,
                localizedDetailRepository,
                commandInterpreter,
                timelineManager,
                ProceduralClipFactoryChainItem.class,
                value -> value.getType().equals(TimelineProceduralClipType.STANDARD));
    }

    @Override
    protected ProceduralFactoryInfo getInfoFor(ProceduralClipFactoryChainItem factory) {
        return new ProceduralFactoryInfo(factory.getProceduralClipId(), factory.getProceduralClipName());
    }
}
