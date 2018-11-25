package com.helospark.tactview.ui.javafx.uicomponents.propertyvalue;

import java.io.File;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.EffectParametersRepository;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.FileProvider;
import com.helospark.tactview.ui.javafx.JavaFXUiMain;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.UiTimelineManager;

import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

@Component
public class FilePropertyValueSetterChainItem extends TypeBasedPropertyValueSetterChainItem<FileProvider> {
    private UiCommandInterpreterService commandInterpreter;
    private EffectParametersRepository effectParametersRepository;
    private UiTimelineManager uiTimelineManager;

    public FilePropertyValueSetterChainItem(EffectParametersRepository effectParametersRepository,
            UiCommandInterpreterService commandInterpreter, UiTimelineManager uiTimelineManager) {
        super(FileProvider.class);
        this.commandInterpreter = commandInterpreter;
        this.effectParametersRepository = effectParametersRepository;
        this.uiTimelineManager = uiTimelineManager;
    }

    @Override
    protected EffectLine handle(FileProvider fileProvider) {
        TextField textArea = new TextField();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setSelectedExtensionFilter(new ExtensionFilter(fileProvider.getExtension(), new String[]{fileProvider.getExtension()}));
        fileChooser.setTitle("Open Resource File");
        Button browseButton = new Button("Browse");

        HBox hbox = new HBox();
        hbox.getChildren().addAll(textArea, browseButton);

        PrimitiveEffectLine lineItem = PrimitiveEffectLine.builder()
                .withCurrentValueProvider(() -> textArea.getText())
                .withDescriptorId(fileProvider.getId())
                .withUpdateFunction(position -> {
                    String fileName = providerValueToString(fileProvider.getId(), position);
                    textArea.setText(fileName);
                    fileChooser.setInitialFileName(fileName);
                })
                .withVisibleNode(hbox)
                .withCommandInterpreter(commandInterpreter)
                .withEffectParametersRepository(effectParametersRepository)
                .withUpdateFromValue(value -> {
                    textArea.setText((String) value);
                    fileChooser.setInitialFileName((String) value);
                })
                .build();
        browseButton.setOnMouseClicked(e -> {
            File file = fileChooser.showOpenDialog(JavaFXUiMain.STAGE);
            if (file != null) {
                lineItem.updateFromValue.accept(file.getAbsolutePath());
                lineItem.sendKeyframe(uiTimelineManager.getCurrentPosition());
            }
        });

        return lineItem;
    }

    private String providerValueToString(String id, TimelinePosition position) {
        return effectParametersRepository.getValueAt(id, position);
    }

}
