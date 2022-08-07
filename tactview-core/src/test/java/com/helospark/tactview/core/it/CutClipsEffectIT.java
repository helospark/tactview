package com.helospark.tactview.core.it;

import static com.helospark.tactview.core.it.PictureAssertions.assertFrameOfColor;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.helospark.lightdi.LightDiContext;
import com.helospark.tactview.core.it.util.IntegrationTestUtil;
import com.helospark.tactview.core.it.util.ui.FakeUi;
import com.helospark.tactview.core.timeline.AudioVideoFragment;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Color;

public class CutClipsEffectIT {
    private LightDiContext lightDi;
    private FakeUi fakeUi;
    private TimelineManagerAccessor timelineManager;

    @BeforeEach
    public void setUp() {
        lightDi = IntegrationTestUtil.startContext();
        fakeUi = lightDi.getBean(FakeUi.class);
        timelineManager = lightDi.getBean(TimelineManagerAccessor.class);
    }

    @AfterEach
    public void tearDown() {
        lightDi.close();
    }

    @Test
    public void testCutEffectWithoutKeyframes() {
        // GIVEN
        TimelineClip clip = fakeUi.dragProceduralClipToFirstChannel("singlecolor", TimelinePosition.ofZero());

        // WHEN
        timelineManager.cutClip(clip.getId(), TimelinePosition.ofSeconds(5));

        // THEN
        assertThat(timelineManager.getAllClipIds().size(), is(2));
        AudioVideoFragment frame1 = fakeUi.requestPreviewVideoFrame(TimelinePosition.ofSeconds(2));
        assertFrameOfColor(frame1, 255, 255, 255, 255);
        AudioVideoFragment frame2 = fakeUi.requestPreviewVideoFrame(TimelinePosition.ofSeconds(5));
        assertFrameOfColor(frame2, 255, 255, 255, 255);
        AudioVideoFragment frame3 = fakeUi.requestPreviewVideoFrame(TimelinePosition.ofSeconds(7));
        assertFrameOfColor(frame3, 255, 255, 255, 255);
    }

    @Test
    public void testCutEffectWithoutOnPrecisePosition() {
        // GIVEN
        TimelineClip clip = fakeUi.dragProceduralClipToFirstChannel("singlecolor", TimelinePosition.ofZero());

        // WHEN
        timelineManager.cutClip(clip.getId(), TimelinePosition.ofSeconds(5.123456));

        // THEN
        assertThat(timelineManager.getAllClipIds().size(), is(2));
        AudioVideoFragment frame1 = fakeUi.requestPreviewVideoFrame(TimelinePosition.ofSeconds(2));
        assertFrameOfColor(frame1, 255, 255, 255, 255);
    }

    @Test
    public void testCutEffectWithKeyframes() {
        // GIVEN
        TimelineClip clip = fakeUi.dragProceduralClipToFirstChannel("singlecolor", TimelinePosition.ofZero());
        fakeUi.selectClipAndFindSettingByName(clip.getId(), "color")
                .enableKeyframes()
                .moveToPosition(TimelinePosition.ofSeconds(0))
                .addKeyframe(new Color(1.0, 1.0, 1.0))
                .moveToPosition(TimelinePosition.ofSeconds(1))
                .addKeyframe(new Color(1.0, 1.0, 1.0))
                .moveToPosition(TimelinePosition.ofSeconds(9))
                .addKeyframe(new Color(0.0, 1.0, 1.0));

        // WHEN
        timelineManager.cutClip(clip.getId(), TimelinePosition.ofSeconds(5));

        // THEN
        assertThat(timelineManager.getAllClipIds().size(), is(2));
        AudioVideoFragment frame1 = fakeUi.requestPreviewVideoFrame(TimelinePosition.ofSeconds(0.5));
        assertFrameOfColor(frame1, 255, 255, 255, 255);

        AudioVideoFragment frame2 = fakeUi.requestPreviewVideoFrame(TimelinePosition.ofSeconds(1.0));
        assertFrameOfColor(frame2, 255, 255, 255, 255);

        AudioVideoFragment frame3 = fakeUi.requestPreviewVideoFrame(TimelinePosition.ofSeconds(5.0));
        assertFrameOfColor(frame3, 127, 255, 255, 255);

        AudioVideoFragment frame4 = fakeUi.requestPreviewVideoFrame(TimelinePosition.ofSeconds(9.0));
        assertFrameOfColor(frame4, 0, 255, 255, 255);
    }

    @Test
    public void testCutMultipleCuts() {
        // GIVEN
        TimelineClip clip = fakeUi.dragProceduralClipToFirstChannel("singlecolor", TimelinePosition.ofZero());
        fakeUi.selectClipAndFindSettingByName(clip.getId(), "color")
                .enableKeyframes()
                .moveToPosition(TimelinePosition.ofSeconds(0))
                .addKeyframe(new Color(1.0, 1.0, 1.0))
                .moveToPosition(TimelinePosition.ofSeconds(1))
                .addKeyframe(new Color(1.0, 1.0, 1.0))
                .moveToPosition(TimelinePosition.ofSeconds(9))
                .addKeyframe(new Color(0.0, 1.0, 1.0));

        // WHEN
        List<TimelineClip> cuttedParts = timelineManager.cutClip(clip.getId(), TimelinePosition.ofSeconds(5));
        cuttedParts = timelineManager.cutClip(cuttedParts.get(1).getId(), TimelinePosition.ofSeconds(7.5));
        cuttedParts = timelineManager.cutClip(cuttedParts.get(1).getId(), TimelinePosition.ofSeconds(8.5));

        // THEN
        assertThat(timelineManager.getAllClipIds().size(), is(4));
        AudioVideoFragment frame1 = fakeUi.requestPreviewVideoFrame(TimelinePosition.ofSeconds(0.5));
        assertFrameOfColor(frame1, 255, 255, 255, 255);

        AudioVideoFragment frame2 = fakeUi.requestPreviewVideoFrame(TimelinePosition.ofSeconds(1.0));
        assertFrameOfColor(frame2, 255, 255, 255, 255);

        AudioVideoFragment frame3 = fakeUi.requestPreviewVideoFrame(TimelinePosition.ofSeconds(5.0));
        assertFrameOfColor(frame3, 127, 255, 255, 255);

        AudioVideoFragment frame4 = fakeUi.requestPreviewVideoFrame(TimelinePosition.ofSeconds(9.0));
        assertFrameOfColor(frame4, 0, 255, 255, 255);
    }
}
