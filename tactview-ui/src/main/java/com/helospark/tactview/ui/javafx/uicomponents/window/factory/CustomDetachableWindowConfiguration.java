package com.helospark.tactview.ui.javafx.uicomponents.window.factory;

import com.helospark.lightdi.annotation.Bean;
import com.helospark.lightdi.annotation.Configuration;
import com.helospark.tactview.ui.javafx.uicomponents.propertyvalue.graph.GraphingDialog;
import com.helospark.tactview.ui.javafx.uicomponents.window.AudioSpectrumWindow;
import com.helospark.tactview.ui.javafx.uicomponents.window.HistogramWindow;
import com.helospark.tactview.ui.javafx.uicomponents.window.RgbWaveformWindow;
import com.helospark.tactview.ui.javafx.uicomponents.window.VectorScopeWindow;
import com.helospark.tactview.ui.javafx.uicomponents.window.WaveformWindow;

@Configuration
public class CustomDetachableWindowConfiguration {

    @Bean
    public CustomDetachableWindowTabFactory audioSpectrumWindow(AudioSpectrumWindow window) {
        return new CustomDetachableWindowTabFactory(window);
    }

    @Bean
    public CustomDetachableWindowTabFactory histogramWindow(HistogramWindow window) {
        return new CustomDetachableWindowTabFactory(window);
    }

    @Bean
    public CustomDetachableWindowTabFactory rgbWaveformWindow(RgbWaveformWindow window) {
        return new CustomDetachableWindowTabFactory(window);
    }

    @Bean
    public CustomDetachableWindowTabFactory vectorscopeWindow(VectorScopeWindow window) {
        return new CustomDetachableWindowTabFactory(window);
    }

    @Bean
    public CustomDetachableWindowTabFactory waveformWindow(WaveformWindow window) {
        return new CustomDetachableWindowTabFactory(window);
    }

    @Bean
    public CustomDetachableWindowTabFactory graphingWindow(GraphingDialog window) {
        return new CustomDetachableWindowTabFactory(window);
    }
}
