package com.helospark.tactview.core.render;

import java.util.List;
import java.util.Map;

import com.helospark.tactview.core.optionprovider.OptionProvider;

public interface RenderService {

    void render(RenderRequest renderRequest);

    String getId();

    List<String> getSupportedFormats();

    Map<String, OptionProvider<?>> getOptionProviders();

    boolean supports(RenderRequest renderRequest);

}