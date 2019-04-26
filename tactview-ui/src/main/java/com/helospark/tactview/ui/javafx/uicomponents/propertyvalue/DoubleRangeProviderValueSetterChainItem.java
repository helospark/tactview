package com.helospark.tactview.ui.javafx.uicomponents.propertyvalue;

import java.util.List;
import java.util.Objects;

import org.controlsfx.control.RangeSlider;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.effect.EffectParametersRepository;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.DoubleRange;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleRangeProvider;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.UiTimelineManager;
import com.helospark.tactview.ui.javafx.uicomponents.propertyvalue.contextmenu.ContextMenuAppender;

import javafx.scene.layout.HBox;

@Component
public class DoubleRangeProviderValueSetterChainItem extends TypeBasedPropertyValueSetterChainItem<DoubleRangeProvider> {
    private DoublePropertyValueSetterChainItem doublePropertyValueSetterChainItem;
    private UiCommandInterpreterService commandInterpreter;
    private EffectParametersRepository effectParametersRepository;
    private UiTimelineManager timelineManager;
    private ContextMenuAppender contextMenuAppender;

    public DoubleRangeProviderValueSetterChainItem(DoublePropertyValueSetterChainItem doublePropertyValueSetterChainItem, UiCommandInterpreterService commandInterpreter,
            EffectParametersRepository effectParametersRepository, UiTimelineManager timelineManager, ContextMenuAppender contextMenuAppender) {
        super(DoubleRangeProvider.class);
        this.doublePropertyValueSetterChainItem = doublePropertyValueSetterChainItem;
        this.commandInterpreter = commandInterpreter;
        this.effectParametersRepository = effectParametersRepository;
        this.timelineManager = timelineManager;
        this.contextMenuAppender = contextMenuAppender;
    }

    @Override
    protected EffectLine handle(DoubleRangeProvider doubleRangeProvider, ValueProviderDescriptor descriptor) {
        PrimitiveEffectLine lowEndProvider = (PrimitiveEffectLine) doublePropertyValueSetterChainItem.create(descriptor, doubleRangeProvider.getLowEnd());
        PrimitiveEffectLine highEndProvider = (PrimitiveEffectLine) doublePropertyValueSetterChainItem.create(descriptor, doubleRangeProvider.getHighEnd());

        RangeSlider rangeSlider = new RangeSlider(doubleRangeProvider.getMin(), doubleRangeProvider.getMax(), doubleRangeProvider.getMin(), doubleRangeProvider.getMax());
        rangeSlider.setShowTickMarks(true);
        rangeSlider.setShowTickLabels(true);

        HBox box = new HBox();
        box.getChildren().add(rangeSlider);

        CompositeEffectLine result = CompositeEffectLine
                .builder()
                .withVisibleNode(box)
                .withValues(List.of(lowEndProvider, highEndProvider))
                .withDescriptorId(doubleRangeProvider.getId())
                .withEffectParametersRepository(effectParametersRepository)
                .withCommandInterpreter(commandInterpreter)
                .withDescriptor(descriptor)
                .withUpdateFromValue(value -> {
                    DoubleRange point = (DoubleRange) value;
                    lowEndProvider.getUpdateFromValue().accept(point.lowEnd);
                    highEndProvider.getUpdateFromValue().accept(point.highEnd);
                })
                .withAdditionalUpdateUi(time -> {
                    double lowEnd = effectLineToDouble(lowEndProvider);
                    double highEnd = effectLineToDouble(highEndProvider);
                    System.out.println("Lowhigh: " + lowEnd + " " + highEnd);
                    if (!rangeSlider.isFocused()) {

                        rangeSlider.setLowValue(lowEnd);
                        rangeSlider.setHighValue(highEnd);
                    }
                })
                .build();

        rangeSlider.lowValueProperty().addListener((obj, oldValue, newValue) -> {
            if (!Objects.equals(oldValue, newValue)) {
                lowEndProvider.getUpdateFromValue().accept(newValue);
                lowEndProvider.sendKeyframeWithValue(timelineManager.getCurrentPosition(), String.valueOf(newValue));
            }
        });
        rangeSlider.highValueProperty().addListener((obj, oldValue, newValue) -> {
            System.out.println("High value change " + newValue);
            if (!Objects.equals(oldValue, newValue)) {
                lowEndProvider.getUpdateFromValue().accept(newValue);
                highEndProvider.sendKeyframeWithValue(timelineManager.getCurrentPosition(), String.valueOf(newValue));
            }
        });
        contextMenuAppender.addContextMenu(result, doubleRangeProvider, descriptor, rangeSlider);

        return result;
    }

    private double effectLineToDouble(PrimitiveEffectLine provider) {
        try {
            return Double.valueOf(provider.currentValueProvider.get());
        } catch (Exception e) {
            return 0.0;
        }
    }

}
