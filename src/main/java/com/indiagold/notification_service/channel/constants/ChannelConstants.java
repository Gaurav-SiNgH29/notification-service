package com.indiagold.notification_service.channel.constants;

public final class ChannelConstants {

    private ChannelConstants() {}

    //  Failure Rates (percentage) 
    public static final int EMAIL_FAILURE_RATE   = 15;
    public static final int SMS_FAILURE_RATE     = 10;
    public static final int PUSH_FAILURE_RATE    = 35;
    public static final int IN_APP_FAILURE_RATE  = 10;

    // ─── Error Messages 
    public static final String EMAIL_FAILURE_MSG  = "Simulated email delivery failure";
    public static final String SMS_FAILURE_MSG    = "Simulated SMS delivery failure";
    public static final String SMS_NO_PHONE_MSG   = "No phone number on record";
    public static final String PUSH_FAILURE_MSG   = "Simulated push delivery failure";
    public static final String PUSH_NO_TOKEN_MSG  = "No device token on record";
    public static final String IN_APP_FAILURE_MSG = "Simulated in-app delivery failure";
}