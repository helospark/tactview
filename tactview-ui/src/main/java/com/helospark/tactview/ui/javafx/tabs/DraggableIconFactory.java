package com.helospark.tactview.ui.javafx.tabs;

import java.io.InputStream;
import java.util.Optional;

import com.helospark.lightdi.annotation.Component;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;

@Component
public class DraggableIconFactory {
    private static final int ICON_SIZE = 50;

    public VBox createIcon(String effectId, String name, String iconUri) {
        return createIcon(effectId, name, iconUri, Optional.empty());
    }

    public VBox createIcon(String effectId, String name, String iconUri, Optional<String> description) {
        ImageView image = loadImageFromUri(iconUri);
        image.setPreserveRatio(true);
        image.setFitWidth(ICON_SIZE);
        Label text = new Label();
        text.setTextOverrun(OverrunStyle.ELLIPSIS);
        text.setEllipsisString("...");
        text.setText(name);

        VBox vbox = new VBox();
        vbox.getStyleClass().add("icon");
        vbox.getChildren().addAll(image, text);
        vbox.setPadding(new Insets(10));
        vbox.setAlignment(Pos.CENTER);

        String tooltipText = name;

        if (description.isPresent()) {
            tooltipText += "\n" + description.get();
        }

        Tooltip tooltip = new Tooltip(tooltipText);
        Tooltip.install(vbox, tooltip);

        vbox.setOnDragDetected(event -> {
            Dragboard db = vbox.startDragAndDrop(TransferMode.ANY);
            db.setDragView(image.getImage());

            ClipboardContent content = new ClipboardContent();
            content.putString(effectId);
            db.setContent(content);

            event.consume();
        });

        return vbox;
    }

    private ImageView loadImageFromUri(String iconUri) {
        Image image;
        if (iconUri.startsWith("file:")) {
            image = new Image(iconUri, ICON_SIZE, ICON_SIZE, true, false);
        } else if (iconUri.startsWith("classpath:")) {
            InputStream loadIconFile = this.getClass().getResourceAsStream(iconUri.replaceFirst("classpath:", ""));
            if (loadIconFile == null) {
                throw new IllegalArgumentException("File " + iconUri + " does not exist");
            }
            image = new Image(loadIconFile, ICON_SIZE, ICON_SIZE, true, false); // TODO: may be cached
        } else {
            throw new IllegalArgumentException("Uri " + iconUri + " must start with 'classpath:' or 'file:'");
        }

        return new ImageView(image);
    }
}
