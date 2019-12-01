package com.helospark.tactview.core.render.proxy.compression;

import java.io.File;

import com.helospark.tactview.core.decoder.imagesequence.ImageSequenceDecoderDecorator;
import com.helospark.tactview.core.render.proxy.ffmpeg.FFmpegFrameWithFrameNumber;

public interface CompressedImageWriter {

    void writeCompressedFrame(FFmpegFrameWithFrameNumber frame, File proxyFolder, int width, int height);

    String getImageNamePattern();

    ImageSequenceDecoderDecorator getImageSequenceDecoderDecorator();

}