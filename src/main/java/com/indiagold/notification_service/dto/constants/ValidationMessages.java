package com.indiagold.notification_service.dto.constants;

public final class ValidationMessages {

    private ValidationMessages() {}

    public static final String USER_ID_REQUIRED  = "userId is required";
    public static final String USER_ID_POSITIVE  = "userId must be a positive number";
    public static final String TITLE_REQUIRED    = "title is required";
    public static final String BODY_REQUIRED     = "body is required";
    public static final String CHANNELS_REQUIRED = "at least one channel is required";
}