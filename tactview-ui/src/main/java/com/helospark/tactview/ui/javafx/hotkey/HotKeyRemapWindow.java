package com.helospark.tactview.ui.javafx.hotkey;

import static javafx.stage.Modality.APPLICATION_MODAL;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import com.helospark.tactview.ui.javafx.plugin.RestartDialogOpener;
import com.helospark.tactview.ui.javafx.stylesheet.StylesheetAdderService;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyCombination.Modifier;
import javafx.scene.input.KeyCombination.ModifierValue;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class HotKeyRemapWindow {
    private static final KeyCode NO_KEYCODE_DEFINED = KeyCode.F24;
    private static final int DIALOG_WIDTH = 500;
    private static final Map<KeyCode, Modifier> KEY_CODE_TO_MODIFIER = Map.of(
            KeyCode.CONTROL, KeyCombination.CONTROL_DOWN,
            KeyCode.ALT, KeyCombination.ALT_DOWN,
            KeyCode.SHIFT, KeyCombination.SHIFT_DOWN,
            KeyCode.SHORTCUT, KeyCombination.SHIFT_DOWN);

    private HotKeyRepository hotKeyRepository;
    private StylesheetAdderService stylesheetAdderService;
    private RestartDialogOpener restartDialogOpener;

    private Stage stage;
    private BorderPane pane;

    private Optional<String> editingId = Optional.empty();
    private Set<KeyCode> pressedKeys = new HashSet<>();

    public HotKeyRemapWindow(HotKeyRepository hotKeyRepository, StylesheetAdderService stylesheetAdderService, RestartDialogOpener restartDialogOpener) {
        this.hotKeyRepository = hotKeyRepository;
        this.stylesheetAdderService = stylesheetAdderService;
        this.restartDialogOpener = restartDialogOpener;
    }

    public Scene createScene() {
        pane = new BorderPane();
        pane.getStyleClass().add("dialog-root");

        pane.addEventFilter(KeyEvent.KEY_RELEASED, e -> {
            KeyCode keycode = e.getCode();
            pressedKeys.remove(keycode);
        });

        pane.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            pressedKeys.add(event.getCode());
            if (editingId.isPresent()) {

                System.out.println(event.getCode());

                if (KEY_CODE_TO_MODIFIER.keySet().contains(event.getCode())) {
                    reloadContent();
                    return;
                }

                if (event.getCode().equals(KeyCode.ESCAPE)) {
                    editingId = Optional.empty();
                    reloadContent();
                    return;
                }

                List<Modifier> modifiers = new ArrayList<>();
                if (event.isAltDown()) {
                    modifiers.add(KeyCombination.ALT_DOWN);
                }
                if (event.isControlDown()) {
                    modifiers.add(KeyCombination.CONTROL_DOWN);
                }
                if (event.isShiftDown()) {
                    modifiers.add(KeyCombination.SHIFT_DOWN);
                }
                if (event.isShortcutDown()) {
                    modifiers.add(KeyCombination.SHORTCUT_DOWN);
                }
                KeyCodeCombination combination = new KeyCodeCombination(event.getCode(), modifiers.toArray(new Modifier[0]));

                hotKeyRepository.changeHotKeyForId(editingId.get(), combination);
                editingId = Optional.empty();
                reloadContent();
            }
        });

        reloadContent();
        pane.setBottom(createButtonBar());
        return new Scene(pane, DIALOG_WIDTH, 500);
    }

    private void reloadContent() {
        VBox vbox = new VBox();
        vbox.setPrefWidth(DIALOG_WIDTH);

        for (var entry : hotKeyRepository.getKeyDescriptors().entrySet()) {
            GridPane subGridPane = new GridPane();
            subGridPane.prefWidthProperty().bind(vbox.widthProperty());
            subGridPane.getStyleClass().add("hot-key-grid");

            ColumnConstraints col1 = new ColumnConstraints();
            col1.setPercentWidth(50);
            ColumnConstraints col2 = new ColumnConstraints();
            col2.setPercentWidth(30);
            ColumnConstraints col3 = new ColumnConstraints();
            col3.setPercentWidth(20);

            subGridPane.getColumnConstraints().addAll(col1, col2, col3);
            subGridPane.add(new Label(entry.getValue().getName()), 0, 0);
            subGridPane.add(getCombination(entry), 1, 0);
            Button changeButton = new Button("Change");

            changeButton.setOnAction(a -> {
                editingId = Optional.ofNullable(entry.getKey());
            });

            subGridPane.add(changeButton, 2, 0);

            vbox.getChildren().add(subGridPane);
        }

        ScrollPane scrollPane = new ScrollPane(vbox);
        scrollPane.setHbarPolicy(ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollBarPolicy.ALWAYS);
        scrollPane.prefWidthProperty().bind(pane.widthProperty());
        pane.setCenter(scrollPane);
    }

    private Node getCombination(Entry<String, KeyDescriptor> entry) {
        if (editingId.map(id -> id.equals(entry.getKey())).orElse(false)) {
            List<Modifier> modifiers = new ArrayList<>();
            for (var keyEntry : KEY_CODE_TO_MODIFIER.entrySet()) {
                if (pressedKeys.contains(keyEntry.getKey())) {
                    modifiers.add(keyEntry.getValue());
                }
            }
            return getCombinationNode(new KeyCodeCombination(NO_KEYCODE_DEFINED, modifiers.toArray(new Modifier[0])));
        } else {
            return getCombinationNode(entry.getValue().getCombination());
        }
    }

    private HBox createButtonBar() {
        HBox buttonBar = new HBox();

        Button closeButton = new Button("Close without save");
        closeButton.setOnAction(e -> stage.close());

        Button saveButton = new Button("Save & close");
        saveButton.setOnAction(e -> {
            hotKeyRepository.saveHotkeys();
            stage.close();
        });

        Button saveAndRestartButton = new Button("Save and restart");
        saveAndRestartButton.setOnAction(e -> {
            hotKeyRepository.saveHotkeys();
            restartDialogOpener.confirmRestart("Hotkeys saved, restart to take effect");
        });

        buttonBar.getChildren().addAll(closeButton, saveButton, saveAndRestartButton);

        return buttonBar;
    }

    private Node getCombinationNode(KeyCodeCombination combination) {
        HBox hbox = new HBox();

        if (combination.getShift().equals(ModifierValue.DOWN)) {
            hbox.getChildren().add(createKeyNode("shift"));
        }
        if (combination.getControl().equals(ModifierValue.DOWN)) {
            hbox.getChildren().add(createKeyNode("ctrl"));
        }
        if (combination.getAlt().equals(ModifierValue.DOWN)) {
            hbox.getChildren().add(createKeyNode("alt"));
        }
        if (!combination.getCode().equals(NO_KEYCODE_DEFINED)) {
            hbox.getChildren().add(createKeyNode(combination.getCode().getName()));
        }

        int elementSize = hbox.getChildren().size();
        for (int i = elementSize - 1; i > 0; --i) {
            Label plusLabel = new Label("+");
            plusLabel.getStyleClass().add("key-code-container-plus");
            hbox.getChildren().add(i, plusLabel);
        }

        return hbox;
    }

    private Node createKeyNode(String string) {
        Label text = new Label(string);
        text.getStyleClass().add("key-code-text");
        HBox box = new HBox(text);
        box.getStyleClass().add("key-code-container");
        return box;
    }

    public void open(Scene parentScene) {
        stage = new Stage();
        stage.initOwner(parentScene.getWindow());
        stage.setScene(createScene());
        stage.setResizable(false);
        stylesheetAdderService.setDefaultStyleSheetForDialog(stage, pane);
        stage.initModality(APPLICATION_MODAL);
        stage.show();
    }

}
