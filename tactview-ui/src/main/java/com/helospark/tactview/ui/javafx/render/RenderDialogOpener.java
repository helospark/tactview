package com.helospark.tactview.ui.javafx.render;

import java.util.List;
import java.util.stream.Collectors;

import com.helospark.lightdi.LightDiContext;
import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.aware.ContextAware;
import com.helospark.tactview.core.render.RenderService;
import com.helospark.tactview.core.render.RenderServiceChain;
import com.helospark.tactview.core.repository.ProjectRepository;

@Component
public class RenderDialogOpener implements ContextAware {
    private RenderServiceChain renderService;
    private ProjectRepository projectRepository;
    private LightDiContext context;

    public RenderDialogOpener(RenderServiceChain renderService, ProjectRepository projectRepository) {
        this.renderService = renderService;
        this.projectRepository = projectRepository;
    }

    public void render() {
        List<String> renderServices = context.getListOfBeans(RenderService.class)
                .stream()
                .map(a -> a.getId())
                .collect(Collectors.toList());
        RenderDialog renderDialog = new RenderDialog(renderServices, renderService, projectRepository);
        renderDialog.show();
    }

    @Override
    public void setContext(LightDiContext context) {
        this.context = context;
    }

}
