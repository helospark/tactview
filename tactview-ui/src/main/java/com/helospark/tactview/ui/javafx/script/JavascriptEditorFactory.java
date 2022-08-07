package com.helospark.tactview.ui.javafx.script;

import java.util.Optional;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.evaluator.script.JavascriptExpressionEvaluator;
import com.helospark.tactview.ui.javafx.GlobalTimelinePositionHolder;
import com.helospark.tactview.ui.javafx.repository.NameToIdRepository;
import com.helospark.tactview.ui.javafx.stylesheet.StylesheetAdderService;

@Component
public class JavascriptEditorFactory {
    private StylesheetAdderService stylesheetAdderService;
    private CachedFileContentReader cachedFileContentReader;
    private ScriptVariablesStore scriptVariablesStore;
    private JavascriptExpressionEvaluator javascriptExpressionEvaluator;
    private GlobalTimelinePositionHolder globalTimelinePositionHolder;
    private NameToIdRepository nameToIdRepository;

    public JavascriptEditorFactory(StylesheetAdderService stylesheetAdderService, CachedFileContentReader cachedFileContentReader,
            ScriptVariablesStore scriptVariablesStore, JavascriptExpressionEvaluator javascriptExpressionEvaluator,
            GlobalTimelinePositionHolder globalTimelinePositionHolder, NameToIdRepository nameToIdRepository) {
        this.stylesheetAdderService = stylesheetAdderService;
        this.cachedFileContentReader = cachedFileContentReader;
        this.scriptVariablesStore = scriptVariablesStore;
        this.javascriptExpressionEvaluator = javascriptExpressionEvaluator;
        this.globalTimelinePositionHolder = globalTimelinePositionHolder;
        this.nameToIdRepository = nameToIdRepository;
    }

    public Optional<String> openEditor(String initialValue, Class<?> type) {
        return new JavascriptEditor(stylesheetAdderService, cachedFileContentReader, scriptVariablesStore, javascriptExpressionEvaluator, globalTimelinePositionHolder, nameToIdRepository)
                .show(initialValue, type);
    }
}
