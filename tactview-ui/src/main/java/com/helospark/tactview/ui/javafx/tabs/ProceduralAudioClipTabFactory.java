package com.helospark.tactview.ui.javafx.tabs;

import com.helospark.lightdi.LightDiContext;
import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.annotation.Order;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.timeline.proceduralclip.audio.ProceduralAudioClipFactoryChainItem;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.uicomponents.detailsdata.localizeddetail.LocalizedDetailRepositoryChain;

@Component
@Order(4)
public class ProceduralAudioClipTabFactory extends AbstractProceduralClipTabFactory<ProceduralAudioClipFactoryChainItem> {

    public ProceduralAudioClipTabFactory(LightDiContext lightDi, DraggableIconFactory iconFactory, LocalizedDetailRepositoryChain localizedDetailRepository,
            UiCommandInterpreterService commandInterpreter,
            TimelineManagerAccessor timelineManager) {
        super("audio clips",
                "audio-clip-view",
                lightDi,
                iconFactory,
                localizedDetailRepository,
                commandInterpreter,
                timelineManager,
                ProceduralAudioClipFactoryChainItem.class);
    }

    @Override
    protected ProceduralFactoryInfo getInfoFor(ProceduralAudioClipFactoryChainItem factory) {
        return new ProceduralFactoryInfo(factory.getProceduralClipId(), factory.getProceduralClipName());
    }
}
