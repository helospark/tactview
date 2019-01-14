package com.helospark.tactview.core.it;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.lightdi.LightDiContext;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.decoder.framecache.GlobalMemoryManagerAccessor;
import com.helospark.tactview.core.it.util.IntegrationTestUtil;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.StatelessVideoEffect;
import com.helospark.tactview.core.timeline.TimelineClipType;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.CreateEffectRequest;
import com.helospark.tactview.core.timeline.effect.EffectFactory;
import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.util.StaticObjectMapper;

public class CommonEffectTest {

    @Test
    public void testThatEffectIsGeneratingSameImageAfterCloning() {
        LightDiContext lightDi = IntegrationTestUtil.startContext();
        List<EffectFactory> effectFactories = lightDi.getListOfBeans(EffectFactory.class);

        for (var effectFactory : effectFactories) {
            StatelessEffect effect = effectFactory.createEffect(new CreateEffectRequest(TimelinePosition.ofZero(), effectFactory.getEffectId(), TimelineClipType.VIDEO));
            if (!(effect instanceof StatelessVideoEffect)) {
                continue;
            }

            ReadOnlyClipImage originalFrame = getFrame((StatelessVideoEffect) effect);

            StatelessVideoEffect clonedEffect = (StatelessVideoEffect) effect.cloneEffect(CloneRequestMetadata.ofDefault());

            ReadOnlyClipImage clonedFrame = getFrame(clonedEffect);

            IntegrationTestUtil.assertFrameEquals(originalFrame, clonedFrame, effectFactory.getEffectId() + " is generating different image after clone");

            freeFrame(originalFrame);
            freeFrame(clonedFrame);
        }
        lightDi.close();
    }

    @Test
    public void testThatEffectCanBeSavedAndRestored() throws IOException {
        LightDiContext lightDi = IntegrationTestUtil.startContext();
        List<EffectFactory> effectFactories = lightDi.getListOfBeans(EffectFactory.class);

        for (var effectFactory : effectFactories) {
            StatelessEffect effect = effectFactory.createEffect(new CreateEffectRequest(TimelinePosition.ofZero(), effectFactory.getEffectId(), TimelineClipType.VIDEO));
            if (!(effect instanceof StatelessVideoEffect)) {
                continue;
            }

            ReadOnlyClipImage originalFrame = getFrame((StatelessVideoEffect) effect);

            Object savedEffect = effect.generateSavedContent();
            String saveData = StaticObjectMapper.objectMapper.writeValueAsString(savedEffect);

            JsonNode readData = StaticObjectMapper.objectMapper.readTree(saveData);

            StatelessEffect restoredClip = effectFactory.restoreEffect(readData, new LoadMetadata("filepath"));

            ReadOnlyClipImage clonedFrame = getFrame((StatelessVideoEffect) restoredClip);

            IntegrationTestUtil.assertFrameEquals(originalFrame, clonedFrame, effectFactory.getEffectId() + " is generating different image after save and restore");

            freeFrame(originalFrame);
            freeFrame(clonedFrame);
        }
        lightDi.close();
    }

    private ReadOnlyClipImage getFrame(StatelessVideoEffect effect) {
        ClipImage clipImage = ClipImage.fromSize(600, 400);

        for (int i = 0; i < clipImage.getHeight(); ++i) {
            for (int j = 0; j < clipImage.getWidth(); ++j) {
                clipImage.setRed(127, j, i);
                clipImage.setGreen(127, j, i);
                clipImage.setBlue(127, j, i);
                clipImage.setAlpha(127, j, i);
            }
        }

        StatelessEffectRequest request = StatelessEffectRequest.builder()
                .withCanvasWidth(600)
                .withCanvasHeight(400)
                .withClipPosition(TimelinePosition.ofZero())
                .withCurrentFrame(clipImage)
                .withCurrentTimelineClip(null)
                .withEffectChannel(0)
                .withEffectPosition(TimelinePosition.ofZero())
                .withScale(1.0)
                .build();

        ReadOnlyClipImage result = effect.createFrame(request);

        freeFrame(clipImage);

        return result;
    }

    private void freeFrame(ReadOnlyClipImage clonedFrame) {
        GlobalMemoryManagerAccessor.memoryManager.returnBuffer(clonedFrame.getBuffer());
    }

}
