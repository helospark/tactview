package com.helospark.tactview.ui.javafx.tabs;

import java.io.InputStream;

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

    public VBox createIcon(String effectId, String name, String iconUri) {
        ImageView image = loadImageFromUri(iconUri);
        image.setPreserveRatio(true);
        image.setFitWidth(50);
        Label text = new Label();
        text.setTextOverrun(OverrunStyle.ELLIPSIS);
        text.setEllipsisString("...");
        text.setText(name);

        VBox vbox = new VBox();
        vbox.getStyleClass().add("icon");
        vbox.getChildren().addAll(image, text);
        vbox.setPadding(new Insets(10));
        vbox.setAlignment(Pos.CENTER);

        Tooltip tooltip = new Tooltip(name);
        Tooltip.install(vbox, tooltip);

        vbox.setOnDragDetected(event -> {
            /* drag was detected, start drag-and-drop gesture */
            System.out.println("onDragDetected");

            /* allow any transfer mode */
            Dragboard db = vbox.startDragAndDrop(TransferMode.ANY);

            /* put a string on dragboard */
            ClipboardContent content = new ClipboardContent();
            content.putString(effectId);
            db.setContent(content);

            event.consume();
        });

        return vbox;
    }

    private ImageView loadImageFromUri(String iconUri) {
        if (iconUri.startsWith("file:")) {
            return new ImageView(iconUri);
        } else if (iconUri.startsWith("classpath:")) {
            InputStream loadIconFile = this.getClass().getResourceAsStream(iconUri.replaceFirst("classpath:", ""));
            if (loadIconFile == null) {
                throw new IllegalArgumentException("File " + iconUri + " does not exist");
            }
            Image image = new Image(loadIconFile); // TODO: may be cached
            return new ImageView(image);
        } else {
            throw new IllegalArgumentException("Uri " + iconUri + " must start with 'classpath:' or 'file:'");
        }
    }
}
