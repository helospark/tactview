package com.helospark.tactview.core.render.proxy.compression;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.annotation.Qualifier;
import com.helospark.tactview.core.decoder.framecache.GlobalMemoryManagerAccessor;
import com.helospark.tactview.core.decoder.imagesequence.ImageSequenceDecoderDecorator;
import com.helospark.tactview.core.render.proxy.ffmpeg.FFmpegFrameWithFrameNumber;

import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;

@Component
public class Lz4CompressedImageWriter implements CompressedImageWriter {
    private static final String FILENAME = "image_";
    private static final String EXTENSION = ".lz4";
    private LZ4Compressor compressor;

    private ImageSequenceDecoderDecorator imageSequenceDecoderDecorator;

    public Lz4CompressedImageWriter(@Qualifier("lz4ImageSequenceDecoderDecorator") ImageSequenceDecoderDecorator imageSequenceDecoderDecorator) {
        LZ4Factory factory = LZ4Factory.fastestInstance();
        compressor = factory.fastCompressor();
        this.imageSequenceDecoderDecorator = imageSequenceDecoderDecorator;
    }

    @Override
    public void writeCompressedFrame(FFmpegFrameWithFrameNumber frame, File proxyFolder, int width, int height) {
        File file = new File(proxyFolder, FILENAME + frame.frame + EXTENSION);

        ByteBuffer dest = GlobalMemoryManagerAccessor.memoryManager.requestBuffer(frame.data.limit());
        compressor.compress(frame.data, dest);

        try (var fos = new FileOutputStream(file, false)) {
            FileChannel channel = fos.getChannel();
            dest.flip();
            channel.write(dest);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            GlobalMemoryManagerAccessor.memoryManager.returnBuffer(dest);
        }
    }

    @Override
    public String getImageNamePattern() {
        return FILENAME + "(\\d+)" + EXTENSION;
    }

    @Override
    public ImageSequenceDecoderDecorator getImageSequenceDecoderDecorator() {
        return imageSequenceDecoderDecorator;
    }

}
