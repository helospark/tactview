package com.helospark.tactview.ui.javafx.menu;

import java.util.List;

import javafx.event.ActionEvent;

public interface MenuContribution {

    public List<String> getPath();

    public void onAction(ActionEvent event);

}
