package com.indiagold.notification_service.channel;

import com.indiagold.notification_service.domain.User;
import com.indiagold.notification_service.domain.enums.ChannelType;

public interface NotificationChannel {

    ChannelType getChannelType();

    DispatchResult send(User user, String title, String body);
}