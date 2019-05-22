package com.helospark.tactview.core.it.util.ui;

import java.io.File;

import com.helospark.tactview.core.save.LoadRequest;
import com.helospark.tactview.core.save.SaveAndLoadHandler;

public class LoadDialog {
    private SaveAndLoadHandler saveAndLoadHandler;

    public LoadDialog(SaveAndLoadHandler saveAndLoadHandler) {
        this.saveAndLoadHandler = saveAndLoadHandler;
    }

    public void selectFile(File tmpFile) {
        LoadRequest loadRequest = new LoadRequest(tmpFile.getAbsolutePath());
        saveAndLoadHandler.load(loadRequest);
    }

}
