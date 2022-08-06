package com.helospark.tactview.ui.javafx.uicomponents.propertyvalue.contextmenu;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helospark.lightdi.annotation.Bean;
import com.helospark.lightdi.annotation.Configuration;
import com.helospark.lightdi.annotation.Order;
import com.helospark.tactview.core.timeline.effect.EffectParametersRepository;
import com.helospark.tactview.core.timeline.message.KeyframeAddedRequest;
import com.helospark.tactview.ui.javafx.GlobalTimelinePositionHolder;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.commands.impl.AddKeyframeForPropertyCommand;
import com.helospark.tactview.ui.javafx.commands.impl.ExpressionChangedForPropertyCommand;
import com.helospark.tactview.ui.javafx.commands.impl.ExpressionRemovedForPropertyCommand;
import com.helospark.tactview.ui.javafx.commands.impl.ResetDefaultValuesCommand;
import com.helospark.tactview.ui.javafx.stylesheet.AlertDialogFactory;
import com.helospark.tactview.ui.javafx.uicomponents.propertyvalue.PrimitiveEffectLine;

import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;

@Configuration
public class CommonPropertyValueContextMenuItemConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommonPropertyValueContextMenuItemConfiguration.class);
    private static final DataFormat RAW_DATA_FORMAT = new DataFormat("raw");

    @Bean
    @Order(-20)
    public PropertyValueContextMenuItem copyValue() {
        return allPrimitiveEffectLineSupportingMenuIfRequired(request -> {
            MenuItem copyKeyframeMenuItem = new MenuItem("Copy");
            copyKeyframeMenuItem.setOnAction(e -> {
                Object currentValue = ((PrimitiveEffectLine) (request.effectLine)).getCurrentValueSupplier().get();

                Clipboard clipboard = Clipboard.getSystemClipboard();
                ClipboardContent content = new ClipboardContent();
                content.put(RAW_DATA_FORMAT, currentValue);
                clipboard.setContent(content);
            });
            return copyKeyframeMenuItem;
        });
    }

    @Bean
    @Order(-10)
    public PropertyValueContextMenuItem pasteValue() {
        return allPrimitiveEffectLineSupportingMenuIfRequired(request -> {
            MenuItem pasteKeyframeMenuItem = new MenuItem("Paste");
            pasteKeyframeMenuItem.setOnAction(e -> {
                Clipboard clipboard = Clipboard.getSystemClipboard();
                Object value = clipboard.getContent(RAW_DATA_FORMAT);
                if (value != null) {
                    try {
                        request.effectLine.getUpdateFromValue().accept(value);
                    } catch (Exception ex) {
                        LOGGER.debug("Trying to paste {} into type {}, which is not supported", value, request.containerDescriptor.getKeyframeableEffect().getClass(), ex);
                    }
                }
            });
            return pasteKeyframeMenuItem;
        });
    }

    @Bean
    @Order(-6)
    public PropertyValueContextMenuItem afterCopyPasteSeparator() {
        return alwaysEnableContextMenu(request -> {
            return new SeparatorMenuItem();
        });
    }

    @Bean
    @Order(-5)
    public PropertyValueContextMenuItem resetDefaultsValues(UiCommandInterpreterService commandInterpreter, EffectParametersRepository effectParametersRepository) {
        return alwaysEnableContextMenu(request -> {
            MenuItem resetDefaultsMenuItem = new MenuItem("Reset defaults");
            resetDefaultsMenuItem.setOnAction(e -> {
                commandInterpreter.sendWithResult(new ResetDefaultValuesCommand(effectParametersRepository, request.valueProvider.getId()));
            });
            return resetDefaultsMenuItem;
        });
    }

    @Bean
    @Order(0)
    public PropertyValueContextMenuItem addKeyframeItem(GlobalTimelinePositionHolder timelineManager, UiCommandInterpreterService commandInterpreter,
            EffectParametersRepository effectParametersRepository) {
        return contextMenuEnabledIfKeyframesEnabled(request -> {
            MenuItem addKeyframeMenuItem = new MenuItem("Add keyframe");
            addKeyframeMenuItem.setOnAction(e -> {
                KeyframeAddedRequest keyframeRequest = KeyframeAddedRequest.builder()
                        .withDescriptorId(request.effectLine.getDescriptorId())
                        .withGlobalTimelinePosition(timelineManager.getCurrentPosition())
                        .withValue(request.effectLine.getCurrentValueSupplier().get())
                        .withRevertable(true)
                        .build();

                commandInterpreter.sendWithResult(new AddKeyframeForPropertyCommand(effectParametersRepository, keyframeRequest));
            });
            return addKeyframeMenuItem;
        });
    }

    @Bean
    @Order(10)
    public PropertyValueContextMenuItem removeKeyframeItem(GlobalTimelinePositionHolder timelineManager, EffectParametersRepository effectParametersRepository,
            UiCommandInterpreterService commandInterpreter) {
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
    public PropertyValueContextMenuItem removeAllAndSet(GlobalTimelinePositionHolder timelineManager, EffectParametersRepository effectParametersRepository,
            UiCommandInterpreterService commandInterpreter) {
        return contextMenuEnabledIfKeyframesEnabled(request -> {
            MenuItem removeAllAndSetMenuItemw = new MenuItem("Remove all and set");
            removeAllAndSetMenuItemw.setOnAction(e -> {
                request.effectLine.removeAllAndSetKeyframe(timelineManager.getCurrentPosition());
            });
            return removeAllAndSetMenuItemw;
        });
    }

    @Bean
    @Order(34)
    public PropertyValueContextMenuItem beforeExpressionSeparator(AlertDialogFactory alertDialogFactory, EffectParametersRepository effectParametersRepository,
            UiCommandInterpreterService commandInterpreter) {
        return alwaysEnableContextMenu(request -> {
            return new SeparatorMenuItem();
        });
    }

    @Bean
    @Order(35)
    public PropertyValueContextMenuItem addExpressionMenuItem(AlertDialogFactory alertDialogFactory, EffectParametersRepository effectParametersRepository,
            UiCommandInterpreterService commandInterpreter) {
        return alwaysEnableContextMenu(request -> {
            MenuItem addExpressionMenuItem;
            if (request.valueProvider.getExpression() != null) {
                addExpressionMenuItem = new MenuItem("Change expression");
            } else {
                addExpressionMenuItem = new MenuItem("Add expression");
            }
            addExpressionMenuItem.setOnAction(e2 -> {
                Optional<String> expressionResult = alertDialogFactory.showTextInputDialog("Add expression", "Add expression", request.valueProvider.getExpression());
                if (expressionResult.isPresent() && !expressionResult.get().isBlank()) {

                    KeyframeAddedRequest keyframeAddedRequest = KeyframeAddedRequest.builder()
                            .withDescriptorId(request.valueProvider.getId())
                            .withRevertable(true)
                            .withValue(expressionResult.get())
                            .build();
                    commandInterpreter.synchronousSend(new ExpressionChangedForPropertyCommand(effectParametersRepository, keyframeAddedRequest));
                }
            });
            return addExpressionMenuItem;
        });
    }

    @Bean
    @Order(36)
    public PropertyValueContextMenuItem deleteExpressionMenuItem(AlertDialogFactory alertDialogFactory, EffectParametersRepository effectParametersRepository,
            UiCommandInterpreterService commandInterpreter) {
        return alwaysEnableContextMenu(request -> {
            MenuItem deleteExpressionMenuItem = new MenuItem("Delete expression");
            deleteExpressionMenuItem.setOnAction(e2 -> {
                commandInterpreter.synchronousSend(new ExpressionRemovedForPropertyCommand(effectParametersRepository, request.valueProvider.getId()));
            });
            return deleteExpressionMenuItem;
        });
    }

    @Bean
    @Order(37)
    public PropertyValueContextMenuItem copyFieldReferenceMenuItem(AlertDialogFactory alertDialogFactory, EffectParametersRepository effectParametersRepository,
            UiCommandInterpreterService commandInterpreter) {
        return alwaysEnableContextMenu(request -> {
            MenuItem copyFieldReferenceMenuItem = new MenuItem("Copy field reference");
            copyFieldReferenceMenuItem.setOnAction(e2 -> {
                effectParametersRepository.findContainingElementId(request.valueProvider.getId())
                        .ifPresent(componentId -> {
                            String dataToCopy = "data['" + componentId + "']." + request.containerDescriptor.getName() + "";
                            Clipboard.getSystemClipboard().setContent(Map.of(DataFormat.PLAIN_TEXT, dataToCopy));
                        });
            });
            return copyFieldReferenceMenuItem;
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

                boolean isDisabled = isKeyframingEnabled(request);
                if (isDisabled) {
                    result.setDisable(true);
                }

                return result;
            }
        };
    }

    private Boolean isKeyframingEnabled(PropertyValueContextMenuRequest request) {
        return request.containerDescriptor.getEnabledIf()
                .map(enabledIf -> !enabledIf.apply(request.timelinePosition))
                .orElse(false);
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

    private PropertyValueContextMenuItem alwaysEnableContextMenu(Function<PropertyValueContextMenuRequest, MenuItem> function) {
        return new PropertyValueContextMenuItem() {

            @Override
            public boolean supports(PropertyValueContextMenuRequest request) {
                return true;
            }

            @Override
            public MenuItem createMenuItem(PropertyValueContextMenuRequest request) {
                return function.apply(request);
            }
        };
    }

}
