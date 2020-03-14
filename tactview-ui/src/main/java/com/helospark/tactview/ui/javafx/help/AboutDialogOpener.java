package com.helospark.tactview.ui.javafx.help;

import com.helospark.lightdi.annotation.Component;
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

    public AboutDialogOpener(LocalizedResourceLoader localizedResourceLoader, ClassPathResourceReader classpathResourceReader,
            SimpleMarkdownParser simpleMarkdownParser, StylesheetAdderService stylesheetAdderService) {
        this.localizedResourceLoader = localizedResourceLoader;
        this.classpathResourceReader = classpathResourceReader;
        this.simpleMarkdownParser = simpleMarkdownParser;
        this.stylesheetAdderService = stylesheetAdderService;
    }

    public void openDialog() {
        String resource = loadAboutHtml();
        String contributors = loadContributorsFile();
        VBox aboutDialogText = simpleMarkdownParser.parseMarkdown(resource.replace("{contributors}", contributors));
        AboutDialog aboutDialog = new AboutDialog(aboutDialogText, stylesheetAdderService);
        aboutDialog.show();
    }

    private String loadContributorsFile() {
        return classpathResourceReader.readClasspathFile("about/contributors");
    }

    private String loadAboutHtml() {
        return localizedResourceLoader.loadResource("about/about.html");
    }
}
