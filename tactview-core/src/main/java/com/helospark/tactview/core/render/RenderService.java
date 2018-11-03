package com.helospark.tactview.core.render;

import java.util.List;

public interface RenderService {

    void render(RenderRequest renderRequest);

    String getId();

    List<String> getSupportedFormats();

    boolean supports(RenderRequest renderRequest);

}