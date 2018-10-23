package com.helospark.tactview.ui.javafx.inputmode.sizefunction;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.SizeFunction;
import com.helospark.tactview.ui.javafx.repository.UiProjectRepository;

@Component
public class SizeFunctionImplementation {
    private UiProjectRepository projectRepository;

    public SizeFunctionImplementation(UiProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public double scalePreviewDataUsingSizeFunction(double input, SizeFunction function, int maxSize) {
        switch (function) {
            case IMAGE_SIZE :
                return input * 1.0 / (projectRepository.getScaleFactor());
            case IMAGE_SIZE_IN_0_to_1_RANGE :
                return input / maxSize;
            case NO_TRANSFORMATION :
                return input;
            default :
                return input;
        }
    }

}
