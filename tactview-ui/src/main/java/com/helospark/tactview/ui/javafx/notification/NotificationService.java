package com.helospark.tactview.ui.javafx.notification;

import org.controlsfx.control.Notifications;

import com.helospark.lightdi.annotation.Component;

@Component
public class NotificationService {

    public void showWarning(String title, String message) {
        Notifications.create()
                .darkStyle()
                .title(title)
                .text(message)
                .showWarning();
    }

}
