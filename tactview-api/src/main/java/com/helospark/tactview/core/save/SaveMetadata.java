package com.helospark.tactview.core.save;

import java.util.HashMap;
import java.util.Map;

public class SaveMetadata {
    public static final String LOCALLY_SAVED_SOURCE_PREFIX = "savefile:";

    private boolean packageAllContent = true;
    private Map<String, String> filesToCopy = new HashMap<>();
    private Map<String, byte[]> dataToCopy = new HashMap<>();

    public SaveMetadata(boolean packageAllContent) {
        this.packageAllContent = packageAllContent;
    }

    public boolean isPackageAllContent() {
        return packageAllContent;
    }

    public Map<String, String> getFilesToCopy() {
        return filesToCopy;
    }

    public Map<String, byte[]> getDataToCopy() {
        return dataToCopy;
    }

}
