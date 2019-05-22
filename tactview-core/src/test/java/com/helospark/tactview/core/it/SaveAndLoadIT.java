package com.helospark.tactview.core.it;

import static com.helospark.tactview.core.it.PictureAssertions.assertFrameOfColorWithDelta;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.helospark.lightdi.LightDiContext;
import com.helospark.tactview.core.it.PictureAssertions.Delta;
import com.helospark.tactview.core.it.util.IntegrationTestUtil;
import com.helospark.tactview.core.it.util.parameterresolver.DownloadedResourceName;
import com.helospark.tactview.core.it.util.parameterresolver.TestResourceParameterResolver;
import com.helospark.tactview.core.it.util.ui.FakeUi;
import com.helospark.tactview.core.timeline.AudioVideoFragment;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.VideoClip;
import com.helospark.tactview.core.util.ClassPathResourceReader;

@ExtendWith(TestResourceParameterResolver.class)
public class SaveAndLoadIT {
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
    public void testLoadSavedVideoShouldLoad() throws IOException {
        File tmpFile = copyTestFileFromClassPathToTmpDirectory();

        fakeUi.clickLoadMenuItem()
                .selectFile(tmpFile);

        AudioVideoFragment frame1 = fakeUi.requestPreviewVideoFrame(TimelinePosition.ofSeconds(0.0));
        assertFrameOfColorWithDelta(frame1, 255, 0, 255, 255, Delta.of(1));

        AudioVideoFragment frame2 = fakeUi.requestPreviewVideoFrame(TimelinePosition.ofSeconds(6.9));
        assertFrameOfColorWithDelta(frame2, 255, 0, 0, 255, Delta.of(1));

        AudioVideoFragment frame3 = fakeUi.requestPreviewVideoFrame(TimelinePosition.ofSeconds(20.0));
        assertFrameOfColorWithDelta(frame3, 0, 0, 255, 255, Delta.of(1));
    }

    @Test
    public void testSaveAndLoadShouldNotChangeImage(@DownloadedResourceName("fire.webm") File testFile) throws IOException {
        VideoClip videoClip = (VideoClip) fakeUi.dragFileToTimeline(testFile, TimelinePosition.ofSeconds(1));
        TimelineClip singleColorClip = fakeUi.dragProceduralClipToChannel("singlecolor", TimelinePosition.ofZero(), 1);

        AudioVideoFragment expectedFrame1 = fakeUi.requestPreviewVideoFrame(TimelinePosition.ofSeconds(0.0));
        AudioVideoFragment expectedFrame2 = fakeUi.requestPreviewVideoFrame(TimelinePosition.ofSeconds(5.0));

        File file = File.createTempFile("sample_save_1_" + System.currentTimeMillis(), ".tvs");

        fakeUi.clickSaveMenuItem()
                .selectFile(file);

        fakeUi.deleteClip(videoClip.getId());
        fakeUi.deleteClip(singleColorClip.getId());

        fakeUi.clickLoadMenuItem()
                .selectFile(file);

        AudioVideoFragment actualFrame1 = fakeUi.requestPreviewVideoFrame(TimelinePosition.ofSeconds(0.0));
        AudioVideoFragment actualFrame2 = fakeUi.requestPreviewVideoFrame(TimelinePosition.ofSeconds(5.0));

        IntegrationTestUtil.assertFrameEquals(actualFrame1.getVideoResult(), expectedFrame1.getVideoResult(), "Video frames not equal");
        IntegrationTestUtil.assertFrameEquals(actualFrame2.getVideoResult(), expectedFrame2.getVideoResult(), "Video frames not equal");
    }

    private File copyTestFileFromClassPathToTmpDirectory() throws IOException {
        ClassPathResourceReader fileReader = lightDi.getBean(ClassPathResourceReader.class);
        byte[] tmpData = fileReader.readClasspathFileToByteArray("sample_save.tvs");

        File tmpFile = File.createTempFile("sample_save", "tvs");

        try (FileOutputStream fileWriter = new FileOutputStream(tmpFile)) {
            fileWriter.write(tmpData);
            return tmpFile;
        }
    }

}
