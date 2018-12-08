package com.helospark.tactview.core.util.lut.lutre3d;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.helospark.tactview.core.util.TestUtil;
import com.helospark.tactview.core.util.lut.cube.AbstractCubeLut;
import com.helospark.tactview.core.util.lut.cube.CubeLut3d;

public class Lutre3dLoaderTest {
    private Lutre3dLoader underTest;

    @BeforeEach
    protected void setUp() throws Exception {
        underTest = new Lutre3dLoader();
    }

    @Test
    public void testLoad() throws IOException, URISyntaxException {
        // GIVEN
        InputStream file = TestUtil.classpathFileToInputStream("lut/simple-flame-3d.3dl");

        // WHEN
        AbstractCubeLut result = underTest.readLut(file);

        // THEN
        assertThat(result, instanceOf(CubeLut3d.class));
        TestUtil.assertTripletEquals(((CubeLut3d) result).getRawCubeValues()[0][0][0], new float[]{0.0f, 0.0f, 0.0f});
        TestUtil.assertTripletEquals(((CubeLut3d) result).getRawCubeValues()[1][0][0], new float[]{0.5f, 0.5f, 0.5f});
        TestUtil.assertTripletEquals(((CubeLut3d) result).getRawCubeValues()[3][3][3], new float[]{1.0f, 1.0f, 1.0f});
    }

}
