package com.helospark.tactview.ui.javafx.scenepostprocessor;

import static javafx.scene.input.KeyCode.ESCAPE;
import static javafx.scene.input.KeyCode.K;
import static javafx.scene.input.KeyCode.LEFT;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.helospark.lightdi.LightDiContext;
import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.aware.ContextAware;
import com.helospark.tactview.ui.javafx.GlobalTimelinePositionHolder;
import com.helospark.tactview.ui.javafx.JavaFXUiMain;
import com.helospark.tactview.ui.javafx.RemoveClipService;
import com.helospark.tactview.ui.javafx.RemoveEffectService;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.aware.MainWindowStageAware;
import com.helospark.tactview.ui.javafx.commands.impl.service.MoveByUnitService;
import com.helospark.tactview.ui.javafx.hotkey.HotKeyRepository;
import com.helospark.tactview.ui.javafx.key.GlobalShortcutHandler;
import com.helospark.tactview.ui.javafx.key.KeyCombinationRepository;
import com.helospark.tactview.ui.javafx.key.KeyCombinationRepository.GlobalFilterShortcutInfo;
import com.helospark.tactview.ui.javafx.key.ShortcutExecutedEvent;
import com.helospark.tactview.ui.javafx.key.StandardGlobalShortcutHandler;
import com.helospark.tactview.ui.javafx.repository.CleanableMode;
import com.helospark.tactview.ui.javafx.repository.SelectedNodeRepository;
import com.helospark.tactview.ui.javafx.repository.timelineeditmode.TimelineEditMode;
import com.helospark.tactview.ui.javafx.repository.timelineeditmode.TimelineEditModeRepository;
import com.helospark.tactview.ui.javafx.save.UiSaveHandler;
import com.helospark.tactview.ui.javafx.uicomponents.ClipCutService;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyCombination.Modifier;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

@Component
public class GlobalKeyCombinationAttacher implements ScenePostProcessor, ContextAware, MainWindowStageAware {
    public static final String MOVE_FORWARD_ONE_FRAME = "moveForwardOneFrame";
    public static final String RIPPLE_DELETE = "Ripple delete";
    public static final String DELETE = "delete";
    public static final String FORWARD10S = "forward10s";
    public static final String BACK10S = "back10s";
    public static final String FORWARD_ONE_FRAME = "forwardOneFrame";
    public static final String BACK_ONE_FRAME = "backOneFrame";
    public static final String EXIT_EVERYTHING = "exitEverything";
    public static final String CUT_CLIP_AT_CURRENT_POSITION = "cutClipAtCurrentPosition";

    private KeyCombinationRepository keyCombinationRepository;
    private SelectedNodeRepository selectedNodeRepository;
    private RemoveClipService removeClipService;
    private RemoveEffectService removeEffectService;
    private ClipCutService clipCutService;
    private GlobalTimelinePositionHolder uiTimelineManager;
    private LightDiContext context;
    private TimelineEditModeRepository editModeRepository;
    private HotKeyRepository hotKeyRepository;
    private MoveByUnitService moveByUnitService;

    private Scene scene;

