package com.helospark.tactview.core.it;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.helospark.lightdi.LightDiContext;
import com.helospark.tactview.core.it.util.IntegrationTestUtil;
import com.helospark.tactview.core.it.util.ui.ColorWithAlpha;
import com.helospark.tactview.core.it.util.ui.FakeUi;
import com.helospark.tactview.core.timeline.AudioVideoFragment;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineManagerFramesRequest;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Color;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.InterpolationLine;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;

public class RectangleProceduralClipIT {
    private LightDiContext lightDi;
    private FakeUi fakeUi;

    @BeforeEach
    public void setUp() {
        lightDi = IntegrationTestUtil.startContext();
        fakeUi = lightDi.getBean(FakeUi.class);
    }

    @AfterEach
    public void tearDown() {
        lightDi.close();
    }

    @Test
    public void testRectangleProvider() {
        TimelineClip clip = fakeUi.dragProceduralClipToFirstChannel("rectangle", TimelinePosition.ofZero());

        fakeUi.selectClipAndFindSettingByName(clip.getId(), "Color")
                .addKeyframe(Color.of(1.0, 0.0, 0.0));

        TimelineManagerFramesRequest frameRequest = IntegrationTestUtil.getDefaultFrameRequest().build();
        AudioVideoFragment frame = fakeUi.requestFrame(frameRequest);

        ReadOnlyClipImage videoFrame = frame.getVideoResult();

        IntegrationTestUtil.pixelEquals(videoFrame, 300, 200, new ColorWithAlpha(255, 0, 0, 255));
        IntegrationTestUtil.pixelEquals(videoFrame, 0, 0, new ColorWithAlpha(0, 0, 0, 0));
    }

    @Test
    public void testRectangleProviderWithFullscreen() {
        TimelineClip clip = fakeUi.dragProceduralClipToFirstChannel("rectangle", TimelinePosition.ofZero());

        fakeUi.selectClipAndFindSettingByName(clip.getId(), "Area")
                .addKeyframe(new InterpolationLine(new Point(0.0, 0.0), new Point(1.0, 1.0)));
        fakeUi.selectClipAndFindSettingByName(clip.getId(), "Fuzziness")
                .addKeyframe(0.5);
        fakeUi.selectClipAndFindSettingByName(clip.getId(), "Color")
                .addKeyframe(new Color(1.0, 0.0, 0.0));

        TimelineManagerFramesRequest frameRequest = IntegrationTestUtil.getDefaultFrameRequest().build();
        AudioVideoFragment frame = fakeUi.requestFrame(frameRequest);

        ReadOnlyClipImage videoFrame = frame.getVideoResult();

        IntegrationTestUtil.pixelEquals(videoFrame, 0, 200, new ColorWithAlpha(0, 0, 0, 0));
        IntegrationTestUtil.pixelEquals(videoFrame, 300, 200, new ColorWithAlpha(255, 0, 0, 255));
        IntegrationTestUtil.pixelEquals(videoFrame, 100, 200, new ColorWithAlpha(127, 0, 0, 127));
    }
}
