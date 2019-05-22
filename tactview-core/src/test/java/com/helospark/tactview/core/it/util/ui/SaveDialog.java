package com.helospark.tactview.core.it.util.ui;

import java.io.File;

import com.helospark.tactview.core.save.SaveAndLoadHandler;
import com.helospark.tactview.core.save.SaveRequest;

public class SaveDialog {
    private SaveAndLoadHandler saveAndLoadHandler;

    public SaveDialog(SaveAndLoadHandler saveAndLoadHandler) {
        this.saveAndLoadHandler = saveAndLoadHandler;
    }

    public void selectFile(File tmpFile) {
        SaveRequest saveRequest = new SaveRequest(tmpFile.getAbsolutePath());
        saveAndLoadHandler.save(saveRequest);
    }

}
