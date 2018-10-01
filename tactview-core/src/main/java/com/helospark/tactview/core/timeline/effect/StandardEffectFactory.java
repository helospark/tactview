package com.helospark.tactview.core.timeline.effect;

import java.util.function.Function;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.message.EffectDescriptorsAdded;
import com.helospark.tactview.core.util.messaging.MessagingService;

public class StandardEffectFactory implements EffectFactory {
    private MessagingService messagingService;

    private String supportedEffectId;
    private String name;
    private Function<CreateEffectRequest, StatelessEffect> factory;

    @Generated("SparkTools")
    private StandardEffectFactory(Builder builder) {
        this.messagingService = builder.messagingService;
        this.supportedEffectId = builder.supportedEffectId;
        this.name = builder.name;
        this.factory = builder.factory;
    }

    @Override
    public boolean doesSupport(CreateEffectRequest request) {
        return request.getEffectId().equals(supportedEffectId);
    }

    @Override
    public StatelessEffect createEffect(CreateEffectRequest request) {
        StatelessEffect result = factory.apply(request);

        messagingService.sendAsyncMessage(new EffectDescriptorsAdded(result.getId(), result.getValueProviders()));

        return result;
    }

    @Override
    public String getEffectId() {
        return supportedEffectId;
    }

    @Override
    public String getEffectName() {
        return name;
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private MessagingService messagingService;
        private String supportedEffectId;
        private String name;
        private Function<CreateEffectRequest, StatelessEffect> factory;

        private Builder() {
        }

        public Builder withMessagingService(MessagingService messagingService) {
            this.messagingService = messagingService;
            return this;
        }

        public Builder withSupportedEffectId(String supportedEffectId) {
            this.supportedEffectId = supportedEffectId;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withFactory(Function<CreateEffectRequest, StatelessEffect> factory) {
            this.factory = factory;
            return this;
        }

        public StandardEffectFactory build() {
            return new StandardEffectFactory(this);
        }
    }

}
