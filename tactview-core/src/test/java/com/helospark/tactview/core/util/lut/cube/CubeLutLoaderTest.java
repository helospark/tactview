package com.helospark.tactview.core.util.lut.cube;

import static com.helospark.tactview.core.util.TestUtil.assertTripletEquals;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Color;
import com.helospark.tactview.core.util.TestUtil;

public class CubeLutLoaderTest {
    private CubeLutLoader underTest;

    @BeforeEach
    protected void setUp() throws Exception {
        underTest = new CubeLutLoader();
    }

    @Test
    public void testReadSmallOneDimensionCubeFile() throws IOException, URISyntaxException {
        // GIVEN
        InputStream file = TestUtil.classpathFileToInputStream("lut/simple-1d-lut.cube");

        // WHEN
        AbstractCubeLut result = underTest.readLut(file);

        // THEN
        assertTrue(result instanceof CubeLut1d);
        assertThat(result.size, is(3));
        assertThat(result.title, is("Demo"));
        assertTripletEquals(result.lowerBound, new float[]{0.0f, 0.0f, 0.0f});
        assertTripletEquals(result.upperBound, new float[]{1.0f, 2.0f, 3.0f});
        assertThat(((CubeLut1d) result).values.length, is(3));

        assertTripletEquals(((CubeLut1d) result).values[0], new float[]{0.0f, 0.0f, 0.0f});
        assertTripletEquals(((CubeLut1d) result).values[1], new float[]{0.5f, 1.0f, 1.5f});
        assertTripletEquals(((CubeLut1d) result).values[2], new float[]{1.0f, 1.0f, 1.0f});
    }

    @Test
    public void testReadLargeOneDimensionCubeFileWithDefault() throws IOException, URISyntaxException {
        // GIVEN
        InputStream file = TestUtil.classpathFileToInputStream("lut/large-1d-lut.cube");

        // WHEN
        AbstractCubeLut result = underTest.readLut(file);

        // THEN
        assertTrue(result instanceof CubeLut1d);
        assertThat(result.size, is(32));
        assertTripletEquals(result.lowerBound, new float[]{0.0f, 0.0f, 0.0f});
        assertTripletEquals(result.upperBound, new float[]{1.0f, 1.0f, 1.0f});
        assertThat(((CubeLut1d) result).values.length, is(32));

        assertTripletEquals(((CubeLut1d) result).values[0], new float[]{0.0004883f, 0.0004883f, 0.0004883f});
        assertTripletEquals(((CubeLut1d) result).values[31], new float[]{704.3f, 704.3f, 704.3f});
    }

    @ParameterizedTest
    @MethodSource("inputDataFor1dLogLut")
    public void test1dLutValue(float[] input, float[] output) throws IOException, URISyntaxException {
        // GIVEN
        InputStream file = TestUtil.classpathFileToInputStream("lut/large-1d-lut.cube");
        AbstractCubeLut lut = underTest.readLut(file);

        // WHEN
        Color color = Color.of(input[0], input[1], input[2]);
        Color result = lut.apply(color);

        // THEN
        assertTripletEquals(result, output);
    }

    @ParameterizedTest
    private static Stream<Arguments> inputDataFor1dLogLut() {
        return Stream.of(
                Arguments.of(new float[]{0.0f, 0.0f, 0.0f}, new float[]{0.0004883f, 0.0004883f, 0.0004883f}),
                Arguments.of(new float[]{0.51612903f, 0.51612903f, 0.51612903f}, new float[]{0.7371f, 0.7371f, 0.7371f}),
                Arguments.of(new float[]{1.0f, 1.0f, 1.0f}, new float[]{704.3f, 704.3f, 704.3f}),
                Arguments.of(new float[]{0.016129032f, 0.016129032f, 0.016129032f}, new float[]{0.00062985f, 0.00062985f, 0.00062985f}));
    }

    @Test
    public void testReadThreeDimensionalCube() throws IOException, URISyntaxException {
        // GIVEN
        InputStream file = TestUtil.classpathFileToInputStream("lut/simple-3d-lut.cube");

        // WHEN
        AbstractCubeLut result = underTest.readLut(file);

        // THEN
        assertTrue(result instanceof CubeLut3d);
        assertThat(result.size, is(2));
        assertTripletEquals(result.lowerBound, new float[]{0.0f, 0.0f, 0.0f});
        assertTripletEquals(result.upperBound, new float[]{1.0f, 1.0f, 1.0f});

        assertTripletEquals(((CubeLut3d) result).values[0][0][0], new float[]{0.0f, 0.0f, 0.0f});
        assertTripletEquals(((CubeLut3d) result).values[0][0][1], new float[]{1.0f, 0.0f, 0.0f});
        assertTripletEquals(((CubeLut3d) result).values[1][1][1], new float[]{1.0f, 1.0f, 1.0f});
    }

    @ParameterizedTest
    @MethodSource("inputDataFor3dLut")
    public void test3dLutValues(float[] input, float[] output) throws IOException, URISyntaxException {
        // GIVEN
        InputStream file = TestUtil.classpathFileToInputStream("lut/simple-3d-lut.cube");
        AbstractCubeLut lut = underTest.readLut(file);

        // WHEN
        Color color = Color.of(input[0], input[1], input[2]);
        Color result = lut.apply(color);

        // THEN
        assertTripletEquals(result, output);
    }

    @ParameterizedTest
    private static Stream<Arguments> inputDataFor3dLut() {
        return Stream.of(
                Arguments.of(new float[]{0.0f, 0.0f, 0.0f}, new float[]{0.0f, 0.0f, 0.0f}),
                Arguments.of(new float[]{1.0f, 1.0f, 1.0f}, new float[]{1.0f, 1.0f, 1.0f}),
                Arguments.of(new float[]{0.0f, 0.0f, 0.5f}, new float[]{0.0f, 0.125f, 0.5f}));
    }

}
