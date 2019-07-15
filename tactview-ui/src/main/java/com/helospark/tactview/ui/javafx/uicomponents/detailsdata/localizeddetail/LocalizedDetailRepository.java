package com.helospark.tactview.ui.javafx.uicomponents.detailsdata.localizeddetail;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.PostConstruct;

import com.fasterxml.jackson.core.type.TypeReference;
import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.proceduralclip.ProceduralVisualClip;
import com.helospark.tactview.core.util.ClasspathJsonParserImpl;

@Component
public class LocalizedDetailRepository {
    private static final TypeReference<List<LocalizedDetailDomain>> TYPE_REFERENCE = new TypeReference<List<LocalizedDetailDomain>>() {
    };
    private Map<String, LocalizedDetailDomain> classToDetail = new HashMap<>();
    private ClasspathJsonParserImpl classpathJsonParser;
    private List<LocalizedDetailFileHolder> fileHolders;

    public LocalizedDetailRepository(ClasspathJsonParserImpl classpathJsonParser, List<LocalizedDetailFileHolder> fileHolders) {
        this.classpathJsonParser = classpathJsonParser;
        this.fileHolders = fileHolders;
    }

    @PostConstruct
    public void parse() {
        fileHolders.stream()
                .flatMap(holder -> holder.getFiles().stream())
                .forEach(fileName -> readFromFile(fileName));
    }

    public void readFromFile(String filename) {
        List<LocalizedDetailDomain> readFile = classpathJsonParser.readClasspathFile(filename, TYPE_REFERENCE);
        readFile.stream()
                .forEach(file -> {
                    classToDetail.put(file.getType(), file);
                });
    }

    public Optional<String> queryDetail(String id) {
        return Optional.ofNullable(classToDetail.get(id))
                .map(data -> data.getDescription());
    }

    public Optional<String> queryDetailForClip(TimelineClip clip) {
        return queryDataForClip(clip).map(data -> data.getDescription());
    }

    public Optional<LocalizedDetailDomain> queryData(String id) {
        return Optional.ofNullable(classToDetail.get(id));
    }

    public Optional<LocalizedDetailDomain> queryDataForClip(TimelineClip clip) {
        String id = clip.getCreatorFactoryId();
        if (clip instanceof ProceduralVisualClip) {
            id += ":" + ((ProceduralVisualClip) clip).getProceduralFactoryId();
        }
        return Optional.ofNullable(classToDetail.get(id));
    }
}
