package com.helospark.tactview.ui.javafx;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.render.RenderRequest;
import com.helospark.tactview.core.render.RenderService;
import com.helospark.tactview.core.repository.ProjectRepository;
import com.helospark.tactview.core.timeline.TimelinePosition;

@Component
public class UiRenderService {
    private RenderService renderService;
    private ProjectRepository projectRepository;

    public UiRenderService(RenderService renderService, ProjectRepository projectRepository) {
        this.renderService = renderService;
        this.projectRepository = projectRepository;
    }

    public void renderProject() {
        RenderRequest request = RenderRequest.builder()
                .withWidth(projectRepository.getWidth())
                .withHeight(projectRepository.getHeight())
                .withStep(BigDecimal.ONE.divide(projectRepository.getFps(), 3, RoundingMode.HALF_UP))
                .withStartPosition(TimelinePosition.ofZero())
                .withEndPosition(new TimelinePosition(BigDecimal.valueOf(30)))
                .build();

        renderService.render(request);
    }

}
