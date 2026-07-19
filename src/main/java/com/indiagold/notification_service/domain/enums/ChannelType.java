package com.indiagold.notification_service.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum ChannelType {
    EMAIL,
    SMS,
    PUSH,
    IN_APP;
      @JsonCreator
    public static ChannelType fromString(String value) {
        return ChannelType.valueOf(value.toUpperCase());
    }
}
