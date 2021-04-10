package com.helospark.tactview.core.render.helper;

import com.helospark.tactview.core.timeline.effect.interpolation.provider.ValueListElement;

public class HandledExtensionValueElement extends ValueListElement {
    public ExtensionType extensionType;
    public String extension;

    public HandledExtensionValueElement(String id, String text, ExtensionType extensionType) {
        super(id, text);
        this.extensionType = extensionType;
        this.extension = id;
    }

    public HandledExtensionValueElement(String id, String extension, String text, ExtensionType extensionType) {
        super(id, text);
        this.extensionType = extensionType;
        this.extension = extension;
    }

    public ExtensionType getExtensionType() {
        return extensionType;
    }

    public String getExtension() {
        return extension;
    }

}
