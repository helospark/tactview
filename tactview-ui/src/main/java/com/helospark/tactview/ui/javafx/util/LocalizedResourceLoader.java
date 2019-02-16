package com.helospark.tactview.ui.javafx.util;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.util.ClassPathResourceReader;

@Component
public class LocalizedResourceLoader {
    private ClassPathResourceReader classPathResourceReader;

    public LocalizedResourceLoader(ClassPathResourceReader classPathResourceReader) {
        this.classPathResourceReader = classPathResourceReader;
    }

    public String loadResource(String name) {
        // TODO: try localized postfix
        return classPathResourceReader.readClasspathFile(name);
    }

}
