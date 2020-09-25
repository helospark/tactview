package com.helospark.tactview.ui.javafx.save;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.persistentstate.PersistentState;

@Component
public class RecentlyAccessedRepository {
    @PersistentState
    private List<String> recentlySavedElements = new ArrayList<>();

    public void addNewRecentlySavedElement(File file) {
        String elementToAdd = file.getAbsolutePath();
        recentlySavedElements.remove(elementToAdd);
        recentlySavedElements.add(0, elementToAdd);

        while (recentlySavedElements.size() > 10) {
            recentlySavedElements.remove(recentlySavedElements.size() - 1);
        }
    }

    public List<File> getRecentlySavedElements() {
        return recentlySavedElements.stream()
                .map(a -> new File(a))
                .filter(a -> a.exists())
                .collect(Collectors.toList());
    }

    public void setRecentlySavedElements(List<String> recentlySavedElements) {
        this.recentlySavedElements = recentlySavedElements;
    }

}
