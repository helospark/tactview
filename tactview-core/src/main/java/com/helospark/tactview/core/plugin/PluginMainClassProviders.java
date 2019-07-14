package com.helospark.tactview.core.plugin;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PluginMainClassProviders {
    private static final Logger LOGGER = LoggerFactory.getLogger(PluginMainClassProviders.class);

    public static List<Class<?>> getPluginClasses() {
        List<Class<?>> result = new ArrayList<>();
        try {
            Enumeration<URL> resources = PluginMainClassProviders.class.getClassLoader()
                    .getResources("META-INF/tactview.properties");
            while (resources.hasMoreElements()) {
                try {
                    URL resource = resources.nextElement();
                    String manifest = IOUtils.toString(resource.openStream(), "UTF-8");

                    Pattern pattern = Pattern.compile("mainClass=(.*)");
                    Matcher matcher = pattern.matcher(manifest);

                    if (matcher.matches()) {
                        String mainClassPath = matcher.group(1);

                        Class<?> mainClass = Class.forName(mainClassPath);

                        result.add(mainClass);
                    } else {
                        LOGGER.error("Unable to load plugin file {} with content {}", resource, manifest);
                    }

                } catch (IOException E) {
                    LOGGER.error("Unable to load plugin {}", resources);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Unable to load plugins");
        }
        return result;
    }

}
