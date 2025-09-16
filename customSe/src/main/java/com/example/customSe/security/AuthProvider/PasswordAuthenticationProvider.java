package com.example.customSe.security.AuthProvider;

import com.example.customSe.service.OtpService;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

public class PasswordAuthenticationProvider implements AuthenticationProvider {
    private final DaoAuthenticationProvider daoAuthenticationProvider;
    private final OtpService otpService;

    public PasswordAuthenticationProvider(DaoAuthenticationProvider daoAuthenticationProvider, OtpService otpService) {
        this.daoAuthenticationProvider = daoAuthenticationProvider;
        this.otpService = otpService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Authentication auth = daoAuthenticationProvider.authenticate(authentication);
        if (!auth.isAuthenticated()) {
            return auth;
        }
        String otpChannel=((ExtendedUsernamePasswordAuthenticationToken)authentication).getOtpChannel();
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        if(otpChannel.equalsIgnoreCase("APP")){

        }
        otpService.generateAndSendOtp(userDetails.getUsername(), otpChannel); // implement this service

        return new ExtendedUsernamePasswordAuthenticationToken(auth.getPrincipal(), auth.getCredentials()
                , Collections.singleton(new SimpleGrantedAuthority("ROLE_OTP"))
                ,otpChannel);
/*        return new UsernamePasswordAuthenticationToken(auth.getPrincipal(), auth.getCredentials()
                , Collections.singleton(new SimpleGrantedAuthority("ROLE_OTP"))) {
            @Override
            public boolean isAuthenticated() {
                return false;
            }
        };*/
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
