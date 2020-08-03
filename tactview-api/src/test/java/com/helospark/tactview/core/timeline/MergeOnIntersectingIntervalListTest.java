package com.helospark.tactview.core.timeline;

import static com.helospark.tactview.core.timeline.TimelinePosition.ofSeconds;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class MergeOnIntersectingIntervalListTest {

    @Test
    public void testList() {
        // GIVEN
        MergeOnIntersectingIntervalList mergingList = new MergeOnIntersectingIntervalList();

        // WHEN
        mergingList.addInterval(new TimelineInterval(ofSeconds(1), ofSeconds(3)));
        mergingList.addInterval(new TimelineInterval(ofSeconds(3), ofSeconds(5)));
        mergingList.addInterval(new TimelineInterval(ofSeconds(7), ofSeconds(9)));
        mergingList.addInterval(new TimelineInterval(ofSeconds(8), ofSeconds(9)));
        mergingList.addInterval(new TimelineInterval(ofSeconds(10), ofSeconds(11)));

        // THEN
        assertEquals(3, mergingList.size());
        assertEquals(new TimelineInterval(ofSeconds(1), ofSeconds(5)), mergingList.get(0));
        assertEquals(new TimelineInterval(ofSeconds(7), ofSeconds(9)), mergingList.get(1));
        assertEquals(new TimelineInterval(ofSeconds(10), ofSeconds(11)), mergingList.get(2));
    }

    @Test
    public void testListOutOfOrder() {
        // GIVEN
        MergeOnIntersectingIntervalList mergingList = new MergeOnIntersectingIntervalList();

        // WHEN
        mergingList.addInterval(new TimelineInterval(ofSeconds(8), ofSeconds(9)));
        mergingList.addInterval(new TimelineInterval(ofSeconds(1), ofSeconds(3)));
        mergingList.addInterval(new TimelineInterval(ofSeconds(7), ofSeconds(9)));
        mergingList.addInterval(new TimelineInterval(ofSeconds(10), ofSeconds(11)));
        mergingList.addInterval(new TimelineInterval(ofSeconds(3), ofSeconds(5)));

        // THEN
        assertEquals(3, mergingList.size());
        assertEquals(new TimelineInterval(ofSeconds(1), ofSeconds(5)), mergingList.get(0));
        assertEquals(new TimelineInterval(ofSeconds(7), ofSeconds(9)), mergingList.get(1));
        assertEquals(new TimelineInterval(ofSeconds(10), ofSeconds(11)), mergingList.get(2));
    }

    @Test
    public void testListOverrideAll() {
        // GIVEN
        MergeOnIntersectingIntervalList mergingList = new MergeOnIntersectingIntervalList();

        // WHEN
        mergingList.addInterval(new TimelineInterval(ofSeconds(1), ofSeconds(3)));
        mergingList.addInterval(new TimelineInterval(ofSeconds(3), ofSeconds(5)));
        mergingList.addInterval(new TimelineInterval(ofSeconds(7), ofSeconds(9)));
        mergingList.addInterval(new TimelineInterval(ofSeconds(8), ofSeconds(9)));
        mergingList.addInterval(new TimelineInterval(ofSeconds(10), ofSeconds(11)));
        mergingList.addInterval(new TimelineInterval(ofSeconds(0), ofSeconds(20)));

        // THEN
        assertEquals(1, mergingList.size());
        assertEquals(new TimelineInterval(ofSeconds(0), ofSeconds(20)), mergingList.get(0));
    }

}