    public GlobalKeyCombinationAttacher(UiCommandInterpreterService commandInterpreter,
            KeyCombinationRepository keyCombinationRepository,
            SelectedNodeRepository selectedNodeRepository,
            RemoveClipService removeClipService,
            RemoveEffectService removeEffectService,
            ClipCutService clipCutService,
            GlobalTimelinePositionHolder uiTimelineManager,
            UiSaveHandler uiSaveHandler,
            TimelineEditModeRepository editModeRepository,
            HotKeyRepository hotKeyRepository,
            MoveByUnitService moveByUnitService) {
        this.keyCombinationRepository = keyCombinationRepository;
        this.selectedNodeRepository = selectedNodeRepository;
        this.removeClipService = removeClipService;
        this.removeEffectService = removeEffectService;
        this.clipCutService = clipCutService;
        this.uiTimelineManager = uiTimelineManager;
        this.editModeRepository = editModeRepository;
        this.hotKeyRepository = hotKeyRepository;
        this.moveByUnitService = moveByUnitService;
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
        hotKeyRepository.registerOrGetHotKey(CUT_CLIP_AT_CURRENT_POSITION, on(K), "Cut clip at current position");
        hotKeyRepository.registerOrGetHotKey(EXIT_EVERYTHING, on(ESCAPE), "Exit everything ongoing");
        hotKeyRepository.registerOrGetHotKey(BACK_ONE_FRAME, on(LEFT), "Back one frame");
        hotKeyRepository.registerOrGetHotKey(FORWARD_ONE_FRAME, on(KeyCode.RIGHT), "Forward one frame");
        hotKeyRepository.registerOrGetHotKey(BACK10S, on(KeyCode.PAGE_DOWN), "Back 10s");
        hotKeyRepository.registerOrGetHotKey(FORWARD10S, on(KeyCode.PAGE_UP), "Forward 10s");
        hotKeyRepository.registerOrGetHotKey(DELETE, on(KeyCode.DELETE), "Delete selected");
        hotKeyRepository.registerOrGetHotKey(RIPPLE_DELETE, on(KeyCode.X), RIPPLE_DELETE);

        keyCombinationRepository.registerKeyCombination(hotKeyRepository.getHotKeyById(CUT_CLIP_AT_CURRENT_POSITION),
                useHandler(event -> {
                    clipCutService.cutSelectedClipAtCurrentTimestamp();
                }));
        keyCombinationRepository.registerKeyCombination(hotKeyRepository.getHotKeyById(EXIT_EVERYTHING),
                useHandler(event -> {
                    if (scene.getFocusOwner() instanceof Control) {
                        JavaFXUiMain.canvas.requestFocus(); // remove focus from any control element
                    }
                    List<CleanableMode> elementsToClear = context.getListOfBeans(CleanableMode.class)
                            .stream()
                            .filter(cleanable -> !cleanable.isClean())
                            .sorted((a, b) -> Integer.compare(b.cleanPriority(), a.cleanPriority()))
                            .collect(Collectors.toList());
                    for (int i = 0; i < elementsToClear.size(); ++i) {
                        elementsToClear.get(i).clean();
                        if (elementsToClear.get(i).shouldConsumeCleanEvent()) {
                            break;
                        }
                    }
                }));

        Set<Class<? extends Node>> disabledFocusedNodeClass = Set.of(TextInputControl.class);
        keyCombinationRepository.registerGlobalKeyFilters(hotKeyRepository.getHotKeyById(BACK_ONE_FRAME),
                useHandler(event -> {
                    uiTimelineManager.moveBackOneFrame();
                }), disabledFocusedNodeClass);
        keyCombinationRepository.registerGlobalKeyFilters(hotKeyRepository.getHotKeyById(FORWARD_ONE_FRAME),
                useHandler(event -> {
                    uiTimelineManager.moveForwardOneFrame();
                }), disabledFocusedNodeClass);

        registerMoveByOneUnit(disabledFocusedNodeClass);

        keyCombinationRepository.registerGlobalKeyFilters(hotKeyRepository.getHotKeyById(BACK10S),
                useHandler(event -> {
                    uiTimelineManager.jumpRelative(BigDecimal.valueOf(10));
                }), disabledFocusedNodeClass);
        keyCombinationRepository.registerGlobalKeyFilters(hotKeyRepository.getHotKeyById(FORWARD10S),
                useHandler(event -> {
                    uiTimelineManager.jumpRelative(BigDecimal.valueOf(-10));
                }), disabledFocusedNodeClass);
        keyCombinationRepository.registerGlobalKeyFilters(hotKeyRepository.getHotKeyById(DELETE),
                useHandler(event -> {
                    if (editModeRepository.isRippleDeleteEnabled()) {
                        removeClipService.rippleDeleteClips(selectedNodeRepository.getSelectedClipIds(), editModeRepository.getMode());
                    } else {
                        removeClipService.removeClips(selectedNodeRepository.getSelectedClipIds());
                    }
                    removeEffectService.removeEffects(selectedNodeRepository.getSelectedEffectIds());
                }), disabledFocusedNodeClass);
        keyCombinationRepository.registerGlobalKeyFilters(hotKeyRepository.getHotKeyById(RIPPLE_DELETE),
                useHandler(event -> {
                    removeClipService.rippleDeleteClips(selectedNodeRepository.getSelectedClipIds(), TimelineEditMode.ALL_CHANNEL_RIPPLE);
                    removeEffectService.removeEffects(selectedNodeRepository.getSelectedEffectIds());
                }), disabledFocusedNodeClass);
    }

    private void registerMoveByOneUnit(Set<Class<? extends Node>> disabledFocusedNodeClass) {
        keyCombinationRepository.registerGlobalKeyFilters(
                hotKeyRepository.registerOrGetHotKey("moveForwardOneFrame", new KeyCodeCombination(KeyCode.RIGHT, KeyCombination.CONTROL_DOWN), "Move selected forward one frame"),
                useHandler(event -> {
                    moveByUnitService.moveByOneUnit(MoveByUnitService.Direction.RIGHT);
                }), disabledFocusedNodeClass);

        keyCombinationRepository.registerGlobalKeyFilters(
                hotKeyRepository.registerOrGetHotKey("moveBackwardOneFrame", new KeyCodeCombination(KeyCode.LEFT, KeyCombination.CONTROL_DOWN), "Move selected backward one frame"),
                useHandler(event -> {
                    moveByUnitService.moveByOneUnit(MoveByUnitService.Direction.LEFT);
                }), disabledFocusedNodeClass);

        keyCombinationRepository.registerGlobalKeyFilters(
                hotKeyRepository.registerOrGetHotKey("moveDownOneChannel", new KeyCodeCombination(KeyCode.DOWN, KeyCombination.CONTROL_DOWN), "Move selected down one channel"),
                useHandler(event -> {
                    moveByUnitService.moveByOneUnit(MoveByUnitService.Direction.DOWN);
                }), disabledFocusedNodeClass);

        keyCombinationRepository.registerGlobalKeyFilters(
                hotKeyRepository.registerOrGetHotKey("moveUpOneChannel", new KeyCodeCombination(KeyCode.UP, KeyCombination.CONTROL_DOWN), "Move selected up one channel"),
                useHandler(event -> {
                    moveByUnitService.moveByOneUnit(MoveByUnitService.Direction.UP);
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

    private GlobalShortcutHandler useHandler(Consumer<ShortcutExecutedEvent> consumer) {
        return new StandardGlobalShortcutHandler(consumer);
    }

    @Override
    public void setContext(LightDiContext context) {
        this.context = context;
    }

    @Override
    public void setMainWindowStage(Stage stage) {

        stage.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            Map<KeyCombination, GlobalFilterShortcutInfo> filters = keyCombinationRepository.getGlobalFilters();

            for (var entry : filters.entrySet()) {
                if (entry.getKey().match(event) && !isDisabled(entry.getValue())) {
                    entry.getValue().handler.onShortcutExecuted(new ShortcutExecutedEvent(new KeyCodeCombination(event.getCode())));
                    event.consume();
                    break;
                }
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
