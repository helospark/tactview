package com.helospark.tactview.core.util;

import com.fasterxml.jackson.core.type.TypeReference;

public interface ClasspathJsonParser {

    <T> T readClasspathFile(String fileName, TypeReference<T> reference);

}