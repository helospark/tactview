package com.helospark.tactview.ui.javafx.uicomponents.notification;

import org.controlsfx.control.Notifications;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.message.NotificationMessage;
import com.helospark.tactview.ui.javafx.UiMessagingService;

@Component
public class NotificationMessageListener {

    public NotificationMessageListener(UiMessagingService messagingService) {
        messagingService.register(NotificationMessage.class, message -> {
            Notifications builder = Notifications.create()
                    .darkStyle()
                    .title(message.getTitle())
                    .text(message.getMessage());

            if (message.getLevel().equals(NotificationMessage.Level.ERROR)) {
                builder.showError();
            } else if (message.getLevel().equals(NotificationMessage.Level.WARNING)) {
                builder.showWarning();
            } else {
                builder.show();
            }
        });
    }

}
