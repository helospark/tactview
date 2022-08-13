package com.helospark.tactview.core.it.expression;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.helospark.lightdi.LightDiContext;
import com.helospark.tactview.core.it.util.IntegrationTestUtil;
import com.helospark.tactview.core.it.util.ui.FakePreviewRenderer;
import com.helospark.tactview.core.it.util.ui.FakeUi;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;

public class ExpressionIT {
    private LightDiContext lightDi;
    private FakeUi fakeUi;
    private TimelineManagerAccessor timelineManager;
    private FakePreviewRenderer fakePreviewRenderer;

    @BeforeEach
    public void init() {
        lightDi = IntegrationTestUtil.startContext();
        fakeUi = lightDi.getBean(FakeUi.class);
        timelineManager = lightDi.getBean(TimelineManagerAccessor.class);
        fakePreviewRenderer = lightDi.getBean(FakePreviewRenderer.class);
    }

    @Test
    public void testLoadImage() {
        fakeUi.loadFile("classpath:/savefiles/expression/expression_1.tvs");

        ReadOnlyClipImage videoFrame = fakePreviewRenderer.renderFrame(timelineManager, 0.1, TimelinePosition.ofSeconds(1));

        ClipImage expected = IntegrationTestUtil.loadTestClasspathImage("savefiles/expression/expression_1.png");
        IntegrationTestUtil.assertFrameEquals(videoFrame, expected, "Video frames not equal");
    }

}
