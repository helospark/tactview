package com.helospark.tactview.ui.javafx.uicomponents.propertyvalue.contextmenu;

import java.util.function.Function;

import com.helospark.lightdi.annotation.Bean;
import com.helospark.lightdi.annotation.Configuration;
import com.helospark.lightdi.annotation.Order;
import com.helospark.tactview.core.timeline.effect.EffectParametersRepository;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.UiTimelineManager;
import com.helospark.tactview.ui.javafx.tabs.curve.CurveEditorTab;
import com.helospark.tactview.ui.javafx.uicomponents.propertyvalue.PrimitiveEffectLine;

import javafx.scene.control.MenuItem;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

@Configuration
public class CommonPropertyValueContextMenuItemConfiguration {

    @Bean
    @Order(-20)
    public PropertyValueContextMenuItem copyValue(UiTimelineManager timelineManager) {
        return allPrimitiveEffectLineSupportingMenuIfRequired(request -> {
            MenuItem copyKeyframeMenuItem = new MenuItem("Copy");
            copyKeyframeMenuItem.setOnAction(e -> {
                String currentValue = ((PrimitiveEffectLine) (request.effectLine)).currentValueProvider.get();

                Clipboard clipboard = Clipboard.getSystemClipboard();
                ClipboardContent content = new ClipboardContent();
                content.putString(currentValue);
                clipboard.setContent(content);
            });
            return copyKeyframeMenuItem;
        });
    }

    @Bean
    @Order(-10)
    public PropertyValueContextMenuItem pasteValue(UiTimelineManager timelineManager) {
        return allPrimitiveEffectLineSupportingMenuIfRequired(request -> {
            MenuItem pasteKeyframeMenuItem = new MenuItem("Paste");
            pasteKeyframeMenuItem.setOnAction(e -> {
                Clipboard clipboard = Clipboard.getSystemClipboard();
                String value = clipboard.getString();
                // TODO: Maybe we need a value update function rather
                ((PrimitiveEffectLine) (request.effectLine)).sendKeyframeWithValue(timelineManager.getCurrentPosition(), value);
            });
            return pasteKeyframeMenuItem;
        });
    }

    @Bean
    @Order(-9)
    public PropertyValueContextMenuItem revealCurveInEditor(CurveEditorTab curveEditorTab) {
        return allPrimitiveEffectLineSupportingMenuIfRequired(request -> {
            MenuItem revealInEditorMenuItem = new MenuItem("Reveal curve in editor");
            revealInEditorMenuItem.setOnAction(e -> {
                curveEditorTab.revealInEditor(request.valueProvider);
            });
            return revealInEditorMenuItem;
        });
    }

    @Bean
    @Order(0)
    public PropertyValueContextMenuItem addKeyframeItem(UiTimelineManager timelineManager) {
        return contextMenuEnabledIfKeyframesEnabled(request -> {
            MenuItem addKeyframeMenuItem = new MenuItem("Add keyframe");
            addKeyframeMenuItem.setOnAction(e -> request.effectLine.sendKeyframe(timelineManager.getCurrentPosition()));
            return addKeyframeMenuItem;
        });
    }

    @Bean
    @Order(10)
    public PropertyValueContextMenuItem removeKeyframeItem(UiTimelineManager timelineManager, EffectParametersRepository effectParametersRepository, UiCommandInterpreterService commandInterpreter) {
        return contextMenuEnabledIfKeyframesEnabled(request -> {
            MenuItem removeKeyframeMenuItem = new MenuItem("Remove keyframe");
            removeKeyframeMenuItem.setOnAction(e -> {
                request.effectLine.removeKeyframe(timelineManager.getCurrentPosition());
            });
            return removeKeyframeMenuItem;
        });
    }

    @Bean
    @Order(20)
    public PropertyValueContextMenuItem removeAllAndSet(UiTimelineManager timelineManager, EffectParametersRepository effectParametersRepository, UiCommandInterpreterService commandInterpreter) {
        return contextMenuEnabledIfKeyframesEnabled(request -> {
            MenuItem removeAllAndSetMenuItemw = new MenuItem("Remove all and set");
            removeAllAndSetMenuItemw.setOnAction(e -> {
                request.effectLine.removeAllAndSetKeyframe(timelineManager.getCurrentPosition());
            });
            return removeAllAndSetMenuItemw;
        });
    }

    private PropertyValueContextMenuItem contextMenuEnabledIfKeyframesEnabled(Function<PropertyValueContextMenuRequest, MenuItem> function) {
        return new PropertyValueContextMenuItem() {

            @Override
            public boolean supports(PropertyValueContextMenuRequest request) {
                return request.valueProvider.keyframesEnabled();
            }

            @Override
            public MenuItem createMenuItem(PropertyValueContextMenuRequest request) {
                MenuItem result = function.apply(request);

                boolean isDisabled = request.containerDescriptor.getEnabledIf()
                        .map(enabledIf -> !enabledIf.apply(request.timelinePosition))
                        .orElse(false);
                if (isDisabled) {
                    result.setDisable(true);
                }

                return result;
            }
        };
    }

    private PropertyValueContextMenuItem allPrimitiveEffectLineSupportingMenuIfRequired(Function<PropertyValueContextMenuRequest, MenuItem> function) {
        return new PropertyValueContextMenuItem() {

            @Override
            public boolean supports(PropertyValueContextMenuRequest request) {
                return request.effectLine instanceof PrimitiveEffectLine;
            }

            @Override
            public MenuItem createMenuItem(PropertyValueContextMenuRequest request) {
                return function.apply(request);
            }
        };
    }

}
