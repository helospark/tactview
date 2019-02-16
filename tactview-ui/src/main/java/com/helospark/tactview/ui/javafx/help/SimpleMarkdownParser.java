package com.helospark.tactview.ui.javafx.help;

import com.helospark.lightdi.annotation.Component;

import javafx.geometry.Orientation;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;

@Component
public class SimpleMarkdownParser {

    public VBox parseMarkdown(String data) {
        VBox result = new VBox();
        for (String line : data.split("\n")) {
            if (line.equals("")) {
            } else if (line.startsWith("##")) {
                Label text = new Label(line.replaceFirst("##", "").trim());
                text.setWrapText(true);
                text.getStyleClass().addAll("markdown-title-two", "markdown-text");
                result.getChildren().add(text);
            } else if (line.startsWith("#")) {
                Label text = new Label(line.replaceFirst("#", "").trim());
                text.setWrapText(true);
                text.getStyleClass().addAll("markdown-title-one", "markdown-text");
                result.getChildren().add(text);
            } else if (line.startsWith("---")) {
                Separator separator = new Separator();
                separator.setOrientation(Orientation.HORIZONTAL);
                result.getChildren().add(separator);
            } else {
                Label text = new Label(line);
                text.setWrapText(true);
                text.getStyleClass().add("markdown-text");
                result.getChildren().add(text);
            }
        }
        return result;
    }

}
