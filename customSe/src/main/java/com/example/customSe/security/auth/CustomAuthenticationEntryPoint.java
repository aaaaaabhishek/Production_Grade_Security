package com.example.customSe.security.auth;

import com.example.customSe.payload.response.ApiErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Slf4j
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {

        String correlationId = Optional.ofNullable(request.getHeader("X-Correlation-ID"))
                .orElse(UUID.randomUUID().toString());
        String path = request.getRequestURI();
        String method = request.getMethod();
        String ip = getClientIp(request);
        try {
             MDC.put("correlationId",correlationId);
             String message;
             if(authException instanceof BadCredentialsException){
                 message="Invalid username or password";
             }else if (authException instanceof InsufficientAuthenticationException){
                 message="Authentication token is missing or invalid";
             }else{
                 message="Authentication is required to access this resource";
             }
             log.warn("UNAUTHORIZED ACCESS: ip={} method={} path={} correlationId={} authMessage{}",
                     ip,method,path,correlationId,authException.getMessage());
            ApiErrorResponse errorResponse=new ApiErrorResponse(
                    correlationId,
                     "Unauthorized",
                    message,
                    path,
                    HttpServletResponse.SC_UNAUTHORIZED
                    );
           response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
           response.setContentType("application/json");
           mapper.writeValue(response.getWriter(),errorResponse);
        } finally {
            MDC.clear();
        }
    }
        /// You should only trust the X-Forwarded-For header if your proxy sets it. Otherwise, malicious users could spoof it by adding their own headers.
        /// if they use proxies server like reverse proxy or forward proxy or load balancer it create new ip
        /// first ip is original ip
        /// X-Forwarded-For: 203.0.113.1, 198.51.100.2, 10.0.0.3
        private String getClientIp(HttpServletRequest request) {
            String xfHeader = request.getHeader("X-Forwarded-For");  // ✅ Fixed
            if (xfHeader != null && !xfHeader.isEmpty()) {
                return xfHeader.split(",")[0].trim();  // First IP is the original client
            }
            return request.getRemoteAddr();  // Fallback to the IP seen by the server
        }

    }
