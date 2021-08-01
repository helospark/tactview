package com.helospark.tactview.ui.javafx.uicomponents.notification;

import org.controlsfx.control.Notifications;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.message.NotificationMessage;
import com.helospark.tactview.ui.javafx.UiMessagingService;

@Component
public class NotificationMessageListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationMessageListener.class);

    public NotificationMessageListener(UiMessagingService messagingService) {
        messagingService.register(NotificationMessage.class, message -> {
            Notifications builder = Notifications.create()
                    .darkStyle()
                    .title(message.getTitle())
                    .text(message.getMessage());

            LOGGER.info("Showing notification {}", message);

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
