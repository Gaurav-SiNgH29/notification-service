package com.indiagold.notification_service.channel.sms;

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
public class MockSmsChannel implements NotificationChannel {

    private final RandomGenerator random = RandomGenerator.getDefault();

    @Override
    public ChannelType getChannelType() {
        return ChannelType.SMS;
    }

    @Override
    public DispatchResult send(User user, String title, String body) {
        log.info("SMS dispatch attempt — to={}, title={}", user.getPhone(), title);

        if (user.getPhone() == null) {
            log.error("SMS dispatch FAILED — userId={}, reason={}",
                    user.getId(), ChannelConstants.SMS_NO_PHONE_MSG);
            return DispatchResult.failed(ChannelConstants.SMS_NO_PHONE_MSG);
        }

        if (random.nextInt(100) < ChannelConstants.SMS_FAILURE_RATE) {
            log.error("SMS dispatch FAILED — to={}, reason={}",
                    user.getPhone(), ChannelConstants.SMS_FAILURE_MSG);
            return DispatchResult.failed(ChannelConstants.SMS_FAILURE_MSG);
        }

        log.info("SMS dispatch DELIVERED — to={}", user.getPhone());
        return DispatchResult.delivered();
    }
}