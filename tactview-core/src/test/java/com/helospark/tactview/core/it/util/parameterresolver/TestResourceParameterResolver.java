package com.helospark.tactview.core.it.util.parameterresolver;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
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

            saveFile(new URL(url), testFile);

            return testFile;
        } catch (Exception e) {
            throw new ParameterResolutionException("Unable to resolve parameter", e);
        }
    }

    // https://stackoverflow.com/a/36104239/8258222
    public boolean saveFile(URL imgURL, File imgSavePath) {
        boolean isSucceed = true;

        CloseableHttpClient httpClient = HttpClients.createDefault();

        HttpGet httpGet = new HttpGet(imgURL.toString());
        httpGet.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.11 Safari/537.36");

        try {
            CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity imageEntity = httpResponse.getEntity();

            if (imageEntity != null) {
                FileUtils.copyInputStreamToFile(imageEntity.getContent(), imgSavePath);
            }

        } catch (IOException e) {
        	throw new RuntimeException(e);
        }

        httpGet.releaseConnection();

        return isSucceed;
    }
    
}
