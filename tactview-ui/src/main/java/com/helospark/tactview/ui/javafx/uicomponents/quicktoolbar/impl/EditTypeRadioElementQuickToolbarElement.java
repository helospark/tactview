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
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.Tooltip;

@Component
@Order(50)
public class EditTypeRadioElementQuickToolbarElement implements QuickToolbarMenuElement {
    private static final String EDIT_TYPE_CLASS = "edit-type-button";
    private static final String MENU_BUTTON_ENABLED_CLASS = "menu-button-button-enabled";
    private TimelineEditModeRepository timelineEditModeRepository;

    public EditTypeRadioElementQuickToolbarElement(TimelineEditModeRepository timelineEditModeRepository) {
        this.timelineEditModeRepository = timelineEditModeRepository;
    }

    @Override
    public List<Node> getQuickMenuBarElements() {
        Button normalModeButton = new Button("", new Glyph("FontAwesome", FontAwesome.Glyph.ANGLE_RIGHT));
        normalModeButton.setTooltip(new Tooltip("Normal mode"));
        normalModeButton.getStyleClass().add(EDIT_TYPE_CLASS);
        normalModeButton.getStyleClass().add(MENU_BUTTON_ENABLED_CLASS);
        normalModeButton.setOnMouseClicked(event -> {
            timelineEditModeRepository.setMode(TimelineEditMode.NORMAL);
        });

        Button rippleModeButton = new Button("", new Glyph("FontAwesome", FontAwesome.Glyph.ANGLE_DOUBLE_RIGHT));
        rippleModeButton.setTooltip(new Tooltip("Single channel ripple mode"));
        rippleModeButton.getStyleClass().add(EDIT_TYPE_CLASS);
        rippleModeButton.setOnMouseClicked(event -> {
            timelineEditModeRepository.setMode(TimelineEditMode.SINGLE_CHANNEL_RIPPLE);

        });

        Button fullRippleModeButton = new Button("", new Glyph("FontAwesome", FontAwesome.Glyph.ASTERISK));
        fullRippleModeButton.setTooltip(new Tooltip("Full ripple mode"));
        fullRippleModeButton.getStyleClass().add(EDIT_TYPE_CLASS);
        fullRippleModeButton.setOnMouseClicked(event -> {
            timelineEditModeRepository.setMode(TimelineEditMode.ALL_CHANNEL_RIPPLE);
        });

        timelineEditModeRepository.getModeProperty().addListener((a, oldValue, newValue) -> {
            clearEnableClass(normalModeButton, rippleModeButton, fullRippleModeButton);
            if (newValue.equals(TimelineEditMode.NORMAL)) {
                normalModeButton.getStyleClass().add(MENU_BUTTON_ENABLED_CLASS);
            } else if (newValue.equals(TimelineEditMode.SINGLE_CHANNEL_RIPPLE)) {
                rippleModeButton.getStyleClass().add(MENU_BUTTON_ENABLED_CLASS);
            } else if (newValue.equals(TimelineEditMode.ALL_CHANNEL_RIPPLE)) {
                fullRippleModeButton.getStyleClass().add(MENU_BUTTON_ENABLED_CLASS);
            }
        });

        return List.of(normalModeButton, rippleModeButton, fullRippleModeButton, new Separator(VERTICAL));
    }

    private void clearEnableClass(Button... buttons) {
        for (var button : buttons) {
            button.getStyleClass().remove(MENU_BUTTON_ENABLED_CLASS);
        }
    }

}
