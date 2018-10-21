package com.helospark.tactview.ui.javafx.uicomponents.propertyvalue;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;

import javafx.scene.control.TextField;

@Component
public class DoublePropertyValueSetterChainItem extends TypeBasedPropertyValueSetterChainItem<DoubleProvider> {

    public DoublePropertyValueSetterChainItem() {
        super(DoubleProvider.class);
    }

    @Override
    protected EffectLine handle(DoubleProvider doubleProvider) {
        TextField textField = new TextField();
        return PrimitiveEffectLine.builder()
                .withCurrentValueProvider(() -> textField.getText())
                .withDescriptorId(doubleProvider.getId())
                .withUpdateFunction(position -> textField.setText(doubleProviderValueToString(doubleProvider, position)))
                .withVisibleNode(textField)
                .build();

    }

    private String doubleProviderValueToString(DoubleProvider doubleProvider, TimelinePosition position) {
        return Double.toString(doubleProvider.getValueAt(position));
    }

}
