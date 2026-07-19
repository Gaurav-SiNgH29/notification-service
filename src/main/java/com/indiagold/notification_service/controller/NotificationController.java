package com.indiagold.notification_service.controller;

import com.indiagold.notification_service.domain.enums.DeliveryStatus;
import com.indiagold.notification_service.dto.ChannelResult;
import com.indiagold.notification_service.dto.NotificationRequest;
import com.indiagold.notification_service.dto.NotificationResponse;
import com.indiagold.notification_service.service.NotificationService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    public ResponseEntity<NotificationResponse> sendNotification(
            @Valid @RequestBody NotificationRequest request) {

        log.info("Received notification request — userId={}, channels={}",
                request.getUserId(), request.getChannels());

        List<ChannelResult> results = notificationService.processNotification(request);

        HttpStatus status = determineHttpStatus(results);
        NotificationResponse response = new NotificationResponse(request.getUserId(), results);

        log.info("Returning response — userId={}, httpStatus={}", 
                request.getUserId(), status);

        return ResponseEntity.status(status).body(response);
    }
 

    private HttpStatus determineHttpStatus(List<ChannelResult> results) {
        long failedCount = countByStatus(results, DeliveryStatus.FAILED);

        if (failedCount == 0) {
            return HttpStatus.OK;
        }

        if (failedCount == results.size()) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }

        return HttpStatus.MULTI_STATUS;
    }

    private long countByStatus(List<ChannelResult> results, DeliveryStatus status) {
        return results.stream()
                .filter(result -> result.getStatus() == status)
                .count();
    }
}