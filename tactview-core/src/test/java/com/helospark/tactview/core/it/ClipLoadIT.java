package com.helospark.tactview.core.it;

import java.io.File;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.helospark.lightdi.LightDiContext;
import com.helospark.tactview.core.decoder.VisualMediaMetadata;
import com.helospark.tactview.core.it.util.IntegrationTestUtil;
import com.helospark.tactview.core.it.util.parameterresolver.DownloadedResourceName;
import com.helospark.tactview.core.it.util.parameterresolver.TestResourceParameterResolver;
import com.helospark.tactview.core.it.util.ui.FakeUi;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.timeline.TimelineManagerFramesRequest;
import com.helospark.tactview.core.timeline.TimelineManagerRenderService;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.VideoClip;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ImageClip;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;

@ExtendWith(TestResourceParameterResolver.class)
public class ClipLoadIT {
    private LightDiContext lightDi;
    private FakeUi fakeUi;
    private TimelineManagerAccessor timelineManager;
    private TimelineManagerRenderService timelineManagerRenderService;

    @BeforeEach
    public void init() {
        lightDi = IntegrationTestUtil.startContext();
        fakeUi = lightDi.getBean(FakeUi.class);
        timelineManager = lightDi.getBean(TimelineManagerAccessor.class);
        timelineManagerRenderService = lightDi.getBean(TimelineManagerRenderService.class);
    }

    @AfterEach
    public void destroy() {
        lightDi.close();
    }

    @Test
    public void test4kh264Video(@DownloadedResourceName("4k_h264_beaches.mp4") File testFile) {
        VideoClip videoClip = (VideoClip) fakeUi.dragFileToTimeline(testFile, TimelinePosition.ofZero());

        ReadOnlyClipImage videoFrame = getFrame(timelineManager, videoClip.getMediaMetadata(), 0.1, TimelinePosition.ofSeconds(1));

        ClipImage expected = IntegrationTestUtil.loadTestClasspathImage("clipit/beach_at_1s_and_0.1_scale.png");
        IntegrationTestUtil.assertFrameEquals(videoFrame, expected, "Video frames not equal");
    }

    @Test
    public void testWebmVideo(@DownloadedResourceName("fire.webm") File testFile) {
        VideoClip videoClip = (VideoClip) fakeUi.dragFileToTimeline(testFile, TimelinePosition.ofZero());

        ReadOnlyClipImage videoFrame = getFrame(timelineManager, videoClip.getMediaMetadata(), 0.5, TimelinePosition.ofSeconds(1));

        ClipImage expected = IntegrationTestUtil.loadTestClasspathImage("clipit/fire_at_1s_and_0.1_scale.png");
        IntegrationTestUtil.assertFrameEquals(videoFrame, expected, "Video frames not equal");
    }

    @Test
    public void testJpegImage(@DownloadedResourceName("nightsky.jpeg") File testFile) {
        ImageClip videoClip = (ImageClip) fakeUi.dragFileToTimeline(testFile, TimelinePosition.ofZero());

        ReadOnlyClipImage imageFrameAt1s = getFrame(timelineManager, videoClip.getMediaMetadata(), 0.1, TimelinePosition.ofSeconds(1));
        ReadOnlyClipImage imageFrameAt3s = getFrame(timelineManager, videoClip.getMediaMetadata(), 0.1, TimelinePosition.ofSeconds(3));

        ClipImage expected = IntegrationTestUtil.loadTestClasspathImage("clipit/nightsky_0.1_scale.png");
        IntegrationTestUtil.assertFrameEquals(imageFrameAt1s, expected, "Video frames not equal");
        IntegrationTestUtil.assertFrameEquals(imageFrameAt3s, expected, "Video frames not equal");
    }

    @Test
    public void testGifAnimation(@DownloadedResourceName("earth.gif") File testFile) {
        VideoClip videoClip = (VideoClip) fakeUi.dragFileToTimeline(testFile, TimelinePosition.ofZero());

        ReadOnlyClipImage imageFrame = getFrame(timelineManager, videoClip.getMediaMetadata(), 0.5, TimelinePosition.ofSeconds(1));

        ClipImage expected = IntegrationTestUtil.loadTestClasspathImage("clipit/earth_gif_at_1s.png");
        IntegrationTestUtil.assertFrameEquals(imageFrame, expected, "Video frames not equal");
    }

    @Test
    public void testOgvVideo(@DownloadedResourceName("earth.ogv") File testFile) {
        VideoClip videoClip = (VideoClip) fakeUi.dragFileToTimeline(testFile, TimelinePosition.ofZero());

        ReadOnlyClipImage imageFrame = getFrame(timelineManager, videoClip.getMediaMetadata(), 0.1, new TimelinePosition(1.0));

        ClipImage expected = IntegrationTestUtil.loadTestClasspathImage("clipit/earth_ogv_at_1s.png");
        IntegrationTestUtil.assertFrameEquals(imageFrame, expected, "Video frames not equal");
    }

    private ReadOnlyClipImage getFrame(TimelineManagerAccessor timelineManager, VisualMediaMetadata metadata, double scale, TimelinePosition timelinePosition) {
        TimelineManagerFramesRequest frameRequest = TimelineManagerFramesRequest.builder()
                .withNeedSound(false)
                .withPosition(timelinePosition)
                .withPreviewWidth((int) (scale * metadata.getWidth()))
                .withPreviewHeight((int) (scale * metadata.getHeight()))
                .withScale(scale)
                .build();
        var frame = timelineManagerRenderService.getFrame(frameRequest);

        ReadOnlyClipImage videoFrame = frame.getAudioVideoFragment().getVideoResult();
        return videoFrame;
    }

}
