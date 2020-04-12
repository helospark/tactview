package com.helospark.tactview.core.timeline;

import java.util.List;

//TODO: eventually all public methods should be extracted here.
//for now only the ones related to rendering are extracted
public interface ITimelineClip {

    String getId();

    List<String> getClipDependency(TimelinePosition position);

    List<String> getChannelDependency(TimelinePosition position);

}