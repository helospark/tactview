package com.helospark.tactview.core.timeline.message;

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

    public static enum Level {
        WARNING,
        ERROR
    }

}
