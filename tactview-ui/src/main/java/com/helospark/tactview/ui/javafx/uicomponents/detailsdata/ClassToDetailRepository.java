package com.helospark.tactview.ui.javafx.uicomponents.detailsdata;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.PostConstruct;

import com.fasterxml.jackson.core.type.TypeReference;
import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.util.ClasspathJsonParser;

@Component
public class ClassToDetailRepository {
    private static final TypeReference<List<ClassToDetailDomain>> TYPE_REFERENCE = new TypeReference<List<ClassToDetailDomain>>() {
    };
    private Map<String, String> classToDetail = new HashMap<>();
    private ClasspathJsonParser classpathJsonParser;

    public ClassToDetailRepository(ClasspathJsonParser classpathJsonParser) {
        this.classpathJsonParser = classpathJsonParser;
    }

    @PostConstruct
    public void parse() {
        List<ClassToDetailDomain> readFile = classpathJsonParser.readClasspathFile("localization/clip-description-en_us.json", TYPE_REFERENCE);
        readFile.stream()
                .forEach(file -> {
                    assertClassExists(file);
                    classToDetail.put(file.getType(), file.getDescription());
                });
    }

    private void assertClassExists(ClassToDetailDomain a) {
        try {
            Class.forName(a.getType());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Illegal configuration", e);
        }
    }

    public Optional<String> queryDetail(Class<?> clazz) {
        return Optional.ofNullable(classToDetail.get(clazz.getName()));
    }
}
