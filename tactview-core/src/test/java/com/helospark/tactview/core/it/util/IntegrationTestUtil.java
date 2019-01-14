package com.helospark.tactview.core.it.util;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.Collections;

import com.helospark.lightdi.LightDi;
import com.helospark.lightdi.LightDiContext;
import com.helospark.lightdi.LightDiContextConfiguration;
import com.helospark.tactview.core.TactViewCoreConfiguration;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.util.DebugImageRenderer;
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

    public static void assertFrameEquals(ReadOnlyClipImage firstFrame, ReadOnlyClipImage secondFrame, String errorMessage) {
        if (!firstFrame.isSameSizeAs(secondFrame)) {
            failOnImageError(firstFrame, secondFrame, errorMessage);
        }
        for (int y = 0; y < firstFrame.getHeight(); ++y) {
            for (int x = 0; x < secondFrame.getWidth(); ++x) {
                for (int i = 0; i < 4; ++i) {
                    int color1 = firstFrame.getColorComponentWithOffset(x, y, i);
                    int color2 = secondFrame.getColorComponentWithOffset(x, y, i);
                    if (color1 != color2) {
                        failOnImageError(firstFrame, secondFrame, errorMessage);
                    }
                }
            }
        }
    }

    private static void failOnImageError(ReadOnlyClipImage firstFrame, ReadOnlyClipImage secondFrame, String errorMessage) {
        DebugImageRenderer.render(firstFrame);
        DebugImageRenderer.render(secondFrame);
        fail(errorMessage + "\nImages saved, see URLs in log");
    }

}
