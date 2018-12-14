package com.helospark.tactview.ui.javafx.scenepostprocessor;

import static javafx.scene.input.KeyCode.DELETE;
import static javafx.scene.input.KeyCode.ESCAPE;
import static javafx.scene.input.KeyCode.K;
import static javafx.scene.input.KeyCode.LEFT;
import static javafx.scene.input.KeyCode.Z;
import static javafx.scene.input.KeyCombination.CONTROL_DOWN;
import static javafx.scene.input.KeyCombination.SHIFT_DOWN;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.helospark.lightdi.LightDiContext;
import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.aware.ContextAware;
import com.helospark.tactview.core.timeline.TimelineManager;
import com.helospark.tactview.core.util.StaticObjectMapper;
import com.helospark.tactview.ui.javafx.RemoveClipService;
import com.helospark.tactview.ui.javafx.RemoveEffectService;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.UiTimelineManager;
import com.helospark.tactview.ui.javafx.key.GlobalShortcutHandler;
import com.helospark.tactview.ui.javafx.key.KeyCombinationRepository;
import com.helospark.tactview.ui.javafx.key.StandardGlobalShortcutHandler;
import com.helospark.tactview.ui.javafx.repository.CleanableMode;
import com.helospark.tactview.ui.javafx.repository.CopyPasteRepository;
import com.helospark.tactview.ui.javafx.repository.SelectedNodeRepository;
import com.helospark.tactview.ui.javafx.uicomponents.ClipCutService;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination.Modifier;
import javafx.scene.input.KeyEvent;

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
    private TimelineManager timelineManager;

    public GlobalKeyCombinationAttacher(UiCommandInterpreterService commandInterpreter, KeyCombinationRepository keyCombinationRepository, SelectedNodeRepository selectedNodeRepository,
            RemoveClipService removeClipService,
            RemoveEffectService removeEffectService, ClipCutService clipCutService,
            CopyPasteRepository copyPasteRepository,
            UiTimelineManager uiTimelineManager,
            TimelineManager timelineManager) {
        this.commandInterpreter = commandInterpreter;
        this.keyCombinationRepository = keyCombinationRepository;
        this.selectedNodeRepository = selectedNodeRepository;
        this.removeClipService = removeClipService;
        this.removeEffectService = removeEffectService;
        this.clipCutService = clipCutService;
        this.uiTimelineManager = uiTimelineManager;
        this.timelineManager = timelineManager;
        this.copyPasteRepository = copyPasteRepository;
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
        keyCombinationRepository.registerKeyCombination(on(CONTROL_DOWN, KeyCode.S),
                useHandler("Undo", event -> {
                    try {
                        Map<String, Object> result = new LinkedHashMap<>();

                        timelineManager.generateSavedContent(result);

                        ObjectMapper mapper = StaticObjectMapper.objectMapper;
                        mapper.enable(SerializationFeature.INDENT_OUTPUT);
                        String safdsg = mapper.writeValueAsString(result);
                        File file = new File("/tmp/" + System.currentTimeMillis() + ".json");
                        new FileOutputStream(file).write(safdsg.getBytes());
                    } catch (FileNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }));
        keyCombinationRepository.registerKeyCombination(on(CONTROL_DOWN, KeyCode.L),
                useHandler("Undo", event -> {
                    try {
                        ObjectMapper mapper = StaticObjectMapper.objectMapper;

                        TypeReference<LinkedHashMap<String, Object>> typeRef = new TypeReference<LinkedHashMap<String, Object>>() {
                        };

                        String content = new String(Files.readAllBytes(Paths.get("/tmp/1544795549407.json")), StandardCharsets.UTF_8);

                        JsonNode tree = mapper.readTree(content);

                        timelineManager.loadFrom(tree);

                    } catch (FileNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }));
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
        keyCombinationRepository.registerKeyCombination(on(CONTROL_DOWN, KeyCode.C),
                useHandler("Copy", event -> {
                    List<String> selectedClipIds = selectedNodeRepository.getSelectedClipIds();
                    if (selectedClipIds.size() > 0) { // copy ony the first for now
                        String selectedClipId = selectedClipIds.get(0);
                        copyPasteRepository.copyClip(selectedClipId);
                    } else {
                        List<String> selectedEffects = selectedNodeRepository.getSelectedEffectIds();
                        if (selectedEffects.size() > 0) {
                            String selectedEffectId = selectedEffects.get(0);
                            copyPasteRepository.copyEffect(selectedEffects.get(0));
                        }
                    }
                }));
        keyCombinationRepository.registerKeyCombination(on(CONTROL_DOWN, KeyCode.V),
                useHandler("Copy", event -> {
                    List<String> selectedClipIds = selectedNodeRepository.getSelectedClipIds();
                    if (selectedClipIds.isEmpty()) {
                        copyPasteRepository.pasteWithoutAdditionalInfo();
                    } else {
                        copyPasteRepository.pasteOnExistingEffect(selectedClipIds.get(0));
                    }
                }));
    }

    private Map<String, Object> processMap(Map<String, Object> result) {
        try {
            return whoMadeCheckedExceptionsssss(result);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, Object> whoMadeCheckedExceptionsssss(Map<String, Object> result) throws InstantiationException, IllegalAccessException {
        int i = 0;
        for (i = 0; i < 1000; ++i) {
            boolean hasChange = false;
            Map<String, Object> tempMap = new LinkedHashMap<>();
            for (var entry : result.entrySet()) {

            }
            result.clear();
            result.putAll(tempMap);
            tempMap.clear();
            if (!hasChange) {
                break;
            }
        }
        if (i >= 1000) {
            throw new RuntimeException("Infinite loop?");
        }
        return result;
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

    @Override
    public void setContext(LightDiContext context) {
        this.context = context;
    }

}
