package com.helospark.tactview.ui.javafx;

public class DisplayUpdateRequestMessage {
    boolean invalidateCache;

    public DisplayUpdateRequestMessage(boolean invalidateCache) {
        this.invalidateCache = invalidateCache;
    }

    public boolean isInvalidateCache() {
        return invalidateCache;
    }

    @Override
    public String toString() {
        return "DisplayUpdateRequestMessage [invalidateCache=" + invalidateCache + "]";
    }

}
