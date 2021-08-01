package com.helospark.tactview.core.timeline.message;

import java.util.Objects;

public class NotificationMessage {
    private String message;
    private String title;
    private Level level;

    public NotificationMessage(String message, String title, Level level) {
        this.message = message;
        this.title = title;
        this.level = level;
    }

    public String getMessage() {
        return message;
    }

    public String getTitle() {
        return title;
    }

    public Level getLevel() {
        return level;
    }

    @Override
    public String toString() {
        return "NotificationMessage [message=" + message + ", title=" + title + ", level=" + level + "]";
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof NotificationMessage)) {
            return false;
        }
        NotificationMessage castOther = (NotificationMessage) other;
        return Objects.equals(message, castOther.message) && Objects.equals(title, castOther.title) && Objects.equals(level, castOther.level);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message, title, level);
    }

    public static enum Level {
        WARNING,
        ERROR
    }

}
