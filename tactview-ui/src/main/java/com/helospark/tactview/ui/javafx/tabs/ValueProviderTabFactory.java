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
@Order(10)
public class ValueProviderTabFactory extends AbstractProceduralClipTabFactory<ProceduralClipFactoryChainItem> {

    public ValueProviderTabFactory(LightDiContext lightDi, DraggableIconFactory iconFactory, LocalizedDetailRepositoryChain localizedDetailRepository, UiCommandInterpreterService commandInterpreter,
            TimelineManagerAccessor timelineManager) {
        super("Value providers", "value-provider-clip-view",
                lightDi,
                iconFactory,
                localizedDetailRepository,
                commandInterpreter,
                timelineManager,
                ProceduralClipFactoryChainItem.class,
                value -> value.getType().equals(TimelineProceduralClipType.VALUE_PROVIDER));
    }

    @Override
    protected ProceduralFactoryInfo getInfoFor(ProceduralClipFactoryChainItem factory) {
        return new ProceduralFactoryInfo(factory.getProceduralClipId(), factory.getProceduralClipName());
    }

    @Override
    public boolean isValueProvider() {
        return true;
    }
}
