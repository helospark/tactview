package com.helospark.tactview.core.timeline.proceduralclip.text;

import java.awt.Font;
import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helospark.lightdi.annotation.Component;

@Component
public class FontLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(FontLoader.class);
    private Map<File, Font> fileToFontMap = new ConcurrentHashMap<>();

    public Font loadFont(File file) {
        return fileToFontMap.compute(file, (key, oldValue) -> {
            if (oldValue != null) {
                return oldValue;
            } else {
                return loadFontInternal(file);
            }
        });

    }

    private Font loadFontInternal(File file) {
        try {
            Font result = Font.createFont(Font.TRUETYPE_FONT, file);
            return result;
        } catch (Exception e) {
            LOGGER.warn("Unable to load font for file %s", file.getAbsolutePath(), e);
            return null;
        }
    }
}
