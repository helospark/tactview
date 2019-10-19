package com.helospark.tactview.ui.javafx.tabs;

import com.helospark.lightdi.LightDiContext;
import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.annotation.Order;
import com.helospark.tactview.core.timeline.effect.TimelineEffectType;
import com.helospark.tactview.ui.javafx.uicomponents.detailsdata.localizeddetail.LocalizedDetailRepositoryChain;

@Component
@Order(3)
public class VideoTransitionEffectTabFactory extends AbstractEffectTabFactory {

    public VideoTransitionEffectTabFactory(LightDiContext lightDi, DraggableIconFactory iconFactory, LocalizedDetailRepositoryChain localizedDetailRepository) {
        super(lightDi, iconFactory, localizedDetailRepository, TimelineEffectType.VIDEO_TRANSITION, "transition", "effect-view");
    }

}
