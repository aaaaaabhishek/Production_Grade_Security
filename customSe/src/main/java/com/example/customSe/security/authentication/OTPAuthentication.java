package com.example.customSe.security.authentication;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
public class OTPAuthentication implements Authentication {
    @Serial
    private static  final long serialVersionUID=1L;
    private final Serializable principal;
    private final transient Serializable credentials;
    private Collection<? extends GrantedAuthority> authorities;
    private boolean authenticated = false;
    private final String otpChannel;         // SMS, Email, App
    private final String deviceId;           // Device fingerprint or UUID
    private final String ipAddress;          // For audit or risk checks
    private final String userAgent;          // Optional
    private final long otpAttemptTime;       // Epoch milliseconds
    public OTPAuthentication(Serializable principal, Serializable credentials, Collection<? extends GrantedAuthority> authorities, boolean authenticated, String otpChannel, String deviceId, String ipAddress, String userAgent, long otpAttemptTime) {
        this.principal = principal;
        this.credentials = credentials;
        this.authorities = authorities;
        this.authenticated = authenticated;
        this.otpChannel = otpChannel;
        this.deviceId = deviceId;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.otpAttemptTime = otpAttemptTime;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public Object getCredentials() {
        return credentials;
    }

    @Override
    public Object getDetails() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

    @Override
    public boolean isAuthenticated() {
        return authenticated;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        this.authenticated = isAuthenticated;
    }

    @Override
    public String getName() {
        if(principal instanceof UserDetails userDetails){
            return userDetails.getUsername();
        }
        return principal!=null?principal.toString():"";
    }
    public String getOtpChannel() {
        return otpChannel;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public long getOtpAttemptTime() {
        return otpAttemptTime;
    }

    public String getIpAddress() {
        return ipAddress;
    }

}
