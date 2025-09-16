package com.example.customSe.utils;
public class TimedOtpUtil {
    private final String otp;
    private final long step;

    public TimedOtpUtil(String otp, long step) {
        this.otp = otp;
        this.step = step;
    }

    public String getOtp() {
        return otp;
    }

    public long getStep() {
        return step;
    }

}
