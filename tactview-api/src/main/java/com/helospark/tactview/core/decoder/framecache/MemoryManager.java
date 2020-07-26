package com.helospark.tactview.core.decoder.framecache;

import java.nio.ByteBuffer;
import java.util.List;

public interface MemoryManager {

    ByteBuffer requestBuffer(Integer bytes);

    List<ByteBuffer> requestBuffers(Integer bytes, int count);

    void returnBuffer(ByteBuffer buffer);

    void returnBuffers(List<ByteBuffer> buffers);

    public void dropAllBuffers();

}