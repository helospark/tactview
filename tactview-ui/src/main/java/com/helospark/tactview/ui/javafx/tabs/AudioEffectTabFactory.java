package com.helospark.tactview.ui.javafx.tabs;

import com.helospark.lightdi.LightDiContext;
import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.annotation.Order;
import com.helospark.tactview.core.timeline.effect.TimelineEffectType;
import com.helospark.tactview.ui.javafx.uicomponents.detailsdata.localizeddetail.LocalizedDetailRepository;

@Component
@Order(4)
public class AudioEffectTabFactory extends AbstractEffectTabFactory {

    public AudioEffectTabFactory(LightDiContext lightDi, DraggableIconFactory iconFactory, LocalizedDetailRepository localizedDetailRepository) {
        super(lightDi, iconFactory, localizedDetailRepository, TimelineEffectType.AUDIO_EFFECT, "audio effects", "effect-view");
    }

}
