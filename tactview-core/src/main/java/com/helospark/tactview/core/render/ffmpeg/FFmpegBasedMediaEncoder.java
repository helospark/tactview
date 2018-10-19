package com.helospark.tactview.core.render.ffmpeg;

import com.helospark.tactview.core.util.jpaplugin.NativeImplementation;
import com.sun.jna.Library;

@NativeImplementation("ffmpegmediaencoder")
public interface FFmpegBasedMediaEncoder extends Library {

    public int initEncoder(FFmpegInitEncoderRequest request);

    public void encodeFrames(FFmpegEncodeFrameRequest request);

    public void clearEncoder(FFmpegClearEncoderRequest request);

}
