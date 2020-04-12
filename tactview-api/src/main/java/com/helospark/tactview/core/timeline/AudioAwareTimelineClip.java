package com.helospark.tactview.core.timeline;

// TODO: eventually all public methods should be extracted here.
// for now only the ones related to rendering are extracted
public interface AudioAwareTimelineClip extends ITimelineClip {

    public AudioFrameResult requestAudioFrame(AudioRequest audioRequest);

}
