package com.example.customSe.security.authentication;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class RiskAuthentication implements Authentication {
  private final Serializable principal;
       private final Collection<? extends GrantedAuthority>     authorities;
    private boolean authenticated;

    private final String ipAddress;
    private final String userAgent;
    private final String deviceId;
    private final Instant timestamp;
    private final Map<String, Object> metadata;
    public RiskAuthentication(Serializable principal, Collection<? extends GrantedAuthority> authorities, String ipAddress, String userAgent, String deviceId, Instant timestamp, Map<String, Object> metadata) {
        this.principal = principal;
        this.authorities = authorities;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.deviceId = deviceId;
        this.timestamp = timestamp;
        this.metadata = metadata;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public Object getCredentials() {
        return null;
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
this.authenticated=isAuthenticated;
    }

    @Override
    public String getName() {
        if(principal instanceof UserDetails user){
            return  user.getUsername();
        }
        return principal!=null?principal.toString():"";
    }
    public String getDeviceId() {
        return deviceId;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }
}
