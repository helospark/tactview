package com.helospark.tactview.ui.javafx.scenepostprocessor;

import static javafx.scene.input.KeyCode.ESCAPE;
import static javafx.scene.input.KeyCode.K;
import static javafx.scene.input.KeyCode.LEFT;

import java.math.BigDecimal;
import java.util.Set;
import java.util.function.Consumer;

import com.helospark.lightdi.LightDiContext;
import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.aware.ContextAware;
import com.helospark.tactview.ui.javafx.RemoveClipService;
import com.helospark.tactview.ui.javafx.RemoveEffectService;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.UiTimelineManager;
import com.helospark.tactview.ui.javafx.aware.MainWindowStageAware;
import com.helospark.tactview.ui.javafx.key.GlobalShortcutHandler;
import com.helospark.tactview.ui.javafx.key.KeyCombinationRepository;
import com.helospark.tactview.ui.javafx.key.KeyCombinationRepository.GlobalFilterShortcutInfo;
import com.helospark.tactview.ui.javafx.key.ShortcutExecutedEvent;
import com.helospark.tactview.ui.javafx.key.StandardGlobalShortcutHandler;
import com.helospark.tactview.ui.javafx.repository.CleanableMode;
import com.helospark.tactview.ui.javafx.repository.CopyPasteRepository;
import com.helospark.tactview.ui.javafx.repository.SelectedNodeRepository;
import com.helospark.tactview.ui.javafx.save.UiSaveHandler;
import com.helospark.tactview.ui.javafx.uicomponents.ClipCutService;
import com.helospark.tactview.ui.javafx.uicomponents.PropertyView;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination.Modifier;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

@Component
public class GlobalKeyCombinationAttacher implements ScenePostProcessor, ContextAware, MainWindowStageAware {
    private UiCommandInterpreterService commandInterpreter;
    private KeyCombinationRepository keyCombinationRepository;
    private SelectedNodeRepository selectedNodeRepository;
    private RemoveClipService removeClipService;
    private RemoveEffectService removeEffectService;
    private ClipCutService clipCutService;
    private UiTimelineManager uiTimelineManager;
    private CopyPasteRepository copyPasteRepository;
    private LightDiContext context;
    private PropertyView propertyView;

    private Scene scene;

    public GlobalKeyCombinationAttacher(UiCommandInterpreterService commandInterpreter, KeyCombinationRepository keyCombinationRepository, SelectedNodeRepository selectedNodeRepository,
            RemoveClipService removeClipService,
            RemoveEffectService removeEffectService, ClipCutService clipCutService,
            CopyPasteRepository copyPasteRepository,
            UiTimelineManager uiTimelineManager,
            UiSaveHandler uiSaveHandler,
            PropertyView propertyView) {
        this.commandInterpreter = commandInterpreter;
        this.keyCombinationRepository = keyCombinationRepository;
        this.selectedNodeRepository = selectedNodeRepository;
        this.removeClipService = removeClipService;
        this.removeEffectService = removeEffectService;
        this.clipCutService = clipCutService;
        this.uiTimelineManager = uiTimelineManager;
        this.propertyView = propertyView;
        this.copyPasteRepository = copyPasteRepository;
    }

    @Override
    public void postProcess(Scene scene) {
        this.scene = scene;
        setupDefaultKeyCombinations(scene);

        keyCombinationRepository.getCombinations()
                .forEach(a -> {
                    scene.getAccelerators().put(a.getKey(), () -> a.getValue().onShortcutExecuted(new ShortcutExecutedEvent(a.getKey())));
                });

    }

    private void setupDefaultKeyCombinations(Scene scene) {
        // TODO: this should be only done if the user has not changed them

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

        Set<Class<? extends Node>> disabledFocusedNodeClass = Set.of(TextField.class);
        keyCombinationRepository.registerGlobalKeyFilters(LEFT,
                useHandler("Back one frame", event -> {
                    uiTimelineManager.moveBackOneFrame();
                }), disabledFocusedNodeClass);
        keyCombinationRepository.registerGlobalKeyFilters(KeyCode.RIGHT,
                useHandler("Back one frame", event -> {
                    uiTimelineManager.moveForwardOneFrame();
                }), disabledFocusedNodeClass);

        keyCombinationRepository.registerGlobalKeyFilters(KeyCode.PAGE_UP,
                useHandler("Back one frame", event -> {
                    uiTimelineManager.jumpRelative(BigDecimal.valueOf(10));
                }), disabledFocusedNodeClass);
        keyCombinationRepository.registerGlobalKeyFilters(KeyCode.PAGE_DOWN,
                useHandler("Back one frame", event -> {
                    uiTimelineManager.jumpRelative(BigDecimal.valueOf(-10));
                }), disabledFocusedNodeClass);
        keyCombinationRepository.registerGlobalKeyFilters(KeyCode.DELETE,
                useHandler("Delete selected", event -> {
                    removeClipService.removeClips(selectedNodeRepository.getSelectedClipIds());
                    removeEffectService.removeEffects(selectedNodeRepository.getSelectedEffectIds());
                }), disabledFocusedNodeClass);
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

    @Override
    public void setMainWindowStage(Stage stage) {

        stage.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            GlobalFilterShortcutInfo element = keyCombinationRepository.getGlobalFilters().get(event.getCode());

            if (element != null && !isDisabled(element)) {
                element.handler.onShortcutExecuted(new ShortcutExecutedEvent(new KeyCodeCombination(event.getCode())));
                event.consume();
            }
        });
    }

    private boolean isDisabled(GlobalFilterShortcutInfo element) {
        Node focusOwner = scene.getFocusOwner();
        if (focusOwner != null) {
            return element.disabledOnNodes
                    .stream()
                    .filter(a -> a.isAssignableFrom(focusOwner.getClass()))
                    .findAny()
                    .isPresent();
        }
        return false;
    }

}
