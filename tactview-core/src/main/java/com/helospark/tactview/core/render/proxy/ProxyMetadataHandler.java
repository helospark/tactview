package com.helospark.tactview.core.render.proxy;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.annotation.Qualifier;
import com.helospark.tactview.core.util.cacheable.Cacheable;

@Component
public class ProxyMetadataHandler {
    private static final String METADATA_JSON_FILE = "tactview-cache-metadata.json";

    private ObjectMapper objectMapper;

    public ProxyMetadataHandler(@Qualifier("simpleObjectMapper") ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void writeMetadata(File proxyImageFolder, int width, int height) throws IOException, JsonGenerationException, JsonMappingException, FileNotFoundException {
        ProxyCacheMetadata proxyCacheMetadata = new ProxyCacheMetadata();
        proxyCacheMetadata.setWidth(width);
        proxyCacheMetadata.setHeight(height);
        objectMapper.writeValue(new FileOutputStream(new File(proxyImageFolder, METADATA_JSON_FILE)), proxyCacheMetadata);
    }

    @Cacheable(cacheTimeInMilliseconds = 100000, size = 100)
    public ProxyCacheMetadata getMetadata(File folder) throws JsonParseException, JsonMappingException, IOException {
        return objectMapper.readValue(new File(folder, METADATA_JSON_FILE), ProxyCacheMetadata.class);
    }

}
