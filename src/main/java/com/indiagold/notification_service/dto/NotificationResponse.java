package com.indiagold.notification_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class NotificationResponse {

    private final Long userId;
    private final List<ChannelResult> results;
}