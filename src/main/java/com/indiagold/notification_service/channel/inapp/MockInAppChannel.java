package com.indiagold.notification_service.channel.inapp;

import com.indiagold.notification_service.channel.DispatchResult;
import com.indiagold.notification_service.channel.NotificationChannel;
import com.indiagold.notification_service.channel.constants.ChannelConstants;
import com.indiagold.notification_service.domain.User;
import com.indiagold.notification_service.domain.enums.ChannelType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.random.RandomGenerator;

@Slf4j
@Component
public class MockInAppChannel implements NotificationChannel {

    private final RandomGenerator random = RandomGenerator.getDefault();

    @Override
    public ChannelType getChannelType() {
        return ChannelType.IN_APP;
    }

    @Override
    public DispatchResult send(User user, String title, String body) {
        log.info("IN_APP dispatch attempt — userId={}, title={}", user.getId(), title);

        if (random.nextInt(100) < ChannelConstants.IN_APP_FAILURE_RATE) {
            log.error("IN_APP dispatch FAILED — userId={}, reason={}",
                    user.getId(), ChannelConstants.IN_APP_FAILURE_MSG);
            return DispatchResult.failed(ChannelConstants.IN_APP_FAILURE_MSG);
        }

        log.info("IN_APP dispatch DELIVERED — userId={}", user.getId());
        return DispatchResult.delivered();
    }
}