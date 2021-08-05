package com.helospark.tactview.core.timeline.subtimeline.loadhelper;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.zeroturnaround.zip.ZipUtil;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.helospark.lightdi.LightDiContext;
import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.save.TemplateSaveAndLoadHandler;
import com.helospark.tactview.core.timeline.AddClipRequest;
import com.helospark.tactview.core.util.StaticObjectMapper;

@Component
public class SubtimelineLoadFileService {
    private LightDiContext context;

    public SubtimelineLoadFileService(LightDiContext context) {
        this.context = context;
    }

    public LoadData getLoadData(AddClipRequest request, String nodeName) {
        try {
            ObjectMapper mapper = StaticObjectMapper.objectMapper;

            File tmpDir = new File(System.getProperty("java.io.tmpdir"));
            File rootDirectory = new File(tmpDir, "tactview_save_" + System.currentTimeMillis());
            ZipUtil.unpack(new File(request.getFile().getAbsolutePath()), rootDirectory);

            File fileName = new File(rootDirectory.getAbsolutePath(), TemplateSaveAndLoadHandler.TEMPLATE_FILE_NAME);

            String content = new String(Files.readAllBytes(Paths.get(fileName.getAbsolutePath())), StandardCharsets.UTF_8);

            JsonNode tree = mapper.readTree(content);

            LoadMetadata loadMetadata = new LoadMetadata(rootDirectory.getAbsolutePath(), mapper, context);

            LoadData loadData = new LoadData(tree.get(nodeName), loadMetadata);
            return loadData;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
