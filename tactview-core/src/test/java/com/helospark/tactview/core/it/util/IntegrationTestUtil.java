package com.helospark.tactview.core.it.util;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import javax.imageio.ImageIO;

import com.helospark.lightdi.LightDi;
import com.helospark.lightdi.LightDiContext;
import com.helospark.lightdi.LightDiContextConfiguration;
import com.helospark.tactview.core.it.util.ui.ColorWithAlpha;
import com.helospark.tactview.core.timeline.TimelineManagerFramesRequest;
import com.helospark.tactview.core.timeline.TimelineManagerFramesRequest.Builder;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.util.DebugImageRenderer;
import com.helospark.tactview.core.util.jpaplugin.JnaLightDiPlugin;

public class IntegrationTestUtil {

    public static LightDiContext startContext() {
        LightDiContextConfiguration configuration = LightDiContextConfiguration.builder()
                .withThreadNumber(4)
                .withCheckForIntegrity(true)
                .withAdditionalDependencies(Collections.singletonList(new JnaLightDiPlugin()))
                .build();
        LightDiContext lightDi = LightDi.initContextByClass(TestContextConfiguration.class, configuration);
        lightDi.eagerInitAllBeans();

        return lightDi;
    }

    public static void assertFrameEquals(ReadOnlyClipImage firstFrame, ReadOnlyClipImage secondFrame, String errorMessage) {
        if (!firstFrame.isSameSizeAs(secondFrame)) {
            failOnImageError(firstFrame, secondFrame, errorMessage);
        }
        for (int y = 0; y < firstFrame.getHeight(); ++y) {
            for (int x = 0; x < secondFrame.getWidth(); ++x) {
                for (int i = 0; i < 4; ++i) {
                    int color1 = firstFrame.getColorComponentWithOffset(x, y, i);
                    int color2 = secondFrame.getColorComponentWithOffset(x, y, i);
                    if (color1 != color2) {
                        System.out.println(x + " " + y + " " + i);
                        failOnImageError(firstFrame, secondFrame, errorMessage);
                    }
                }
            }
        }
    }

    private static void failOnImageError(ReadOnlyClipImage firstFrame, ReadOnlyClipImage secondFrame, String errorMessage) {
        DebugImageRenderer.render(firstFrame);
        DebugImageRenderer.render(secondFrame);
        fail(errorMessage + "\nImages saved, see URLs in log");
    }

    public static ClipImage loadTestClasspathImage(String fileName) {
        try {
            byte[] bytes = readClasspathFile(fileName);
            BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(bytes));

            ClipImage result = ClipImage.fromSize(bufferedImage.getWidth(), bufferedImage.getHeight());

            for (int i = 0; i < bufferedImage.getHeight(); ++i) {
                for (int j = 0; j < bufferedImage.getWidth(); ++j) {
                    Color color = new Color(bufferedImage.getRGB(j, i));
                    int red = color.getRed();
                    int green = color.getGreen();
                    int blue = color.getBlue();
                    int alpha = color.getAlpha();

                    result.setRed(red, j, i);
                    result.setGreen(green, j, i);
                    result.setBlue(blue, j, i);
                    result.setAlpha(alpha, j, i);
                }
            }

            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] readClasspathFile(String fileName) throws IOException, URISyntaxException {
        Path uri = Paths.get(IntegrationTestUtil.class.getResource("/" + fileName).toURI());
        return Files.readAllBytes(uri);
    }

    public static Builder getDefaultFrameRequest() {
        return TimelineManagerFramesRequest.builder()
                .withNeedSound(false)
                .withPosition(TimelinePosition.ofZero())
                .withPreviewWidth(600)
                .withPreviewHeight(400)
                .withScale(1.0);
    }

    public static void pixelEquals(ReadOnlyClipImage videoFrame, int x, int y, ColorWithAlpha colorWithAlpha) {
        assertThat(videoFrame.getRed(x, y), is(colorWithAlpha.getRed()));
        assertThat(videoFrame.getGreen(x, y), is(colorWithAlpha.getGreen()));
        assertThat(videoFrame.getBlue(x, y), is(colorWithAlpha.getBlue()));
        assertThat(videoFrame.getAlpha(x, y), is(colorWithAlpha.getAlpha()));
    }

}
