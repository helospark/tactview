package com.helospark.tactview.core.it.util;

import java.util.Collections;

import com.helospark.lightdi.LightDi;
import com.helospark.lightdi.LightDiContext;
import com.helospark.lightdi.LightDiContextConfiguration;
import com.helospark.tactview.core.TactViewCoreConfiguration;
import com.helospark.tactview.core.util.jpaplugin.JnaLightDiPlugin;

public class IntegrationTestUtil {

    public static LightDiContext startContext() {
        LightDiContextConfiguration configuration = LightDiContextConfiguration.builder()
                .withThreadNumber(4)
                .withCheckForIntegrity(true)
                .withAdditionalDependencies(Collections.singletonList(new JnaLightDiPlugin()))
                .build();
        LightDiContext lightDi = LightDi.initContextByClass(TactViewCoreConfiguration.class, configuration);
        lightDi.eagerInitAllBeans();

        return lightDi;
    }

}
