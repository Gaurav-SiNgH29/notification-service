package com.indiagold.notification_service.channel;

import com.indiagold.notification_service.domain.enums.DeliveryStatus;
import lombok.Getter;

@Getter
public class DispatchResult {

    private final DeliveryStatus status;
    private final String errorMessage;

    private DispatchResult(DeliveryStatus status, String errorMessage) {
        this.status = status;
        this.errorMessage = errorMessage;
    }

    public static DispatchResult delivered() {
        return new DispatchResult(DeliveryStatus.DELIVERED, null);
    }

    public static DispatchResult failed(String reason) {
        return new DispatchResult(DeliveryStatus.FAILED, reason);
    }
}