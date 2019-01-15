package com.helospark.tactview.core.it.util.parameterresolver;

import java.io.File;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

public class TestResourceParameterResolver implements ParameterResolver {

    @Override
    public boolean supportsParameter(ParameterContext parameterContext,
            ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter().getType() == File.class && parameterContext.isAnnotated(DownloadedResourceName.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext,
            ExtensionContext extensionContext) throws ParameterResolutionException {
        try {
            DownloadedResourceName annotation = parameterContext.findAnnotation(DownloadedResourceName.class).orElseThrow();

            String name = annotation.value();

            File userHome = new File(System.getProperty("user.home"));

            File testFileRootFolder = new File(userHome, ".tactview" + File.separatorChar + "testfiles");
            testFileRootFolder.mkdirs();

            File testFile = new File(testFileRootFolder, name);

            if (testFile.exists()) {
                return testFile;
            }

            String url = NameToResourceMap.NAME_TO_URL.get(name);
            if (url == null) {
                throw new RuntimeException("Resource " + name + " does not exist");
            }

            FileUtils.copyURLToFile(new URL(url), testFile);

            return testFile;
        } catch (Exception e) {
            throw new ParameterResolutionException("Unable to resolve parameter", e);
        }
    }

}
