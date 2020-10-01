package com.helospark.tactview.core.save;

import java.io.File;

public class LoadMetadata {
    private String fileLocation;

    public LoadMetadata(String fileLocation) {
        this.fileLocation = fileLocation;
    }

    public String getFileLocation() {
        return fileLocation;
    }

    // TODO: It's not really ideal in here, but avoids lots of copyPaste
    public File resolveFilePath(String fileName) {
        File file;
        if (fileName.startsWith(SaveMetadata.LOCALLY_SAVED_SOURCE_PREFIX)) {
            file = new File(getFileLocation(), fileName.substring(SaveMetadata.LOCALLY_SAVED_SOURCE_PREFIX.length()));
        } else {
            file = new File(fileName);
        }
        return file;
    }
}
