package com.helospark.tactview.core.it;

import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.helospark.lightdi.LightDiContext;
import com.helospark.tactview.core.it.util.IntegrationTestUtil;
import com.helospark.tactview.core.it.util.ui.FakeUi;
import com.helospark.tactview.core.timeline.ITimelineClip;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelinePosition;

public class SpecialPositionMoveEffectIT {
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
    public void testDragClipShouldSetClipToCursorPosition() {
        fakeUi.setCursorTo(TimelinePosition.ofSeconds(60));
        TimelineClip clip = fakeUi.dragProceduralClipToFirstChannel("singlecolor", TimelinePosition.ofZero());

        fakeUi.dragClip(clip)
                .dragTo(TimelinePosition.ofSeconds(59.5));
        assertThatClipIsAt(clip, 60);

        fakeUi.dragClip(clip)
                .dragTo(TimelinePosition.ofSeconds(29.5));
        assertThatClipIsAt(clip, 30);

        fakeUi.dragClip(clip)
                .dragTo(TimelinePosition.ofSeconds(30));
        assertThatClipIsAt(clip, 30);

        fakeUi.dragClip(clip)
                .dragTo(TimelinePosition.ofSeconds(200));
        assertThatClipIsAt(clip, 200);
    }

    @Test
    public void testDragClipShouldJumpToPositionOfOtherClip() {
        TimelineClip clip1 = fakeUi.dragProceduralClipToChannel("singlecolor", TimelinePosition.ofSeconds(60), 0);
        TimelineClip clip2 = fakeUi.dragProceduralClipToChannel("singlecolor", TimelinePosition.ofSeconds(0), 1);

        fakeUi.dragClip(clip2)
                .dragTo(TimelinePosition.ofSeconds(59.5));
        assertThatClipIsAt(clip2, 60);

        fakeUi.dragClip(clip2)
                .dragTo(TimelinePosition.ofSeconds(90.5));
        assertThatClipIsAt(clip2, 90);
    }

    private void assertThatClipIsAt(TimelineClip clip, double position) {
        assertTrue("Clip is expected to be at " + position + " but was at " + clip.getInterval().getStartPosition().getSeconds(),
                clip.getInterval().getStartPosition().distanceFrom(TimelinePosition.ofSeconds(position)).doubleValue() < 0.001);
    }

}
