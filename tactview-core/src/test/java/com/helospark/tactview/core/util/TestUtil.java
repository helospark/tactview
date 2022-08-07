package com.helospark.tactview.core.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.IsCloseTo.closeTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Color;
import com.helospark.tactview.core.util.lut.cube.LutColor;

public class TestUtil {

    public static String readClasspathFile(String fileName) throws IOException, URISyntaxException {
        Path uri = Paths.get(TestUtil.class.getResource("/" + fileName).toURI());
        return new String(Files.readAllBytes(uri), Charset.forName("UTF-8"));
    }

    public static InputStream classpathFileToInputStream(String fileName) throws IOException, URISyntaxException {
        Path uri = Paths.get(TestUtil.class.getResource("/" + fileName).toURI());
        return Files.newInputStream(uri);
    }

    public static void assertTripletEquals(LutColor actual, float[] expected) {
        assertTripletEquals(new float[]{actual.r, actual.g, actual.b}, expected);
    }

    public static void assertTripletEquals(Color result, float[] expected) {
        assertTripletEquals(new float[]{(float) result.red, (float) result.green, (float) result.blue}, expected);
    }

    public static void assertTripletEquals(float[] actual, float[] expected) {
        assertEquals(actual.length, expected.length);
        for (int i = 0; i < actual.length; ++i) {
            assertThat((double) actual[i], closeTo(expected[i], 0.000001));
        }
    }
}
