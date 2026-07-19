package com.indiagold.notification_service.channel.push;

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
public class MockPushChannel implements NotificationChannel {

    private final RandomGenerator random = RandomGenerator.getDefault();

    @Override
    public ChannelType getChannelType() {
        return ChannelType.PUSH;
    }

    @Override
    public DispatchResult send(User user, String title, String body) {
        log.info("PUSH dispatch attempt — deviceToken={}, title={}",
                user.getDeviceToken(), title);

        if (user.getDeviceToken() == null) {
            log.error("PUSH dispatch FAILED — userId={}, reason={}",
                    user.getId(), ChannelConstants.PUSH_NO_TOKEN_MSG);
            return DispatchResult.failed(ChannelConstants.PUSH_NO_TOKEN_MSG);
        }

        if (random.nextInt(100) < ChannelConstants.PUSH_FAILURE_RATE) {
            log.error("PUSH dispatch FAILED — deviceToken={}, reason={}",
                    user.getDeviceToken(), ChannelConstants.PUSH_FAILURE_MSG);
            return DispatchResult.failed(ChannelConstants.PUSH_FAILURE_MSG);
        }

        log.info("PUSH dispatch DELIVERED — deviceToken={}", user.getDeviceToken());
        return DispatchResult.delivered();
    }
}