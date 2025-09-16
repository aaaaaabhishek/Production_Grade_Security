package com.example.customSe.security.manager;

import com.example.customSe.security.AuthProvider.ExtendedUsernamePasswordAuthenticationToken;
import com.example.customSe.security.AuthProvider.OTPAuthenticationProvider;
import com.example.customSe.security.AuthProvider.PasswordAuthenticationProvider;
import com.example.customSe.security.authentication.OTPAuthentication;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

public class CustomAuthenticationManager implements AuthenticationManager {

    private final PasswordAuthenticationProvider passwordAuthenticationProvider;
    private final OTPAuthenticationProvider otpAuthenticationProvider;

    public CustomAuthenticationManager(PasswordAuthenticationProvider passwordAuthenticationProvider, OTPAuthenticationProvider otpAuthenticationProvider) {
        this.passwordAuthenticationProvider = passwordAuthenticationProvider;
        this.otpAuthenticationProvider = otpAuthenticationProvider;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (authentication instanceof ExtendedUsernamePasswordAuthenticationToken) {
            Authentication result = passwordAuthenticationProvider.authenticate(authentication);

            if (result.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_OTP"))) {
                // Password OK, waiting for OTP
                return result;
            }

            // Optional risk check after password step
            //     return riskProvider.authenticate(result);
        }

        if (authentication instanceof OTPAuthentication) {
            Authentication result = otpAuthenticationProvider.authenticate(authentication);
            return result;
            // Optional risk check after OTP step
//                return riskProvider.authenticate(result);
        }

        throw new ProviderNotFoundException("Unsupported authentication token type: " + authentication.getClass());
    }
}
