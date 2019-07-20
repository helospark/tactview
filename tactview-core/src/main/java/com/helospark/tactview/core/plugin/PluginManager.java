package com.helospark.tactview.core.plugin;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.zeroturnaround.zip.ZipUtil;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.annotation.Qualifier;
import com.helospark.lightdi.annotation.Value;

@Component
public class PluginManager {
    private static final String PLUGIN_DESCRIPTOR_JSON = "plugin-descriptor.json";
    private String pluginDirectoryPath;
    private ObjectMapper objectMapper;

    public PluginManager(@Value("${tactview.plugindirectory}") String pluginDirectoryPath, @Qualifier("simpleObjectMapper") ObjectMapper objectMapper) {
        this.pluginDirectoryPath = pluginDirectoryPath;
        this.objectMapper = objectMapper;
    }

    public void installPlugin(File file) {
        try {
            PluginDescriptor pluginDescriptor = objectMapper.readValue(ZipUtil.unpackEntry(file, PLUGIN_DESCRIPTOR_JSON), PluginDescriptor.class);

            validatePluginDescriptor(pluginDescriptor);

            File pluginDirectory = new File(pluginDirectoryPath);
            if (!pluginDirectory.exists()) {
                pluginDirectory.mkdirs();
            }

            File thisPluginDirectory = new File(pluginDirectory, pluginDescriptor.getId());

            if (!thisPluginDirectory.exists()) {
                thisPluginDirectory.mkdirs();
            } else {
                FileUtils.deleteDirectory(thisPluginDirectory);
                thisPluginDirectory.mkdirs();
            }
            ZipUtil.unpack(file, thisPluginDirectory);

            boolean valid = isInstallationValid(thisPluginDirectory);

            if (!valid) {
                FileUtils.deleteDirectory(thisPluginDirectory);
                thisPluginDirectory.mkdirs();
                throw new RuntimeException("Invalid plugin");
            }

        } catch (Exception e) {
            throw new RuntimeException("Cannot install plugin", e);
        }
    }

    private void validatePluginDescriptor(PluginDescriptor pluginDescriptor) {
        if (pluginDescriptor.getId() == null) {
            throw new RuntimeException("Plugin ID cannot be null");
        }
        if (pluginDescriptor.getId().matches("[a-zA-Z0-9\\-]")) {
            throw new RuntimeException("Plugin ID contains invalid characters");
        }
        if (pluginDescriptor.getName() == null) {
            throw new RuntimeException("Plugin name cannot be null");
        }
        if (pluginDescriptor.getDescription() == null) {
            throw new RuntimeException("Plugin description cannot be null");
        }
        if (pluginDescriptor.getVersion() == null) {
            throw new RuntimeException("Plugin version cannot be null");
        }
    }

    private boolean isInstallationValid(File thisPluginDirectory) {
        boolean hasJarFile = false;
        for (File file : thisPluginDirectory.listFiles()) {
            if (file.getName().endsWith(".jar")) {
                if (hasJarFile) {
                    return false;
                } else {
                    hasJarFile = true;
                }
            }
        }
        return true;
    }

    public List<PluginDescriptor> getInstalledPlugins() {
        File pluginDirectory = new File(pluginDirectoryPath);

        if (!pluginDirectory.exists()) {
            return List.of();
        }

        return Arrays.stream(pluginDirectory.listFiles())
                .filter(a -> a.isDirectory())
                .map(a -> readMetadata(a))
                .collect(Collectors.toList());
    }

    public void deletePlugin(String id) {
        try {
            File pluginDirectory = new File(pluginDirectoryPath, id);
            FileUtils.deleteDirectory(pluginDirectory);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private PluginDescriptor readMetadata(File a) {
        try {
            return objectMapper.readValue(new File(a, PLUGIN_DESCRIPTOR_JSON), PluginDescriptor.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
