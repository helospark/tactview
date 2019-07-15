package com.helospark.tactview.core.repository;

import java.util.List;

import javax.annotation.PostConstruct;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.message.EffectAddedMessage;
import com.helospark.tactview.core.timeline.valueprovidereffect.StatelessValueProviderEffect;
import com.helospark.tactview.core.util.messaging.MessagingService;

@Component
public class DynamicValueEffectRepository {
    private MessagingService messagingService;

    private List<StatelessValueProviderEffect> effects;

    public DynamicValueEffectRepository(MessagingService messagingService) {
        this.messagingService = messagingService;
    }

    @PostConstruct
    public void init() {
        messagingService.register(EffectAddedMessage.class, message -> {
            if (message.getEffect() instanceof StatelessValueProviderEffect) {
                effects.add((StatelessValueProviderEffect) message.getEffect());
            }
        });
    }

}
