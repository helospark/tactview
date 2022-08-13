package com.helospark.tactview.ui.javafx.script;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.helospark.tactview.core.timeline.effect.interpolation.KeyframeableEffect;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Color;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.DoubleRange;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.InterpolationLine;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Polygon;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Rectangle;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.evaluator.EvaluationContext;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.evaluator.script.JavascriptExpressionEvaluator;
import com.helospark.tactview.ui.javafx.DisplayUpdateRequestMessage;
import com.helospark.tactview.ui.javafx.GlobalTimelinePositionHolder;
import com.helospark.tactview.ui.javafx.UiMessagingService;
import com.helospark.tactview.ui.javafx.repository.NameToIdRepository;
import com.helospark.tactview.ui.javafx.stylesheet.StylesheetAdderService;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class JavascriptEditor {
    private CachedFileContentReader cachedFileContentReader;
    private ScriptVariablesStore scriptVariablesStore;
    private JavascriptExpressionEvaluator javascriptExpressionEvaluator;
    private GlobalTimelinePositionHolder globalTimelinePositionHolder;
    private NameToIdRepository nameToIdRepository;
    private UiMessagingService messagingService;

    private Stage stage;
    private TextArea textArea;
    boolean hasResult = true;
    Button testButton;
    Label descriptionLabel;
    Label exceptionLabel;
    String originalExpression;
    KeyframeableEffect<?> provider;

    public JavascriptEditor(StylesheetAdderService stylesheetAdderService, CachedFileContentReader cachedFileContentReader,
            ScriptVariablesStore scriptVariablesStore, JavascriptExpressionEvaluator javascriptExpressionEvaluator,
            GlobalTimelinePositionHolder globalTimelinePositionHolder, NameToIdRepository nameToIdRepository,
            UiMessagingService messagingService) {
        this.cachedFileContentReader = cachedFileContentReader;
        this.scriptVariablesStore = scriptVariablesStore;
        this.javascriptExpressionEvaluator = javascriptExpressionEvaluator;
        this.globalTimelinePositionHolder = globalTimelinePositionHolder;
        this.nameToIdRepository = nameToIdRepository;
        this.messagingService = messagingService;

        BorderPane root = new BorderPane();
        root.getStyleClass().add("dialog-root");

        Scene dialog = new Scene(root);
        stage = new Stage();
        stylesheetAdderService.styleDialog(stage, root, "stylesheet.css");
        stage.setTitle("Script editor");
        stage.setScene(dialog);

        dialog.setOnKeyPressed(e -> {
            if (e.getCode().equals(KeyCode.ESCAPE)) {
                stage.close();
            }
        });

        textArea = new TextArea();
        textArea.setWrapText(true);

        root.setCenter(textArea);

        ButtonBar buttonBar = new ButtonBar();
        Button saveAndClose = new Button("Save and close");
        saveAndClose.setOnAction(e -> {
            provider.setExpression(originalExpression); // will be set elsewhere
            stage.close();
        });
        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> {
            provider.setExpression(originalExpression);
            hasResult = false;
            stage.close();
        });
        Button testButton = new Button("Test");
        testButton.setOnAction(e -> {
            provider.setExpression(textArea.getText()); // temporarily set
            messagingService.sendAsyncMessage(new DisplayUpdateRequestMessage(true));
        });
        stage.setOnCloseRequest(e -> {
            provider.setExpression(originalExpression);
            hasResult = false;
        });
        buttonBar.getButtons().add(saveAndClose);
        buttonBar.getButtons().add(cancelButton);
        buttonBar.getButtons().add(testButton);

        ButtonBar.setButtonData(saveAndClose, ButtonData.RIGHT);
        ButtonBar.setButtonData(cancelButton, ButtonData.LEFT);
        ButtonBar.setButtonData(testButton, ButtonData.LEFT);

        root.setBottom(buttonBar);

        VBox topBox = new VBox();
        descriptionLabel = new Label();
        descriptionLabel.getStyleClass().add("javascript-editor-description");

        exceptionLabel = new Label();
        exceptionLabel.getStyleClass().add("javascript-exception");

        topBox.getChildren().add(descriptionLabel);
        topBox.getChildren().add(exceptionLabel);
        root.setTop(topBox);
    }

    public Optional<String> show(Class<?> type, KeyframeableEffect<?> provider) {
        this.provider = provider;
        originalExpression = provider.getExpression();
        hasResult = true;
        textArea.setText("");
        if (originalExpression != null) {
            textArea.appendText(originalExpression);
        }

        updateExceptionLabelForScript(originalExpression);

        String description = "Write Javascript that returns a " + type.getSimpleName() + "\n";
        description += "Either write a single expression or set a variable with name 'result'\n";
        description += "Use context menu to insert snippet and variable references\n";
        descriptionLabel.setText(description);

        appendContextMenu(type);

        stage.showAndWait();

        if (!hasResult || textArea.getText().isBlank()) {
            return Optional.empty();
        } else {
            return Optional.of(textArea.getText());
        }

    }

    private void updateExceptionLabelForScript(String initialData) {
        EvaluationContext evaluationContext = scriptVariablesStore.getEvaluationContext();

        if (evaluationContext != null) {
            String exceptionForScript = evaluationContext.getExceptions().get(initialData);
            exceptionLabel.setText(exceptionForScript);
        } else {
            exceptionLabel.setText("");
        }
    }

    private void appendContextMenu(Class<?> type) {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem insertSnippet = createInsertCodeSnippetMenu(type);
        contextMenu.getItems().add(insertSnippet);
        MenuItem insertReference = createInsertReferenceMenu();
        contextMenu.getItems().add(insertReference);

        textArea.setContextMenu(contextMenu);
    }

    private MenuItem createInsertCodeSnippetMenu(Class<?> type) {
        Menu snippetMenu = new Menu("Insert snippet");

        MenuItem ifMenu = new MenuItem("if");
        ifMenu.setOnAction(e -> {

            textArea.insertText(textArea.getCaretPosition(), readFileAndReplacePlaceholders("classpath:/script_samples/if.js", type) + "\n");
        });
        MenuItem expressionMenu = new MenuItem("single expression");
        expressionMenu.setOnAction(e -> {
            textArea.insertText(textArea.getCaretPosition(), readFileAndReplacePlaceholders("classpath:/script_samples/single_expression.js", type) + "\n");
        });

        snippetMenu.getItems().add(ifMenu);
        snippetMenu.getItems().add(expressionMenu);

        List<Class<?>> supportedTypes = List.of(String.class, Double.class, Boolean.class, Color.class, InterpolationLine.class, Rectangle.class, DoubleRange.class, Polygon.class);

        for (var clazz : supportedTypes) {
            MenuItem typeMenu = new MenuItem("type " + clazz.getSimpleName());
            typeMenu.setOnAction(e -> {
                textArea.insertText(textArea.getCaretPosition(), readFileAndReplacePlaceholders("classpath:/script_samples/result_expression.js", clazz));
            });
            snippetMenu.getItems().add(typeMenu);
        }

        return snippetMenu;
    }

    private String readFileAndReplacePlaceholders(String fileName, Class<?> type) {
        String result = cachedFileContentReader.readFile(fileName);

        List<String> defaultValues = getDefaultValuesPerType(type);
        Map<String, String> placeholders = Map.of("{{EXAMPLE_1}}", defaultValues.get(0),
                "{{EXAMPLE_2}}", defaultValues.get(1));

        for (var entry : placeholders.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }

        return result;
    }

    // clips['clipId'].rectangle.x
    private MenuItem createInsertReferenceMenu() {
        Menu referenceMenu = new Menu("Insert reference");

        EvaluationContext evaluationContext = scriptVariablesStore.getEvaluationContext();
        if (evaluationContext != null) {
            Map<String, Map<String, Object>> variableMap = javascriptExpressionEvaluator.getVariables(globalTimelinePositionHolder.getCurrentPosition(), evaluationContext);

            for (var entry : variableMap.entrySet()) {
                Menu menu = new Menu(entry.getKey());
                referenceMenu.getItems().add(menu);

                List<MenuItem> menus = createMenuItemsRecursively(1, entry.getKey(), entry.getValue());
                for (var child : menus) {
                    menu.getItems().add(child);
                }
            }
        }

        return referenceMenu;
    }

    // path will be like: clips['clipId'].rectangle.x
    private List<MenuItem> createMenuItemsRecursively(int depth, String path, Map<String, Object> map) {
        List<MenuItem> result = new ArrayList<>();
        for (var entry : map.entrySet()) {
            String currentPath = path;
            String menuName = entry.getKey();
            if (depth == 0) {
                currentPath = menuName;
            } else if (depth == 1) {
                currentPath += "['" + menuName + "']";
            } else {
                currentPath += "." + menuName;
            }
            String finalPath = currentPath;
            if (entry.getValue() instanceof Map) {
                Menu menu = new Menu(replaceIdWithName(menuName));
                if (depth > 1) {
                    menu.setOnAction(e -> {
                        if (e.getTarget().equals(menu)) { // child menu items also trigger this
                            textArea.insertText(textArea.getCaretPosition(), finalPath);
                        }
                    });
                }

                List<MenuItem> childItems = createMenuItemsRecursively(depth + 1, finalPath, (Map) entry.getValue());

                for (var childItem : childItems) {
                    menu.getItems().add(childItem);
                }

                result.add(menu);
            } else {
                MenuItem menuItem = new MenuItem(menuName + " (" + entry.getValue().getClass().getSimpleName() + ")");
                menuItem.setMnemonicParsing(false);
                menuItem.setOnAction(e -> {
                    textArea.insertText(textArea.getCaretPosition(), finalPath);
                });
                result.add(menuItem);
            }
        }
        return result;
    }

    private String replaceIdWithName(String menuName) {
        for (var entry : nameToIdRepository.getIdToNameMap().entrySet()) {
            menuName = menuName.replace(entry.getKey(), entry.getValue());
        }
        return menuName;
    }

    public List<String> getDefaultValuesPerType(Class<?> type) {
        if (type.equals(String.class)) {
            return List.of("'test1'", "'test2'");
        } else if (Number.class.isAssignableFrom(type)) {
            return List.of("1", "2");
        } else if (type.equals(Boolean.class)) {
            return List.of("true", "false");
        } else if (type.equals(Point.class)) {
            return List.of("{x: 0.1, y: 0.2}", "{x: 0.3, y: 0.4}");
        } else if (type.equals(InterpolationLine.class)) {
            return List.of("{x1: 0.1, y1: 0.2, x2: 0.3, y2: 0.4}", "{x1: 0.5, y1: 0.6, x2: 0.7, y2: 0.8}");
        } else if (type.equals(Color.class)) {
            return List.of("{r: 0.1, g: 0.2, b: 0.3}", "{r: 0.1, g: 0.2, b: 0.3}");
        } else if (type.equals(Rectangle.class)) {
            return List.of("{x1: 0.1, y1: 0.2,\n x2: 0.3, y2: 0.2\nx3: 0.3, y3: 0.4\nx4: 0.1, y4: 0.4\n}",
                    "{x1: 0.2, y1: 0.4,\n x2: 0.6, y2: 0.4\nx3: 0.6, y3: 0.4\nx4: 0.2, y4: 0.8\n}");
        } else if (type.equals(DoubleRange.class)) {
            return List.of("{low: 0.1, high: 0.2}",
                    "{low: 0.5, high: 0.8}");
        } else if (type.equals(Polygon.class)) {
            return List.of("{\n"
                    + "  numberOfPoints: 3,\n"
                    + "  points: [\n"
                    + "    {x: 0.5, y: 0.2},\n"
                    + "    {x: 0.3, y: 0.5},\n"
                    + "    {x: 0.8, y: 0.5}\n"
                    + "  ]\n"
                    + "}",
                    "{\n"
                            + "  numberOfPoints: 3,\n"
                            + "  points: [\n"
                            + "    {x: 0.5, y: 0.2},\n"
                            + "    {x: 0.3, y: 0.5},\n"
                            + "    {x: 0.8, y: 0.5}\n"
                            + "  ]\n"
                            + "}");
        } else {
            return List.of("{...}", "{...}");
        }
    }

    public void onDisplayUpdated() {
        updateExceptionLabelForScript(textArea.getText());
    }

}
