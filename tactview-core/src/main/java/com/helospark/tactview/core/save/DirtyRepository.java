package com.helospark.tactview.core.save;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.helospark.lightdi.annotation.Component;

@Component
public class DirtyRepository {
    private boolean isDirty = false;
    private long dirtyStatusChange = 0;
    private List<Consumer<Boolean>> listeners = new ArrayList<>();

    public boolean isDirty() {
        return isDirty;
    }

    public void setDirty(boolean newDirtyStatus) {
        callListeners(newDirtyStatus);
        this.isDirty = newDirtyStatus;
        dirtyStatusChange = System.currentTimeMillis();
    }

    private void callListeners(boolean dirty) {
        listeners.stream()
                .forEach(a -> a.accept(dirty));
    }

    public void addUiChangeListener(Consumer<Boolean> onDirtyChangeListener) {
        listeners.add(onDirtyChangeListener);
    }

    public long getDirtyStatusChange() {
        return dirtyStatusChange;
    }

}
