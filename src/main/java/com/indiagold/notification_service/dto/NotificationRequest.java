package com.indiagold.notification_service.dto;

import com.indiagold.notification_service.domain.enums.ChannelType;
import com.indiagold.notification_service.dto.constants.ValidationMessages;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class NotificationRequest {

    @NotNull(message = ValidationMessages.USER_ID_REQUIRED)
    @Positive(message =  ValidationMessages.USER_ID_POSITIVE)
    private Long userId;

    @NotBlank(message =  ValidationMessages.TITLE_REQUIRED)
    private String title;

    @NotBlank(message =  ValidationMessages.BODY_REQUIRED)
    private String body;

    @NotEmpty(message = ValidationMessages.CHANNELS_REQUIRED)
    private List<ChannelType> channels;
}