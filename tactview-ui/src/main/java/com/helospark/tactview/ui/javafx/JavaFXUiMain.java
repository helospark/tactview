package com.helospark.tactview.ui.javafx;

import static java.awt.image.BufferedImage.TYPE_INT_BGR;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.commons.lang3.SystemUtils;
import org.controlsfx.control.NotificationPane;

import com.helospark.lightdi.LightDiContext;
import com.helospark.lightdi.LightDiContextConfiguration;
import com.helospark.lightdi.properties.Environment;
import com.helospark.lightdi.properties.PropertySourceHolder;
import com.helospark.tactview.core.init.PostInitializationArgsCallback;
import com.helospark.tactview.core.plugin.PluginMainClassProviders;
import com.helospark.tactview.core.save.DirtyRepository;
import com.helospark.tactview.core.util.jpaplugin.JnaLightDiPlugin;
import com.helospark.tactview.ui.javafx.aware.MainWindowStageAware;
import com.helospark.tactview.ui.javafx.inputmode.InputModeRepository;
import com.helospark.tactview.ui.javafx.layout.DefaultLayoutProvider;
import com.helospark.tactview.ui.javafx.menu.MenuProcessor;
import com.helospark.tactview.ui.javafx.menu.defaultmenus.projectsize.ProjectSizeInitializer;
import com.helospark.tactview.ui.javafx.render.RenderDialogOpener;
import com.helospark.tactview.ui.javafx.repository.UiProjectRepository;
import com.helospark.tactview.ui.javafx.save.ExitWithSaveService;
import com.helospark.tactview.ui.javafx.scenepostprocessor.ScenePostProcessor;
import com.helospark.tactview.ui.javafx.stylesheet.StylesheetAdderService;
import com.helospark.tactview.ui.javafx.tabs.dockabletab.DockableTabRepository;
import com.helospark.tactview.ui.javafx.tabs.dockabletab.impl.PreviewDockableTabFactory;
import com.helospark.tactview.ui.javafx.tiwulfx.com.panemu.tiwulfx.common.TiwulFXUtil;
import com.helospark.tactview.ui.javafx.tiwulfx.com.panemu.tiwulfx.control.DetachableTabPane;
import com.helospark.tactview.ui.javafx.tiwulfx.com.panemu.tiwulfx.control.DetachableTabPaneLoadModel;
import com.helospark.tactview.ui.javafx.uicomponents.DefaultCanvasTranslateSetter;
import com.helospark.tactview.ui.javafx.uicomponents.PropertyView;
import com.helospark.tactview.ui.javafx.uicomponents.UiTimeline;
import com.helospark.tactview.ui.javafx.uicomponents.VideoStatusBarUpdater;

import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.MenuBar;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.PopupWindow.AnchorLocation;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

public class JavaFXUiMain extends Application {
    private static String[] mainArgs = new String[0];

    public static Stage STAGE = null;

    static LightDiContext lightDi;

    private Stage splashStage;
    private ImageView splasViewh;

    static UiTimelineManager uiTimelineManager;
    public static Canvas canvas;
    static UiTimeline uiTimeline;
    static UiProjectRepository uiProjectRepository;
    static PropertyView effectPropertyView;
    static RenderDialogOpener renderService;
    static DisplayUpdaterService displayUpdateService;
    static ProjectSizeInitializer projectSizeInitializer;

