package com.helospark.tactview.core.timeline.blendmode;

import java.util.Collections;
import java.util.List;

import com.helospark.lightdi.LightDiContext;
import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.aware.ContextAware;

@Component
public class BlendModeStrategyAccessor implements ContextAware {
    private static LightDiContext context = null;

    public static List<BlendModeStrategy> getStrategies() {
        if (context == null) {
            return Collections.emptyList();
        } else {
            return context.getListOfBeans(BlendModeStrategy.class);
        }
    }

    @Override
    public void setContext(LightDiContext context) {
        BlendModeStrategyAccessor.context = context;
    }

}
