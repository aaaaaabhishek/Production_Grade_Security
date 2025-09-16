package com.example.customSe.payload.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OtpRequest {
    private String username;
    private String otp;
}
