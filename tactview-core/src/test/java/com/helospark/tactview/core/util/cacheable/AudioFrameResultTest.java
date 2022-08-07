package com.helospark.tactview.core.util.cacheable;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.nio.ByteBuffer;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.helospark.tactview.core.timeline.AudioFrameResult;

public class AudioFrameResultTest {

    @Test
    public void test() {
        java.nio.ByteBuffer buffer = ByteBuffer.wrap(new byte[]{1, 2, 3, 4});
        AudioFrameResult result = new AudioFrameResult(List.of(buffer), 48000, 4);

        int value1 = result.getSampleAt(0, 0);

        result.setSampleAt(0, 0, value1);

        int value2 = result.getSampleAt(0, 0);

        assertThat(value1, is(value2));
    }

    @Test
    public void test2() {
        java.nio.ByteBuffer buffer = ByteBuffer.wrap(new byte[]{1, -2, -3, 4});
        AudioFrameResult result = new AudioFrameResult(List.of(buffer), 48000, 4);

        int value1 = result.getSampleAt(0, 0);

        result.setSampleAt(0, 0, value1);

        int value2 = result.getSampleAt(0, 0);

        assertThat(value1, is(value2));
    }

    @Test
    public void test3() {
        java.nio.ByteBuffer buffer = ByteBuffer.wrap(new byte[]{-53, 49, -75, -76});
        AudioFrameResult result = new AudioFrameResult(List.of(buffer), 48000, 4);

        int value1 = result.getSampleAt(0, 0);

        result.setSampleAt(0, 0, value1);

        int value2 = result.getSampleAt(0, 0);

        assertThat(value1, is(value2));
        assertThat(buffer.get(0), is((byte) -53));
        assertThat(buffer.get(1), is((byte) 49));
        assertThat(buffer.get(2), is((byte) -75));
        assertThat(buffer.get(3), is((byte) -76));
    }

    @Test
    public void test4() {
        java.nio.ByteBuffer buffer = ByteBuffer.wrap(new byte[]{-22, -61});
        AudioFrameResult result = new AudioFrameResult(List.of(buffer), 48000, 2);

        int value1 = result.getSampleAt(0, 0);

        assertThat(value1, is(-5437));
    }

    @Test
    public void test5() {
        java.nio.ByteBuffer buffer = ByteBuffer.wrap(new byte[]{0, 0});
        AudioFrameResult result = new AudioFrameResult(List.of(buffer), 48000, 2);

        result.setSampleAt(0, 0, -5437);
        int value1 = result.getSampleAt(0, 0);

        assertThat(value1, is(-5437));
    }

}
