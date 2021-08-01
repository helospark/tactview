package com.helospark.tactview.ui.javafx.uicomponents.quicktoolbar.impl;

import static javafx.geometry.Orientation.VERTICAL;

import java.util.List;

import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;

import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.annotation.Order;
import com.helospark.tactview.ui.javafx.uicomponents.UiCutHandler;
import com.helospark.tactview.ui.javafx.uicomponents.quicktoolbar.QuickToolbarMenuElement;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.Tooltip;

@Component
@Order(20)
public class CutQuickToolbarElement implements QuickToolbarMenuElement {
    private UiCutHandler uiCutHandler;

    public CutQuickToolbarElement(UiCutHandler uiCutHandler) {
        this.uiCutHandler = uiCutHandler;
    }

    @Override
    public List<Node> getQuickMenuBarElements() {
        Button cutAllClipsButton = new Button("", new Glyph("FontAwesome", FontAwesome.Glyph.CUT));
        cutAllClipsButton.setTooltip(new Tooltip("Cut all clips at cursor position"));

        cutAllClipsButton.setOnMouseClicked(event -> {
            uiCutHandler.cutAllAtCurrentPosition();
        });
        return List.of(cutAllClipsButton, new Separator(VERTICAL));
    }

}
