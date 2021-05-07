package com.helospark.tactview.core.util;

import java.io.File;
import java.nio.file.Files;

public class FileTypeProberUtil {

    public static boolean isImageByContentType(File file) {
        try {
            String contentType = Files.probeContentType(file.toPath());
            if (contentType == null) {
                return false;
            } else {
                return contentType.contains("image/");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isAudioByContentType(File file) {
        try {
            String contentType = Files.probeContentType(file.toPath());

            if (contentType == null) {
                return false;
            } else {
                if (contentType.equals("audio/ogg")) {
                    return false; // we cannot decide, since video OGG is also returning this content type
                } else {
                    return contentType.contains("audio/");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}