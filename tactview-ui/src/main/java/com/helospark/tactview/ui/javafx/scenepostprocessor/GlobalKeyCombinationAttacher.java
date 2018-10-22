package com.helospark.tactview.ui.javafx.scenepostprocessor;

import static javafx.scene.input.KeyCode.DELETE;
import static javafx.scene.input.KeyCode.ESCAPE;
import static javafx.scene.input.KeyCode.K;
import static javafx.scene.input.KeyCode.Z;
import static javafx.scene.input.KeyCombination.CONTROL_DOWN;
import static javafx.scene.input.KeyCombination.SHIFT_DOWN;

import java.util.function.Consumer;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.ui.javafx.RemoveClipService;
import com.helospark.tactview.ui.javafx.RemoveEffectService;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.inputmode.InputModeRepository;
import com.helospark.tactview.ui.javafx.key.GlobalShortcutHandler;
import com.helospark.tactview.ui.javafx.key.KeyCombinationRepository;
import com.helospark.tactview.ui.javafx.key.StandardGlobalShortcutHandler;
import com.helospark.tactview.ui.javafx.repository.SelectedNodeRepository;
import com.helospark.tactview.ui.javafx.uicomponents.ClipCutService;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination.Modifier;
import javafx.scene.input.KeyEvent;

@Component
public class GlobalKeyCombinationAttacher implements ScenePostProcessor {
    private UiCommandInterpreterService commandInterpreter;
    private KeyCombinationRepository keyCombinationRepository;
    private SelectedNodeRepository selectedNodeRepository;
    private RemoveClipService removeClipService;
    private RemoveEffectService removeEffectService;
    private ClipCutService clipCutService;
    private InputModeRepository inputModeRepository;

    public GlobalKeyCombinationAttacher(UiCommandInterpreterService commandInterpreter, KeyCombinationRepository keyCombinationRepository, SelectedNodeRepository selectedNodeRepository,
            RemoveClipService removeClipService,
            RemoveEffectService removeEffectService, ClipCutService clipCutService,
            InputModeRepository inputModeRepository) {
        this.commandInterpreter = commandInterpreter;
        this.keyCombinationRepository = keyCombinationRepository;
        this.selectedNodeRepository = selectedNodeRepository;
        this.removeClipService = removeClipService;
        this.removeEffectService = removeEffectService;
        this.clipCutService = clipCutService;
        this.inputModeRepository = inputModeRepository;
    }

    @Override
    public void postProcess(Scene scene) {
        setupDefaultKeyCombinations();

        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            keyCombinationRepository.getCombinations()
                    .filter(a -> a.getKey().match(event))
                    .findFirst()
                    .ifPresent(a -> {
                        a.getValue().onShortcutExecuted(event);
                        event.consume();
                    });
        });
    }

    private void setupDefaultKeyCombinations() {
        // TODO: this should be only done if the user has not changed them
        keyCombinationRepository.registerKeyCombination(on(CONTROL_DOWN, Z),
                useHandler("Undo", event -> commandInterpreter.revertLast()));
        keyCombinationRepository.registerKeyCombination(on(CONTROL_DOWN, SHIFT_DOWN, Z),
                useHandler("Redo", event -> commandInterpreter.redoLast()));
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
                    inputModeRepository.reset();
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

    private GlobalShortcutHandler useHandler(String name, Consumer<KeyEvent> consumer) {
        return new StandardGlobalShortcutHandler(name, consumer);
    }

}
