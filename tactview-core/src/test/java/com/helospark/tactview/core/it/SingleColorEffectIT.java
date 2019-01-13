package com.helospark.tactview.core.it;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.jupiter.api.Test;

import com.helospark.lightdi.LightDiContext;
import com.helospark.tactview.core.it.util.IntegrationTestUtil;
import com.helospark.tactview.core.it.util.ui.FakeUi;
import com.helospark.tactview.core.timeline.AudioVideoFragment;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Color;

public class SingleColorEffectIT {

    @Test
    public void testSingleColorEffect() {
        LightDiContext lightDi = IntegrationTestUtil.startContext();
        FakeUi fakeUi = lightDi.getBean(FakeUi.class);

        fakeUi.dragProceduralClipToFirstChannel("singlecolor", TimelinePosition.ofZero());

        AudioVideoFragment frame = fakeUi.requestPreviewVideoFrame(TimelinePosition.ofZero());

        assertFrameOfColor(frame, 255, 255, 255, 255);
        lightDi.close();
    }

    @Test
    public void testSingleColorEffectWithAddedKeyframes() {
        LightDiContext lightDi = IntegrationTestUtil.startContext();
        FakeUi fakeUi = lightDi.getBean(FakeUi.class);

        TimelineClip addedClip = fakeUi.dragProceduralClipToFirstChannel("singlecolor", TimelinePosition.ofZero());

        fakeUi.enableKeyframesFor(addedClip.getId(), "color");
        fakeUi.setKeyframeForColorDescriptor(addedClip.getId(), "color", TimelinePosition.ofZero(), new Color(1.0, 1.0, 1.0));
        fakeUi.setKeyframeForColorDescriptor(addedClip.getId(), "color", TimelinePosition.ofSeconds(10), new Color(0.0, 1.0, 1.0));

        AudioVideoFragment frameAtZero = fakeUi.requestPreviewVideoFrame(TimelinePosition.ofZero());
        AudioVideoFragment frameAtHalfway = fakeUi.requestPreviewVideoFrame(TimelinePosition.ofSeconds(5));
        AudioVideoFragment frameAtEnd = fakeUi.requestPreviewVideoFrame(TimelinePosition.ofSeconds(10));

        assertFrameOfColor(frameAtZero, 255, 255, 255, 255);
        assertFrameOfColor(frameAtHalfway, 127, 255, 255, 255);
        assertFrameOfColor(frameAtEnd, 0, 255, 255, 255);

        lightDi.close();
    }

    @Test
    public void testSingleColorEffectWhenKeyframesAreNotEnabledShouldOnlyKeepLast() {
        LightDiContext lightDi = IntegrationTestUtil.startContext();
        FakeUi fakeUi = lightDi.getBean(FakeUi.class);

        TimelineClip addedClip = fakeUi.dragProceduralClipToFirstChannel("singlecolor", TimelinePosition.ofZero());

        fakeUi.disableKeyframesFor(addedClip.getId(), "color");
        fakeUi.setKeyframeForColorDescriptor(addedClip.getId(), "color", TimelinePosition.ofZero(), new Color(1.0, 1.0, 1.0));
        fakeUi.setKeyframeForColorDescriptor(addedClip.getId(), "color", TimelinePosition.ofSeconds(10), new Color(0.0, 1.0, 1.0));

        AudioVideoFragment frameAtZero = fakeUi.requestPreviewVideoFrame(TimelinePosition.ofZero());
        AudioVideoFragment frameAtHalfway = fakeUi.requestPreviewVideoFrame(TimelinePosition.ofSeconds(5));
        AudioVideoFragment frameAtEnd = fakeUi.requestPreviewVideoFrame(TimelinePosition.ofSeconds(10));

        assertFrameOfColor(frameAtZero, 0, 255, 255, 255);
        assertFrameOfColor(frameAtHalfway, 0, 255, 255, 255);
        assertFrameOfColor(frameAtEnd, 0, 255, 255, 255);

        lightDi.close();
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
