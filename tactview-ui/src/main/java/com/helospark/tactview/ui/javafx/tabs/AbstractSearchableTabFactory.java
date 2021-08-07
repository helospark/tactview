package com.helospark.tactview.ui.javafx.tabs;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.helospark.tactview.ui.javafx.uicomponents.detailsdata.localizeddetail.LocalizedDetailDomain;

import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;

public abstract class AbstractSearchableTabFactory implements TabFactory {
    private String tabText;
    private String tabId;

    public AbstractSearchableTabFactory(String tabText, String tabId) {
        this.tabText = tabText;
        this.tabId = tabId;
    }

    @Override
    public Tab createTabContent() {
        BorderPane borderPane = new BorderPane();
        borderPane.setOnDragOver(event -> event.acceptTransferModes(TransferMode.ANY));

        FlowPane effectTabContent = new FlowPane(Orientation.HORIZONTAL, 5, 5);

        fillFlowPane(effectTabContent, "");
        Tab effectTab = new Tab();
        effectTab.setText(tabText);

        ScrollPane effectScrollPane = new ScrollPane(effectTabContent);
        effectScrollPane.setFitToWidth(true);
        effectScrollPane.setId(tabId);

        HBox topBox = new HBox();
        topBox.prefWidthProperty().bind(borderPane.widthProperty());
        TextField searchTextField = new TextField();
        searchTextField.setPromptText("Search...");
        searchTextField.getStyleClass().add("tab-search-text-field");
        searchTextField.prefWidthProperty().bind(topBox.widthProperty());
        searchTextField.textProperty().addListener(e -> {
            fillFlowPane(effectTabContent, searchTextField.getText());
        });

        topBox.getChildren().add(searchTextField);

        borderPane.setTop(topBox);
        borderPane.setCenter(effectScrollPane);

        effectTab.setContent(borderPane);

        return effectTab;
    }

    abstract protected void fillFlowPane(FlowPane effectTabContent, String string);

    protected int getScore(Optional<LocalizedDetailDomain> localizedDetail, String id, String name, String searchData) {
        if (searchData.equals("")) {
            return 1;
        }
        int score = 0;
        Pattern pattern = Pattern.compile(Pattern.quote(searchData), Pattern.CASE_INSENSITIVE);
        score += numberOfCaseInsensitiveMatches(id, pattern) * 10;
        score += numberOfCaseInsensitiveMatches(name, pattern) * 10;

        if (localizedDetail.isPresent()) {
            LocalizedDetailDomain ld = localizedDetail.get();
            score += numberOfCaseInsensitiveMatches(ld.getDescription(), pattern);
        }

        if (searchData.length() == 1) {
            return score > 0 ? 1 : 0;
        }

        return score;
    }

    protected int numberOfCaseInsensitiveMatches(String id, Pattern pattern) {
        int matchCount = 0;
        Matcher matcher = pattern.matcher(id);
        while (matcher.find()) {
            ++matchCount;
        }
        return matchCount;
    }

    protected static class ScoredNodeHolder implements Comparable<ScoredNodeHolder> {
        Node node;
        int score;

        public ScoredNodeHolder(Node node, int score) {
            this.node = node;
            this.score = score;
        }

        @Override
        public int compareTo(ScoredNodeHolder o) {
            return Integer.compare(score, o.score);
        }
    }
}
