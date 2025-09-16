package com.example.customSe.security.AuthProvider;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class ExtendedUsernamePasswordAuthenticationToken extends UsernamePasswordAuthenticationToken {
    private final String otpChannel;

    public ExtendedUsernamePasswordAuthenticationToken(Object principal, Object credentials, String otpChannel) {
        super(principal, credentials);
        this.otpChannel = otpChannel;
    }
    public ExtendedUsernamePasswordAuthenticationToken(
            Object principal,
            Object credentials,
            Collection<? extends GrantedAuthority> authorities, String otpChannel
            ) {
        super(principal, credentials, authorities);
        this.otpChannel = otpChannel;
    }
    public String getOtpChannel() {
        return otpChannel;
    }
}

