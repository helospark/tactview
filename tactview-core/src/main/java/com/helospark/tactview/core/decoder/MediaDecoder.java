package com.helospark.tactview.core.decoder;

import java.io.File;

public interface MediaDecoder {

    public MediaMetadata readMetadata(File file);

}
