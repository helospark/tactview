package com.helospark.tactview.core.it;

import static com.helospark.tactview.core.it.PictureAssertions.assertFrameOfColorWithDelta;

import java.io.File;
import java.math.BigDecimal;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.helospark.lightdi.LightDiContext;
import com.helospark.tactview.core.it.PictureAssertions.Delta;
import com.helospark.tactview.core.it.util.IntegrationTestUtil;
import com.helospark.tactview.core.it.util.ui.FakeUi;
import com.helospark.tactview.core.repository.ProjectRepository;
import com.helospark.tactview.core.timeline.AudioVideoFragment;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Color;

// TODO: concurrent
public class RenderAndLoadIT {
    private LightDiContext lightDi;
    private FakeUi fakeUi;
    private ProjectRepository projectRepository;

    @BeforeEach
    public void setUp() {
        lightDi = IntegrationTestUtil.startContext();
        fakeUi = lightDi.getBean(FakeUi.class);
        projectRepository = lightDi.getBean(ProjectRepository.class);
    }

    @AfterEach
    public void tearDown() {
        lightDi.close();
    }

    @ParameterizedTest
    @MethodSource("containerProvider")
    public void testRenderVideoThenDragRenderedVideoAndCheckFrames(String extension) {
        projectRepository.initializeVideo(640, 480, BigDecimal.valueOf(24));

        TimelineClip clip = fakeUi.dragProceduralClipToFirstChannel("singlecolor", TimelinePosition.ofZero());

        fakeUi.selectClipAndFindSettingByName(clip.getId(), "color")
                .enableKeyframes()
                .moveToPosition(TimelinePosition.ofSeconds(0))
                .addKeyframe(Color.of(1.0, 0.0, 0.0))
                .moveToPosition(TimelinePosition.ofSeconds(0.5))
                .addKeyframe(Color.of(1.0, 0.0, 0.0))
                .moveToPosition(TimelinePosition.ofSeconds(1.0))
                .addKeyframe(Color.of(0.0, 0.0, 1.0));

        File renderTargetFile = new File(System.getProperty("java.io.tmpdir") + File.separator + "tactview_render_test." + extension);

        if (renderTargetFile.exists()) {
            renderTargetFile.delete();
        }

        fakeUi.openRenderDialog()
                .setFileName(renderTargetFile.getAbsolutePath())
                .setEndPosition(TimelinePosition.ofSeconds(5.0))
                .clickRender()
                .waitUntilRenderFinishes();

        fakeUi.deleteClip(clip.getId());

        fakeUi.dragFileToTimeline(renderTargetFile, TimelinePosition.ofZero());

        AudioVideoFragment startFrame = fakeUi.requestPreviewVideoFrame(TimelinePosition.ofSeconds(0.2));
        AudioVideoFragment endFrame = fakeUi.requestPreviewVideoFrame(TimelinePosition.ofSeconds(2.5));

        assertFrameOfColorWithDelta(startFrame, 255, 0, 0, 255, Delta.of(5));
        assertFrameOfColorWithDelta(endFrame, 0, 0, 255, 255, Delta.of(5));

        startFrame.free();
        endFrame.free();

        renderTargetFile.delete();
    }

    private static Stream<Arguments> containerProvider() {
        return Stream.of(
                Arguments.of("mp4"),
                // Arguments.of("ogg")// investigation required on ogg decode
                Arguments.of("wmv"),
                Arguments.of("flv"),
                Arguments.of("webm"),
                Arguments.of("avi"));
    }
}
