package com.helospark.tactview.ui.javafx.scenepostprocessor;

import static javafx.scene.input.KeyCode.DELETE;
import static javafx.scene.input.KeyCode.ESCAPE;
import static javafx.scene.input.KeyCode.K;
import static javafx.scene.input.KeyCode.LEFT;

import java.util.function.Consumer;

import com.helospark.lightdi.LightDiContext;
import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.aware.ContextAware;
import com.helospark.tactview.ui.javafx.RemoveClipService;
import com.helospark.tactview.ui.javafx.RemoveEffectService;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.UiTimelineManager;
import com.helospark.tactview.ui.javafx.key.GlobalShortcutHandler;
import com.helospark.tactview.ui.javafx.key.KeyCombinationRepository;
import com.helospark.tactview.ui.javafx.key.ShortcutExecutedEvent;
import com.helospark.tactview.ui.javafx.key.StandardGlobalShortcutHandler;
import com.helospark.tactview.ui.javafx.repository.CleanableMode;
import com.helospark.tactview.ui.javafx.repository.CopyPasteRepository;
import com.helospark.tactview.ui.javafx.repository.SelectedNodeRepository;
import com.helospark.tactview.ui.javafx.save.UiSaveHandler;
import com.helospark.tactview.ui.javafx.uicomponents.ClipCutService;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination.Modifier;

@Component
public class GlobalKeyCombinationAttacher implements ScenePostProcessor, ContextAware {
    private UiCommandInterpreterService commandInterpreter;
    private KeyCombinationRepository keyCombinationRepository;
    private SelectedNodeRepository selectedNodeRepository;
    private RemoveClipService removeClipService;
    private RemoveEffectService removeEffectService;
    private ClipCutService clipCutService;
    private UiTimelineManager uiTimelineManager;
    private CopyPasteRepository copyPasteRepository;
    private LightDiContext context;
    private UiSaveHandler uiSaveHandler;

    public GlobalKeyCombinationAttacher(UiCommandInterpreterService commandInterpreter, KeyCombinationRepository keyCombinationRepository, SelectedNodeRepository selectedNodeRepository,
            RemoveClipService removeClipService,
            RemoveEffectService removeEffectService, ClipCutService clipCutService,
            CopyPasteRepository copyPasteRepository,
            UiTimelineManager uiTimelineManager,
            UiSaveHandler uiSaveHandler) {
        this.commandInterpreter = commandInterpreter;
        this.keyCombinationRepository = keyCombinationRepository;
        this.selectedNodeRepository = selectedNodeRepository;
        this.removeClipService = removeClipService;
        this.removeEffectService = removeEffectService;
        this.clipCutService = clipCutService;
        this.uiTimelineManager = uiTimelineManager;
        this.uiSaveHandler = uiSaveHandler;
        this.copyPasteRepository = copyPasteRepository;
    }

    @Override
    public void postProcess(Scene scene) {
        setupDefaultKeyCombinations();

        keyCombinationRepository.getCombinations()
                .forEach(a -> {
                    scene.getAccelerators().put(a.getKey(), () -> a.getValue().onShortcutExecuted(new ShortcutExecutedEvent(a.getKey())));
                });
    }

    private void setupDefaultKeyCombinations() {
        // TODO: this should be only done if the user has not changed them
        //        keyCombinationRepository.registerKeyCombination(on(CONTROL_DOWN, Z),
        //                useHandler("Undo", event -> commandInterpreter.revertLast()));
        //        keyCombinationRepository.registerKeyCombination(on(CONTROL_DOWN, SHIFT_DOWN, Z),
        //                useHandler("Redo", event -> commandInterpreter.redoLast()));
        keyCombinationRepository.registerKeyCombination(on(DELETE),
                useHandler("Delete selected", event -> {
                    removeClipService.removeClips(selectedNodeRepository.getSelectedClipIds());
                    removeEffectService.removeEffects(selectedNodeRepository.getSelectedEffectIds());
                }));
        keyCombinationRepository.registerKeyCombination(on(K),
                useHandler("Cut clip at current position", event -> {
                    clipCutService.cutSelectedClipAtCurrentTimestamp();
                }));
        keyCombinationRepository.registerKeyCombination(on(ESCAPE),
                useHandler("Exit everything ongoing", event -> {
                    context.getListOfBeans(CleanableMode.class)
                            .stream()
                            .forEach(cleanable -> cleanable.clean());
                }));
        keyCombinationRepository.registerKeyCombination(on(LEFT),
                useHandler("Back one frame", event -> {
                    uiTimelineManager.moveBackOneFrame();
                }));
        keyCombinationRepository.registerKeyCombination(on(KeyCode.RIGHT),
                useHandler("Back one frame", event -> {
                    uiTimelineManager.moveForwardOneFrame();
                }));
    }

    private KeyCodeCombination on(KeyCode code) {
        return new KeyCodeCombination(code);
    }

    private KeyCodeCombination on(Modifier modifier, KeyCode code) {
        return new KeyCodeCombination(code, modifier);
    }

    private KeyCodeCombination on(Modifier modifier1, Modifier modifier2, KeyCode code) {
        return new KeyCodeCombination(code, modifier1, modifier2);
    }

    private GlobalShortcutHandler useHandler(String name, Consumer<ShortcutExecutedEvent> consumer) {
        return new StandardGlobalShortcutHandler(name, consumer);
    }

    @Override
    public void setContext(LightDiContext context) {
        this.context = context;
    }

}
