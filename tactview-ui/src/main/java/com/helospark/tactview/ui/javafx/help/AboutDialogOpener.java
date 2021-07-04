package com.helospark.tactview.ui.javafx.help;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.annotation.Qualifier;
import com.helospark.tactview.core.util.ClassPathResourceReader;
import com.helospark.tactview.ui.javafx.stylesheet.StylesheetAdderService;
import com.helospark.tactview.ui.javafx.util.LocalizedResourceLoader;

import javafx.scene.layout.VBox;

@Component
public class AboutDialogOpener {
    private LocalizedResourceLoader localizedResourceLoader;
    private ClassPathResourceReader classpathResourceReader;
    private SimpleMarkdownParser simpleMarkdownParser;
    private StylesheetAdderService stylesheetAdderService;
    private ObjectMapper objectMapper;

    public AboutDialogOpener(LocalizedResourceLoader localizedResourceLoader, ClassPathResourceReader classpathResourceReader,
            SimpleMarkdownParser simpleMarkdownParser, StylesheetAdderService stylesheetAdderService, @Qualifier("simpleObjectMapper") ObjectMapper objectMapper) {
        this.localizedResourceLoader = localizedResourceLoader;
        this.classpathResourceReader = classpathResourceReader;
        this.simpleMarkdownParser = simpleMarkdownParser;
        this.stylesheetAdderService = stylesheetAdderService;
        this.objectMapper = objectMapper;
    }

    public void openDialog() {
        String resource = loadAboutHtml();
        String contributors = loadContributorsFile();
        String templated = templateResource(resource, contributors);
        VBox aboutDialogText = simpleMarkdownParser.parseMarkdown(templated);
        AboutDialog aboutDialog = new AboutDialog(aboutDialogText, stylesheetAdderService);
        aboutDialog.show();
    }

    private String templateResource(String resource, String contributors) {
        String templated = resource.replace("{contributors}", contributors);
        String gitHash = "Unknown";
        try {
            JsonNode tree = objectMapper.readTree(classpathResourceReader.readClasspathFile("git.properties"));
            gitHash = tree.get("git.commit.id").asText();
        } catch (Exception e) {
            e.printStackTrace();
        }
        templated = templated.replace("{git_commit}", gitHash);
        return templated;
    }

    private String loadContributorsFile() {
        return classpathResourceReader.readClasspathFile("about/contributors");
    }

    private String loadAboutHtml() {
        return localizedResourceLoader.loadResource("about/about.html");
    }
}
