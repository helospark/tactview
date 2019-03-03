package com.helospark.tactview.ui.javafx.tabs;

import com.helospark.lightdi.LightDiContext;
import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.annotation.Order;
import com.helospark.tactview.core.timeline.effect.TimelineEffectType;
import com.helospark.tactview.ui.javafx.uicomponents.detailsdata.localizeddetail.LocalizedDetailRepository;

@Component
@Order(0)
public class VideoEffectTabFactory extends AbstractEffectTabFactory {

    public VideoEffectTabFactory(LightDiContext lightDi, DraggableIconFactory iconFactory, LocalizedDetailRepository localizedDetailRepository) {
        super(lightDi, iconFactory, localizedDetailRepository, TimelineEffectType.VIDEO_EFFECT, "video effect", "effect-view");
    }

}
