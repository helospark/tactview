package com.helospark.tactview.core.it;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

import com.helospark.lightdi.LightDiContext;
import com.helospark.tactview.core.TactViewCoreConfiguration;
import com.helospark.tactview.core.it.util.IntegrationTestUtil;

public class CoreContextStartedIT {

    @Test
    public void testContextShouldStart() {
        LightDiContext lightDi = IntegrationTestUtil.startContext();

        TactViewCoreConfiguration asd = lightDi.getBean(TactViewCoreConfiguration.class);

        assertThat(asd, is(not(nullValue())));
    }

}
