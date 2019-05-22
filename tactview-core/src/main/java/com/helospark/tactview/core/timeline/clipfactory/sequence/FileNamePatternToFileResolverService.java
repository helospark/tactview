package com.helospark.tactview.core.timeline.clipfactory.sequence;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.helospark.lightdi.annotation.Service;

@Service
public class FileNamePatternToFileResolverService {

    //    @Cacheable
    public List<FileHolder> filenamePatternToFileResolver(String filePathAndPattern) {
        int index = filePathAndPattern.lastIndexOf(File.separatorChar);

        String path = filePathAndPattern.substring(0, index);
        String fileNamePattern = filePathAndPattern.substring(index + 1);

        Pattern pattern = Pattern.compile(fileNamePattern);

        File folder = new File(path);

        TreeSet<FileHolder> files = new TreeSet<>();

        for (File file : folder.listFiles()) {
            String name = file.getName();
            Matcher matcher = pattern.matcher(name);
            if (matcher.matches()) {
                String frameIndex = matcher.group(1);
                int intFrameIndex = Integer.valueOf(frameIndex);
                files.add(new FileHolder(file, intFrameIndex));
            }
        }

        return new ArrayList<>(files);
    }

}
