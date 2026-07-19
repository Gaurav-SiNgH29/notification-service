package com.indiagold.notification_service.channel.email;

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
public class MockEmailChannel implements NotificationChannel {

    private final RandomGenerator random = RandomGenerator.getDefault();

    @Override
    public ChannelType getChannelType() {
        return ChannelType.EMAIL;
    }

    @Override
    public DispatchResult send(User user, String title, String body) {
        log.info("EMAIL dispatch attempt — to={}, title={}", user.getEmail(), title);

        if (random.nextInt(100) < ChannelConstants.EMAIL_FAILURE_RATE) {
            log.error("EMAIL dispatch FAILED — to={}, reason={}",
                    user.getEmail(), ChannelConstants.EMAIL_FAILURE_MSG);
            return DispatchResult.failed(ChannelConstants.EMAIL_FAILURE_MSG);
        }

        log.info("EMAIL dispatch DELIVERED — to={}", user.getEmail());
        return DispatchResult.delivered();
    }
}