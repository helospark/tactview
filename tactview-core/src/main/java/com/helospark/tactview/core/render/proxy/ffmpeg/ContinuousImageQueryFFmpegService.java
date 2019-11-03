package com.helospark.tactview.core.render.proxy.ffmpeg;

import com.helospark.tactview.core.util.jpaplugin.NativeImplementation;
import com.sun.jna.Library;

@NativeImplementation("ffmpegconinousimagequeryservice")
public interface ContinuousImageQueryFFmpegService extends Library {

    public int openFile(InitializeReadJobRequest request);

    public int readFrames(QueryFramesRequest request);

    public void freeJob(int jobId);
}
