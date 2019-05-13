package com.helospark.tactview.core.util;

import java.io.File;
import java.nio.file.Files;

public class FileTypeProberUtil {

    public static boolean isImageByContentType(File file) {
        try {
            return Files.probeContentType(file.toPath()).contains("image/");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
