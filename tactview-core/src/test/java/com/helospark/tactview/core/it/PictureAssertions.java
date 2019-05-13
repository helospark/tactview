package com.helospark.tactview.core.it;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.helospark.tactview.core.timeline.AudioVideoFragment;

public class PictureAssertions {

    public static void assertFrameOfColor(AudioVideoFragment frame, int red, int green, int blue, int alpha) {
        assertThat(frame.getVideoResult().getWidth(), is(600));
        assertThat(frame.getVideoResult().getHeight(), is(400));

        for (int i = 0; i < frame.getVideoResult().getHeight(); ++i) {
            for (int j = 0; j < frame.getVideoResult().getWidth(); ++j) {
                assertThat(frame.getVideoResult().getRed(j, i), is(red));
                assertThat(frame.getVideoResult().getGreen(j, i), is(green));
                assertThat(frame.getVideoResult().getBlue(j, i), is(blue));
                assertThat(frame.getVideoResult().getAlpha(j, i), is(alpha));
            }
        }
    }

    public static class Delta {
        int delta;

        public Delta(int delta) {
            this.delta = delta;
        }

        public static Delta of(int delta) {
            return new Delta(delta);
        }
    }

    public static void assertFrameOfColorWithDelta(AudioVideoFragment frame, int red, int green, int blue, int alpha, Delta delta) {
        assertThat(frame.getVideoResult().getWidth(), is(600));
        assertThat(frame.getVideoResult().getHeight(), is(400));

        for (int i = 0; i < frame.getVideoResult().getHeight(); ++i) {
            for (int j = 0; j < frame.getVideoResult().getWidth(); ++j) {
                assertWithDelta("red", frame.getVideoResult().getRed(j, i), red, delta);
                assertWithDelta("green", frame.getVideoResult().getGreen(j, i), green, delta);
                assertWithDelta("blue", frame.getVideoResult().getBlue(j, i), blue, delta);
                assertWithDelta("alpha", frame.getVideoResult().getAlpha(j, i), alpha, delta);
            }
        }
    }

    private static void assertWithDelta(String color, int value, int expected, Delta delta) {
        assertTrue(color + " should be " + expected + " but was " + value, Math.abs(value - expected) < delta.delta);
    }

}
