package com.helospark.tactview.core.it;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.helospark.lightdi.LightDiContext;
import com.helospark.tactview.core.it.util.IntegrationTestUtil;
import com.helospark.tactview.core.it.util.ui.FakeUi;
import com.helospark.tactview.core.timeline.AudioVideoFragment;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Color;

public class SingleColorEffectIT {
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
    public void testSingleColorEffect() {
        fakeUi.dragProceduralClipToFirstChannel("singlecolor", TimelinePosition.ofZero());

        AudioVideoFragment frame = fakeUi.requestPreviewVideoFrame(TimelinePosition.ofZero());

        assertFrameOfColor(frame, 255, 255, 255, 255);
    }

    @Test
    public void testSingleColorEffectWithAddedKeyframes() {
        TimelineClip addedClip = fakeUi.dragProceduralClipToFirstChannel("singlecolor", TimelinePosition.ofZero());

        fakeUi.selectClipAndFindSettingByName(addedClip.getId(), "color")
                .enableKeyframes()
                .moveToPosition(TimelinePosition.ofZero())
                .addKeyframe(new Color(1.0, 1.0, 1.0))
                .moveToPosition(TimelinePosition.ofSeconds(10))
                .addKeyframe(new Color(0.0, 1.0, 1.0));

        AudioVideoFragment frameAtZero = fakeUi.requestPreviewVideoFrame(TimelinePosition.ofZero());
        AudioVideoFragment frameAtHalfway = fakeUi.requestPreviewVideoFrame(TimelinePosition.ofSeconds(5));
        AudioVideoFragment frameAtEnd = fakeUi.requestPreviewVideoFrame(TimelinePosition.ofSeconds(10));

        assertFrameOfColor(frameAtZero, 255, 255, 255, 255);
        assertFrameOfColor(frameAtHalfway, 127, 255, 255, 255);
        assertFrameOfColor(frameAtEnd, 0, 255, 255, 255);
    }

    @Test
    public void testSingleColorEffectWhenKeyframesAreNotEnabledShouldOnlyKeepLast() {
        TimelineClip addedClip = fakeUi.dragProceduralClipToFirstChannel("singlecolor", TimelinePosition.ofZero());

        fakeUi.selectClipAndFindSettingByName(addedClip.getId(), "color")
                .disableKeyframes()
                .moveToPosition(TimelinePosition.ofZero())
                .addKeyframe(new Color(1.0, 1.0, 1.0))
                .moveToPosition(TimelinePosition.ofSeconds(10))
                .addKeyframe(new Color(0.0, 1.0, 1.0));

        AudioVideoFragment frameAtZero = fakeUi.requestPreviewVideoFrame(TimelinePosition.ofZero());
        AudioVideoFragment frameAtHalfway = fakeUi.requestPreviewVideoFrame(TimelinePosition.ofSeconds(5));
        AudioVideoFragment frameAtEnd = fakeUi.requestPreviewVideoFrame(TimelinePosition.ofSeconds(10));

        assertFrameOfColor(frameAtZero, 0, 255, 255, 255);
        assertFrameOfColor(frameAtHalfway, 0, 255, 255, 255);
        assertFrameOfColor(frameAtEnd, 0, 255, 255, 255);
    }

    private void assertFrameOfColor(AudioVideoFragment frame, int red, int green, int blue, int alpha) {
        assertThat(frame.getVideoResult().getWidth(), is(600));
        assertThat(frame.getVideoResult().getHeight(), is(400));

        for (int i = 0; i < frame.getVideoResult().getHeight(); ++i) {
            for (int j = 0; j < frame.getVideoResult().getWidth(); ++j) {
                assertThat(frame.getVideoResult().getRed(j, i), is(red));
                assertThat(frame.getVideoResult().getGreen(j, i), is(green));
                assertThat(frame.getVideoResult().getBlue(j, i), is(blue));
                assertThat(frame.getVideoResult().getAlpha(j, i), is(alpha));
            }
        }
    }
}
