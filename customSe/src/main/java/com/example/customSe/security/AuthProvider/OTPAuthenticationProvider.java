package com.example.customSe.security.AuthProvider;

import com.example.customSe.entity.User;
import com.example.customSe.repository.UserRepository;
import com.example.customSe.security.MyUserDetails;
import com.example.customSe.security.authentication.OTPAuthentication;
import com.example.customSe.service.CustomUserDetailsService;
import com.example.customSe.service.OtpService;
import com.example.customSe.utils.GoogleAuthenticatorService;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class OTPAuthenticationProvider implements AuthenticationProvider {
    private final OtpService otpService;
    private final CustomUserDetailsService userDetailsService;
    private final GoogleAuthenticatorService googleAuthenticatorService;
    private final UserRepository userRepository;

    public OTPAuthenticationProvider(OtpService otpService, GoogleAuthenticatorService googleAuthenticatorService, CustomUserDetailsService userDetailsService, UserRepository userRepository) {
        this.otpService = otpService;
        this.googleAuthenticatorService = googleAuthenticatorService;

        this.userDetailsService = userDetailsService;
        this.userRepository = userRepository;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        if (!(authentication instanceof OTPAuthentication otpAuth)) {
            throw new IllegalArgumentException("It required only OTPAuthentication object");
        }
        Object principal = otpAuth.getPrincipal();
        String username = (principal instanceof MyUserDetails userDetails)
                ? userDetails.getUsername()
                : principal.toString();
        if (otpAuth.getOtpChannel().equalsIgnoreCase("APP")) {
            String provideOtp = authentication.getCredentials().toString();

            User user=userRepository.findByUsernameOrEmail(username,null)
                 .orElseThrow(()->new RuntimeException("user not found"));
           if(googleAuthenticatorService.verifyCode(user.getSecretKey(), Integer.parseInt(provideOtp))){
               throw new BadCredentialsException("Invalid otp");
           }

        } else if (otpAuth.getOtpChannel().equalsIgnoreCase("EMAIL")) {
            String provideOtp = authentication.getCredentials().toString();
            if (!otpService.verifyOtp(username, provideOtp)) {
                throw new BadCredentialsException("Invalid otp");
            }
            otpService.clearOtp(username);
        }
        Collection<? extends GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_ADMIN")
        );

        return new OTPAuthentication(
                username,
                null,
                authorities,
                true,
                otpAuth.getOtpChannel(),
                otpAuth.getDeviceId(),
                otpAuth.getIpAddress(),
                otpAuth.getUserAgent(),
                otpAuth.getOtpAttemptTime()
        );
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return OTPAuthentication.class.isAssignableFrom(authentication);
    }
}
