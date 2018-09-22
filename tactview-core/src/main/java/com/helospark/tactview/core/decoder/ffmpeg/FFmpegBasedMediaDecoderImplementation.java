package com.helospark.tactview.core.decoder.ffmpeg;

import com.helospark.tactview.core.util.jpaplugin.NativeImplementation;
import com.sun.jna.Library;

@NativeImplementation(value = "ffmpegmediadecoder")
public interface FFmpegBasedMediaDecoderImplementation extends Library {

    public FFmpegResult readMediaMetadata(String filePath);

    public void readFrames(FFmpegImageRequest ffmpegRequest);

}
