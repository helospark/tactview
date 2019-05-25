package com.helospark.tactview.core.it;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.helospark.lightdi.LightDiContext;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.it.util.IntegrationTestUtil;
import com.helospark.tactview.core.it.util.ui.FakeUi;
import com.helospark.tactview.core.timeline.AudioVideoFragment;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.proceduralclip.ProceduralClipFactoryChainItem;

public class CommonFrameTest {
    LightDiContext lightDi;
    FakeUi fakeUi;
    TimelineManagerAccessor timelineManagerAccessor;

    @BeforeEach
    public void setUp() {
        lightDi = IntegrationTestUtil.startContext();
        fakeUi = lightDi.getBean(FakeUi.class);
        timelineManagerAccessor = lightDi.getBean(TimelineManagerAccessor.class);
    }

    @AfterEach
    public void tearDown() {
        lightDi.close();
    }

    @Test
    public void testThatProceduralClipGeneratesTheSameImageAfterCloning() {
        List<String> proceduralClipIds = lightDi.getListOfBeans(ProceduralClipFactoryChainItem.class)
                .stream()
                .map(a -> a.getProceduralClipId())
                .collect(Collectors.toList());

        for (var proceduralClipId : proceduralClipIds) {
            TimelineClip addedClip = fakeUi.dragProceduralClipToFirstChannel(proceduralClipId, TimelinePosition.ofZero());

            AudioVideoFragment expectedFrame = fakeUi.requestPreviewVideoFrame(TimelinePosition.ofSeconds(1.0));

            TimelineClip newClip = addedClip.cloneClip(CloneRequestMetadata.fullCopy());

            timelineManagerAccessor.addClip(timelineManagerAccessor.getChannels().get(1), newClip); // maybe fakeUi.paste

            fakeUi.deleteClip(addedClip.getId());

            AudioVideoFragment actualFrame = fakeUi.requestPreviewVideoFrame(TimelinePosition.ofSeconds(1.0));

            fakeUi.deleteClip(newClip.getId());

            IntegrationTestUtil.assertFrameEquals(actualFrame.getVideoResult(), expectedFrame.getVideoResult(), "Clip is not the same after cloning for clip " + proceduralClipId);

            actualFrame.free();
            expectedFrame.free();
        }
    }

    @Test
    public void testThatProceduralClipGeneratesTheSameImageAfterSavingAndReloading() throws IOException {
        List<String> proceduralClipIds = lightDi.getListOfBeans(ProceduralClipFactoryChainItem.class)
                .stream()
                .map(a -> a.getProceduralClipId())
                .collect(Collectors.toList());

        for (var proceduralClipId : proceduralClipIds) {
            TimelineClip addedClip = fakeUi.dragProceduralClipToFirstChannel(proceduralClipId, TimelinePosition.ofZero());

            AudioVideoFragment expectedFrame = fakeUi.requestPreviewVideoFrame(TimelinePosition.ofSeconds(1.0));

            File file = File.createTempFile("procedural_clip_save_and_load_" + proceduralClipId + "_" + System.currentTimeMillis(), ".tvs");

            fakeUi.clickSaveMenuItem()
                    .selectFile(file);

            fakeUi.deleteClip(addedClip.getId());

            fakeUi.clickLoadMenuItem()
                    .selectFile(file);

            AudioVideoFragment actualFrame = fakeUi.requestPreviewVideoFrame(TimelinePosition.ofSeconds(1.0));

            IntegrationTestUtil.assertFrameEquals(actualFrame.getVideoResult(), expectedFrame.getVideoResult(), "Clip is not the same after saving and loading for clip " + proceduralClipId);

            actualFrame.free();
            expectedFrame.free();

            fakeUi.deleteClip(addedClip.getId()); // loaded clip has same id
            file.delete();
        }
    }

}
