package com.helospark.tactview.core.util.lightdi;

import java.io.File;

import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.properties.PropertyConverter;

@Component
public class ValueToFileConverter implements PropertyConverter<File> {

    @Override
    public File convert(String property) {
        String canonicalPath = property.replace('\\', File.separatorChar).replace('/', File.separatorChar);

        return new File(canonicalPath);
    }

}
