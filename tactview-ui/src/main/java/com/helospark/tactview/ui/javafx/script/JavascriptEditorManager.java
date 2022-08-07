package com.helospark.tactview.ui.javafx.script;

import java.util.Optional;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.effect.interpolation.KeyframeableEffect;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.evaluator.script.JavascriptExpressionEvaluator;
import com.helospark.tactview.ui.javafx.GlobalTimelinePositionHolder;
import com.helospark.tactview.ui.javafx.UiMessagingService;
import com.helospark.tactview.ui.javafx.repository.NameToIdRepository;
import com.helospark.tactview.ui.javafx.stylesheet.StylesheetAdderService;
import com.helospark.tactview.ui.javafx.uicomponents.display.DisplayUpdatedListener;
import com.helospark.tactview.ui.javafx.uicomponents.display.DisplayUpdatedRequest;

@Component
public class JavascriptEditorManager implements DisplayUpdatedListener {
    private StylesheetAdderService stylesheetAdderService;
    private CachedFileContentReader cachedFileContentReader;
    private ScriptVariablesStore scriptVariablesStore;
    private JavascriptExpressionEvaluator javascriptExpressionEvaluator;
    private GlobalTimelinePositionHolder globalTimelinePositionHolder;
    private NameToIdRepository nameToIdRepository;
    private UiMessagingService messagingService;

    private JavascriptEditor openEditor;

    public JavascriptEditorManager(StylesheetAdderService stylesheetAdderService, CachedFileContentReader cachedFileContentReader,
            ScriptVariablesStore scriptVariablesStore, JavascriptExpressionEvaluator javascriptExpressionEvaluator,
            GlobalTimelinePositionHolder globalTimelinePositionHolder, NameToIdRepository nameToIdRepository,
            UiMessagingService messagingService) {
        this.stylesheetAdderService = stylesheetAdderService;
        this.cachedFileContentReader = cachedFileContentReader;
        this.scriptVariablesStore = scriptVariablesStore;
        this.javascriptExpressionEvaluator = javascriptExpressionEvaluator;
        this.globalTimelinePositionHolder = globalTimelinePositionHolder;
        this.nameToIdRepository = nameToIdRepository;
        this.messagingService = messagingService;
    }

    public Optional<String> openEditor(KeyframeableEffect<?> provider, Class<?> type) {
        openEditor = new JavascriptEditor(stylesheetAdderService, cachedFileContentReader, scriptVariablesStore, javascriptExpressionEvaluator, globalTimelinePositionHolder,
                nameToIdRepository, messagingService);

        Optional<String> result = openEditor.show(type, provider);

        openEditor = null;
        return result;
    }

    @Override
    public void displayUpdated(DisplayUpdatedRequest request) {
        if (openEditor != null) {
            openEditor.onDisplayUpdated();
        }
    }
}
