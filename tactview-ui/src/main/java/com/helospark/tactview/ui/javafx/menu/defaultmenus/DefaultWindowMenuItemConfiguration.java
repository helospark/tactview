package com.helospark.tactview.ui.javafx.menu.defaultmenus;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.helospark.lightdi.annotation.Bean;
import com.helospark.lightdi.annotation.Configuration;
import com.helospark.lightdi.annotation.Order;
import com.helospark.lightdi.annotation.Value;
import com.helospark.tactview.core.util.messaging.MessagingService;
import com.helospark.tactview.ui.javafx.JavaFXUiMain;
import com.helospark.tactview.ui.javafx.layout.DefaultLayoutProvider;
import com.helospark.tactview.ui.javafx.menu.DefaultMenuContribution;
import com.helospark.tactview.ui.javafx.menu.SelectableMenuContribution;
import com.helospark.tactview.ui.javafx.menu.SeparatorMenuContribution;
import com.helospark.tactview.ui.javafx.save.QuerySaveFilenameService;
import com.helospark.tactview.ui.javafx.save.QuerySaveFilenameService.QuerySaveFileNameRequest;
import com.helospark.tactview.ui.javafx.stylesheet.AlertDialogFactory;
import com.helospark.tactview.ui.javafx.tabs.dockabletab.DockableTabFromIdFactory;
import com.helospark.tactview.ui.javafx.tabs.dockabletab.DockableTabRepository;
import com.helospark.tactview.ui.javafx.tiwulfx.com.panemu.tiwulfx.control.DetachableTab;
import com.helospark.tactview.ui.javafx.tiwulfx.com.panemu.tiwulfx.control.DetachableTabPaneLoadModel;

import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

@Configuration
public class DefaultWindowMenuItemConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultWindowMenuItemConfiguration.class);
    private static final String TACTVIEW_LAYOUT_EXTENSION = "tvlay";
    public static final String WINDOW_MENU_ITEM = "Window";

    private DockableTabRepository dockableTabRepository;
    private ObjectMapper objectMapper;

    public DefaultWindowMenuItemConfiguration(DockableTabRepository dockableTabRepository, DockableTabFromIdFactory dockableTabFromIdFactory) {
        this.dockableTabRepository = dockableTabRepository;

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new SimpleModule()
                .addSerializer(new DetachableTabSerializer(DetachableTab.class))
                .addDeserializer(DetachableTab.class, new DetachableTabDeserializer(DetachableTab.class, dockableTabFromIdFactory)));
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Bean
    @Order(3997)
    public SelectableMenuContribution saveLayoutMenuItem(MessagingService messagingService, QuerySaveFilenameService querySaveFilenameService,
            @Value("${tactview.homedirectory}") String homeDirectory, AlertDialogFactory alertDialogFactory) {
        return new DefaultMenuContribution(List.of(WINDOW_MENU_ITEM, "Save layout"), e -> {
            QuerySaveFileNameRequest request = QuerySaveFileNameRequest.builder()
                    .withInitialDirectory(homeDirectory)
                    .withTitle("Save layout file")
                    .build();
            Optional<String> optionalFileName = querySaveFilenameService.queryUserAboutFileName(request);

            if (optionalFileName.isPresent()) {
                try {
                    String fileName = optionalFileName.get();
                    if (!fileName.endsWith("." + TACTVIEW_LAYOUT_EXTENSION)) {
                        fileName += ("." + TACTVIEW_LAYOUT_EXTENSION);
                    }

                    DetachableTabPaneLoadModel data = dockableTabRepository.extractLoadModel();
                    String result = objectMapper.writeValueAsString(data);

                    try (FileOutputStream fos = new FileOutputStream(new File(fileName))) {
                        fos.write(result.getBytes(StandardCharsets.UTF_8));
                    }
                } catch (Exception e1) {
                    alertDialogFactory.showExceptionDialog("Unable to save layout", e1);
                    LOGGER.error("Unable to save layout", e1);
                }

            }
        });
    }

    @Bean
    @Order(3998)
    public SelectableMenuContribution loadLayoutMenuItem(MessagingService messagingService, AlertDialogFactory alertDialogFactory) {
        return new DefaultMenuContribution(List.of(WINDOW_MENU_ITEM, "Load layout"), e -> {
            try {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setSelectedExtensionFilter(new ExtensionFilter("TactView layout file", TACTVIEW_LAYOUT_EXTENSION));
                fileChooser.setTitle("Open Layout");
                File file = fileChooser.showOpenDialog(JavaFXUiMain.STAGE);

                if (file != null) {
                    try (var inputStream = new FileInputStream(file)) {
                        DetachableTabPaneLoadModel result = objectMapper.readValue(inputStream, DetachableTabPaneLoadModel.class);
                        dockableTabRepository.loadAndSetModelToParent(result);
                    }
                }
            } catch (Exception e1) {
                alertDialogFactory.showExceptionDialog("Unable to load layout", e1);
                LOGGER.error("Unable to load layout", e1);
            }

        });
    }

    @Bean
    @Order(3999)
    public SelectableMenuContribution resetLayoutMenuItem(MessagingService messagingService, DefaultLayoutProvider defaultLayoutProvider) {
        return new DefaultMenuContribution(List.of(WINDOW_MENU_ITEM, "Reset layout"), e -> {
            dockableTabRepository.loadAndSetModelToParent(defaultLayoutProvider.provideDefaultLayout());
        });
    }

    @Bean
    @Order(3999)
    public SeparatorMenuContribution separatorAfterSaveAndLoadLayout(MessagingService messagingService) {
        return new SeparatorMenuContribution(List.of(WINDOW_MENU_ITEM));
    }

    static class DetachableTabSerializer extends StdSerializer<DetachableTab> {
        private static final long serialVersionUID = 1L;

        protected DetachableTabSerializer(Class<DetachableTab> type) {
            super(type);
        }

        @Override
        public void serialize(DetachableTab value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString(value.getTabId());
        }

    }

    static class DetachableTabDeserializer extends StdDeserializer<DetachableTab> {
        private static final long serialVersionUID = 1L;
        private DockableTabFromIdFactory dockableTabFromIdFactory;

        public DetachableTabDeserializer(Class<DetachableTab> type, DockableTabFromIdFactory dockableTabFromIdFactory) {
            super(type);
            this.dockableTabFromIdFactory = dockableTabFromIdFactory;
        }

        @Override
        public DetachableTab deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            String id = p.getValueAsString();
            return dockableTabFromIdFactory.createTab(id);
        }

    }

}