    @Override
    public void start(Stage stage) throws IOException {
        DirtyRepository dirtyRepository = lightDi.getBean(DirtyRepository.class);
        ExitWithSaveService exitWithSaveService = lightDi.getBean(ExitWithSaveService.class);
        StylesheetAdderService styleSheetAdder = lightDi.getBean(StylesheetAdderService.class);

        styleSheetAdder.setTactviewIconForStage(stage);

        JavaFXUiMain.STAGE = stage;

        lightDi.getListOfBeans(MainWindowStageAware.class)
                .forEach(listener -> listener.setMainWindowStage(stage));

        NotificationPane notificationPane = new NotificationPane();
        BorderPane root = new BorderPane();
        Scene scene = new Scene(root, 650, 550, Color.GREY);

        styleSheetAdder.addStyleSheets(root, "stylesheet.css");

        MenuBar menuBar = lightDi.getBean(MenuProcessor.class).createMenuBar();
        if (SystemUtils.IS_OS_MAC_OSX) {
            // https://stackoverflow.com/a/28874063
            menuBar.useSystemMenuBarProperty().set(true);
        }

        stage.setOnCloseRequest(e -> {
            exitApplication(exitWithSaveService, e);
        });

        root.setTop(menuBar);
        stage.setScene(scene);
        stage.setTitle("TactView - Video editor");

        dirtyRepository.addUiChangeListener(value -> {
            Platform.runLater(() -> {
                String title = "";
                if (value) {
                    title += "* ";
                }
                title += "TactView - Video editor";
                stage.setTitle(title);
            });
        });
        stage.setMaximized(true);

        if (SystemUtils.IS_OS_MAC) {
            SwingFXUtils.toFXImage(new BufferedImage(100, 100, TYPE_INT_BGR), null);
        }

        SplitPane mainContentPane = new SplitPane(); // spacing between child nodes only.
        mainContentPane.setId("content-area");
        mainContentPane.setPrefWidth(scene.getWidth());
        mainContentPane.setPadding(new Insets(1)); // space between vbox border and child nodes column
        mainContentPane.setDividerPositions(0.6);

        canvas = new Canvas();
        lightDi.getBean(CanvasStateHolder.class).setCanvas(canvas);
        lightDi.getBean(DefaultCanvasTranslateSetter.class).setDefaultCanvasTranslate(uiProjectRepository.getPreviewWidth(), uiProjectRepository.getPreviewHeight());
        InputModeRepository inputModeRepository = lightDi.getBean(InputModeRepository.class);
        inputModeRepository.setCanvas(canvas);
        displayUpdateService.setCanvas(canvas);
        displayUpdateService.updateCurrentPositionWithInvalidatedCache();

        Tooltip tooltip = new Tooltip();

        StringProperty statusTextProperty = lightDi.getBean(VideoStatusBarUpdater.class).getTextProperty();
        tooltip.textProperty().bind(statusTextProperty);
        tooltip.setHideOnEscape(false);
        tooltip.setAutoHide(false);
        tooltip.setAnchorLocation(AnchorLocation.CONTENT_TOP_LEFT);

        statusTextProperty.addListener((e, oldV, newV) -> {
            if (newV.length() > 0) {
                Bounds canvasBottom = canvas.localToScene(canvas.getBoundsInLocal());
                double x = canvasBottom.getMinX();
                double y = canvasBottom.getMaxY() + 60;
                tooltip.show(canvas, x, y);
            } else {
                tooltip.hide();
            }
        });
        tooltip.setWrapText(true);

        HBox upperPane = new HBox();
        upperPane.setId("upper-content-area");
        upperPane.setMinHeight(300);
        upperPane.setFillHeight(true);

        DetachableTabPaneLoadModel layoutToLoad = lightDi.getBean(DefaultLayoutProvider.class).provideDefaultLayout();
        DockableTabRepository dockableTabRepository = lightDi.getBean(DockableTabRepository.class);
        dockableTabRepository.setParentPane(upperPane);
        dockableTabRepository.loadAndSetModelToParent(layoutToLoad);

        TiwulFXUtil.setTiwulFXStyleSheet(scene);

        VBox lower = new VBox(5);
        lower.setPrefWidth(scene.getWidth());
        lower.setPrefHeight(300);
        lower.setId("timeline-view");

        BorderPane timeline = uiTimeline.createTimeline(lower, root);
        lower.getChildren().add(timeline);
        VBox.setVgrow(timeline, Priority.ALWAYS);

        mainContentPane.getItems().add(upperPane);
        mainContentPane.getItems().add(lower);
        mainContentPane.setOrientation(Orientation.VERTICAL);

        root.setCenter(mainContentPane);
        notificationPane.setContent(root);

        inputModeRepository.registerInputModeChangeConsumerr(onClassChangeDisableTabs());
        inputModeRepository.registerInputModeChangeConsumerr(onClassChange(timeline));

        lightDi.getListOfBeans(ScenePostProcessor.class)
                .stream()
                .forEach(processor -> processor.postProcess(scene));

        lightDi.getListOfBeans(PostInitializationArgsCallback.class)
                .forEach(postInitCallback -> postInitCallback.call(mainArgs));

        if (splashStage.isShowing()) {
            stage.show();
            splashStage.toFront();
            FadeTransition fadeSplash = new FadeTransition(Duration.seconds(0.5), splasViewh);
            fadeSplash.setDelay(Duration.millis(800));
            fadeSplash.setFromValue(1.0);
            fadeSplash.setToValue(0.0);
            fadeSplash.setOnFinished(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    splashStage.hide();
                }
            });
            fadeSplash.play();
        }
    }

    private void showSplash(Stage splashStage, ImageView splash) {
        StackPane splashLayout = new StackPane();
        splashLayout.setStyle("-fx-background-color: transparent;");
        splashLayout.getChildren().add(splash);
        Scene splashScene = new Scene(splashLayout, 690, 590);
        splashScene.setFill(Color.TRANSPARENT);
        splashStage.setScene(splashScene);
        splashStage.show();
    }

    private void exitApplication(ExitWithSaveService exitWithSaveService, WindowEvent event) {
        boolean exitPerformed = exitWithSaveService.optionallySaveAndThenRun(() -> {
            Platform.exit();
            lightDi.close();
            System.exit(0);
        });
        if (!exitPerformed) {
            event.consume();
        }
    }

    private static Consumer<Boolean> onClassChangeDisableTabs() {
        return enabled -> {
            List<DetachableTabPane> tabRepos = lightDi.getBean(DockableTabRepository.class).findNodesNotContainingId(PreviewDockableTabFactory.ID);
            for (var element : tabRepos) {
                if (enabled) {
                    element.getStyleClass().add("input-mode-enabled");
                } else {
                    element.getStyleClass().remove("input-mode-enabled");
                }
            }
        };
    }

    private static Consumer<Boolean> onClassChange(Node element) {
        return enabled -> {
            if (enabled) {
                element.getStyleClass().add("input-mode-enabled");
            } else {
                element.getStyleClass().remove("input-mode-enabled");
            }
        };
    }

    @Override
    public void init() throws Exception {
        super.init();

        Platform.runLater(() -> {
            splashStage = new Stage(StageStyle.DECORATED);
            splashStage.setTitle("Tactview starting...");
            StylesheetAdderService.setTactviewIconForStageStatic(splashStage);

            splasViewh = new ImageView(new Image(getClass().getResource("/tactview-splash.png").toString()));

            splashStage.initStyle(StageStyle.TRANSPARENT);
            showSplash(splashStage, splasViewh);
        });

        LightDiContextConfiguration configuration = LightDiContextConfiguration.builder()
                .withThreadNumber(4)
                .withCheckForIntegrity(true)
                .withAdditionalDependencies(Collections.singletonList(new JnaLightDiPlugin()))
                .withUseClasspathFile(false)
                .build();
        List<Class<?>> allClasses = new ArrayList<>();
        allClasses.add(MainApplicationConfiguration.class);
        allClasses.addAll(PluginMainClassProviders.getPluginClasses());
        lightDi = new LightDiContext(configuration);
        lightDi.addPropertySource(createInitialPropertySource());
        lightDi.loadDependencies(List.of(), allClasses);

        uiTimeline = lightDi.getBean(UiTimeline.class);
        uiTimelineManager = lightDi.getBean(UiTimelineManager.class);
        effectPropertyView = lightDi.getBean(PropertyView.class);

        uiTimelineManager.registerUiPlaybackConsumer(position -> uiTimeline.updateLine(position));
        uiTimelineManager.registerUiPlaybackConsumer(position -> effectPropertyView.updateValues(position));

        displayUpdateService = lightDi.getBean(DisplayUpdaterService.class);
        projectSizeInitializer = lightDi.getBean(ProjectSizeInitializer.class);
        uiTimelineManager.setDisplayUpdaterService(lightDi.getBean(DisplayUpdaterService.class));

        uiProjectRepository = lightDi.getBean(UiProjectRepository.class);
        renderService = lightDi.getBean(RenderDialogOpener.class);
        lightDi.eagerInitAllBeans();
    }

    private PropertySourceHolder createInitialPropertySource() {
        Map<String, String> propertyMap = new HashMap<>();
        try {
            propertyMap.put("tactview.installation.folder", new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI()).getParent());
        } catch (Throwable t) {
            System.out.println("Cannot determine installation location");
            t.printStackTrace();
        }

        return new PropertySourceHolder(Environment.ENVIRONMENT_PROPERTY_ORDER + 1, propertyMap);
    }

    // Do NOT run this one. Run the one in {@link application.HackyMain}!
    public static void main(String[] args) {
        JavaFXUiMain.mainArgs = args; // Since Javafx init does not give access to this
        launch(args);
    }

    public void launchUi() {
        launch();
    }

}