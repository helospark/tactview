package com.helospark.tactview.ui.javafx.uicomponents.quicktoolbar.impl;

import static javafx.geometry.Orientation.VERTICAL;

import java.util.List;

import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;

import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.annotation.Order;
import com.helospark.tactview.ui.javafx.repository.timelineeditmode.TimelineEditMode;
import com.helospark.tactview.ui.javafx.repository.timelineeditmode.TimelineEditModeRepository;
import com.helospark.tactview.ui.javafx.uicomponents.quicktoolbar.QuickToolbarMenuElement;

import javafx.scene.Node;
import javafx.scene.control.Separator;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;

@Component
@Order(60)
public class SpecialPointEnabledToolbarElement implements QuickToolbarMenuElement {
    private static final String EDIT_TYPE_CLASS = "edit-type-button";
    private TimelineEditModeRepository timelineEditModeRepository;

    public SpecialPointEnabledToolbarElement(TimelineEditModeRepository timelineEditModeRepository) {
        this.timelineEditModeRepository = timelineEditModeRepository;
    }

    @Override
    public List<Node> getQuickMenuBarElements() {
        ToggleButton magnetModeButton = new ToggleButton("", new Glyph("FontAwesome", FontAwesome.Glyph.MAGNET));
        magnetModeButton.selectedProperty().bindBidirectional(timelineEditModeRepository.getMagnetEditMode());
        magnetModeButton.setTooltip(new Tooltip("Magnet mode"));
        magnetModeButton.getStyleClass().add(EDIT_TYPE_CLASS);
        magnetModeButton.setOnMouseClicked(event -> {
            timelineEditModeRepository.setMode(TimelineEditMode.NORMAL);
        });

        return List.of(magnetModeButton, new Separator(VERTICAL));
    }

}
