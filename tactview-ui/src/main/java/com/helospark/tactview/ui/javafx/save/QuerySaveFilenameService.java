package com.helospark.tactview.ui.javafx.save;

import java.io.File;
import java.util.Optional;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.ui.javafx.JavaFXUiMain;
import com.helospark.tactview.ui.javafx.stylesheet.StylesheetAdderService;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;

@Component
public class QuerySaveFilenameService {
    private StylesheetAdderService stylesheetAdderService;

    public QuerySaveFilenameService(StylesheetAdderService stylesheetAdderService) {
        this.stylesheetAdderService = stylesheetAdderService;
    }

    public Optional<String> queryUserAboutFileName(QuerySaveFileNameRequest request) {
        while (true) {
            FileChooser fileChooser = new FileChooser();
            String initialDirectory = request.initialDirectory;
            fileChooser.setTitle(request.title);
            if (initialDirectory != null) {
                fileChooser.setInitialDirectory(new File(initialDirectory));
            }
            File file = fileChooser.showSaveDialog(JavaFXUiMain.STAGE);

            if (file == null) {
                return Optional.empty();
            }

            String extension = file.getName().endsWith(".tvs") ? "" : ".tvs";
            File fileWithSavedExtension = new File(file.getAbsolutePath() + extension); // TODO: why is this needed in core and ui?

            if (!extension.isEmpty() && fileWithSavedExtension.exists()) { // If user set extension JavaFX already asked this question
                Alert alert = new Alert(AlertType.CONFIRMATION);
                alert.setContentText("File " + fileWithSavedExtension.getAbsolutePath() + " already exists. Override?");
                alert.setHeaderText(null);
                alert.getButtonTypes().setAll(ButtonType.NO, ButtonType.CANCEL, ButtonType.YES);
                stylesheetAdderService.addStyleSheets(alert.getDialogPane(), "stylesheet.css");
                alert.showAndWait();

                if (alert.getResult() == ButtonType.YES) {
                    return Optional.ofNullable(file.getAbsolutePath());
                } else if (alert.getResult() == ButtonType.CANCEL) {
                    return Optional.empty();
                } // else in case of 'NO' try again
            } else {
                return Optional.ofNullable(file.getAbsolutePath());
            }
        }

    }

    public static class QuerySaveFileNameRequest {
        String initialDirectory;
        String title;
        private QuerySaveFileNameRequest(Builder builder) {
            this.initialDirectory = builder.initialDirectory;
            this.title = builder.title;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static final class Builder {
            private String initialDirectory;
            private String title;
            private Builder() {
            }

            public Builder withInitialDirectory(String initialDirectory) {
                this.initialDirectory = initialDirectory;
                return this;
            }

            public Builder withTitle(String title) {
                this.title = title;
                return this;
            }

            public QuerySaveFileNameRequest build() {
                return new QuerySaveFileNameRequest(this);
            }
        }

    }

}
