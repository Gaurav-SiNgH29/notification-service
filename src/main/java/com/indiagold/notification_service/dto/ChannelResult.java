package com.indiagold.notification_service.dto;

import com.indiagold.notification_service.domain.enums.ChannelType;
import com.indiagold.notification_service.domain.enums.DeliveryStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor

public class ChannelResult {
    private final ChannelType channel;
    private final DeliveryStatus status;
}
