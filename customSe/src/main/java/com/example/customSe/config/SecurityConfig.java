package com.example.customSe.config;

import ch.qos.logback.classic.helpers.MDCInsertingServletFilter;
import com.example.customSe.config.properties.AppSessionProperties;
import com.example.customSe.repository.UserRepository;
import com.example.customSe.security.AuthProvider.OTPAuthenticationProvider;
import com.example.customSe.security.AuthProvider.PasswordAuthenticationProvider;
import com.example.customSe.security.auth.CustomAccessDeniedHandler;
import com.example.customSe.security.auth.CustomAuthenticationEntryPoint;
//import com.example.customSe.security.filter.RateLimitingFilter;
import com.example.customSe.security.manager.CustomAuthenticationManager;
import com.example.customSe.service.CustomUserDetailsService;
import com.example.customSe.service.OtpService;
import com.example.customSe.utils.GoogleAuthenticatorService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;
import org.springframework.security.web.header.writers.CompositeHeaderWriter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
//@EnableConfigurationProperties(AppSessionProperties.class)

public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final OtpService otpService;
    private final GoogleAuthenticatorService googleAuthenticatorService;
    private final UserRepository userRepository;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final AppSessionProperties sessionProperties;

    public SecurityConfig(CustomUserDetailsService userDetailsService, OtpService otpService, GoogleAuthenticatorService googleAuthenticatorService, UserRepository userRepository, CustomAccessDeniedHandler customAccessDeniedHandler, CustomAuthenticationEntryPoint customAuthenticationEntryPoint, AppSessionProperties sessionProperties) {
        this.userDetailsService = userDetailsService;
        this.otpService = otpService;
        this.googleAuthenticatorService = googleAuthenticatorService;
        this.userRepository = userRepository;
        this.customAccessDeniedHandler = customAccessDeniedHandler;
        this.customAuthenticationEntryPoint = customAuthenticationEntryPoint;
        this.sessionProperties = sessionProperties;
    }

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;


    //    @Bean
//    public ServletContextInitializer servletContextInitializer() {
//        return servletContext -> {
//            SessionCookieConfig sessionCookieConfig = servletContext.getSessionCookieConfig();
//            sessionCookieConfig.setHttpOnly(true);
//            sessionCookieConfig.setSecure(false);  // Set true if you use HTTPS
//            sessionCookieConfig.setPath("/");
//            // Can't set SameSite directly in standard API, might need to customize response headers or use a filter
//        };
//    }
    @Bean
    public FilterRegistrationBean<MDCInsertingServletFilter> mdcFilter() {
        FilterRegistrationBean<MDCInsertingServletFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new MDCInsertingServletFilter());
        registration.addUrlPatterns("/*");
        registration.setOrder(1); // Ensure it runs early
        return registration;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authenticationManager(authenticationManager());  // Inject your custom manager
        return http
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
                    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                    config.setAllowCredentials(true);
                    config.setAllowedHeaders(List.of("*"));
                    return config;
                }))
//                .sessionManagement(session -> session
//                        .maximumSessions(1) // only 1 session per user
//                )
                .csrf(csrf->csrf.disable())
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers("/api/roles/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/signup", "/api/auth/signin").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/verify-otp").permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> exception
                        .accessDeniedHandler(customAccessDeniedHandler)
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                )

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.valueOf(sessionProperties.getCreationPolicy()))
                        .sessionFixation().migrateSession()
                        .maximumSessions(sessionProperties.getMaxSessions())
                        .maxSessionsPreventsLogin(sessionProperties.isPreventNewLogin())
                )
                .build();

    }

    //    @Bean
//    public AuthenticationProvider authenticationProvider() {
//
//        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
//        provider.setUserDetailsService(userDetailsService);
//        provider.setPasswordEncoder(passwordEncoder());
//        return provider;
//
//    }
    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        DaoAuthenticationProvider daoProvider = new DaoAuthenticationProvider(userDetailsService);
        daoProvider.setPasswordEncoder(passwordEncoder());
        PasswordAuthenticationProvider passwordAuthProvider =
                new PasswordAuthenticationProvider(daoProvider, otpService);
        OTPAuthenticationProvider otpAuthProvider =
                new OTPAuthenticationProvider(otpService, googleAuthenticatorService, userDetailsService, userRepository);
        // Your custom manager that connects both
        return new CustomAuthenticationManager(passwordAuthProvider, otpAuthProvider);
    }
//    @Bean
//    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
//        return authConfig.getAuthenticationManager();
//    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();

    }
//    @Bean
//    public FilterRegistrationBean<RateLimitingFilter> rateLimitFilter() {
//        FilterRegistrationBean<RateLimitingFilter> registration = new FilterRegistrationBean<>();
//        registration.setFilter(new RateLimitingFilter());
//
//        // Apply only to login and OTP endpoints
//        registration.addUrlPatterns("/api/auth/signin", "/api/auth/verify-otp");
//
//        // Optional: Adjust execution order relative to other filters
//        registration.setOrder(2);
//
//        return registration;
//    }


}

