package com.example.customSe.security.AuthProvider;

import com.example.customSe.security.JwtTokenProvider;
import com.example.customSe.security.authentication.JWTAuthentication;
import com.example.customSe.service.CustomUserDetailsService;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;

public class JWTAuthenticationprovider implements AuthenticationProvider {
    private final CustomUserDetailsService userDetailsService;
    private final JwtTokenProvider jwtTokenProvider;

    public JWTAuthenticationprovider(CustomUserDetailsService userDetailsService, JwtTokenProvider jwtTokenProvider) {
        this.userDetailsService = userDetailsService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        try {
            String token = (String) authentication.getCredentials();
            if (!jwtTokenProvider.validateToken(token)) {
                return null;  // or throw an exception
            }
            String username = jwtTokenProvider.getUsernameFromJWT(token);
            if (username == null) {
                return null;
            }
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            return new JWTAuthentication(userDetails, token, userDetails.getAuthorities(), true);
        } catch (InternalAuthenticationServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex);
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        // This provider supports authentication requests with String token credentials,
        // but typically you check for a custom Authentication implementation here.
        // For example, if you have a JWTAuthenticationToken class, check for that.

        // If you are passing the token in UsernamePasswordAuthenticationToken, then:
        return JWTAuthentication.class.isAssignableFrom(authentication);
    }
}
