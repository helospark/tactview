package com.helospark.tactview.core.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class FileTypeProberUtil {

    public static boolean isImageByContentType(File file) {
        try {
            return Files.probeContentType(file.toPath()).contains("image/");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
