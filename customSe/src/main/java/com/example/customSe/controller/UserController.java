package com.example.customSe.controller;

import com.example.customSe.entity.Role;
import com.example.customSe.entity.User;
import com.example.customSe.payload.LoginDto;
import com.example.customSe.payload.SignupDto;
import com.example.customSe.payload.request.OtpRequest;
import com.example.customSe.repository.RoleRepository;
import com.example.customSe.repository.UserRepository;
import com.example.customSe.security.AuthProvider.ExtendedUsernamePasswordAuthenticationToken;
import com.example.customSe.security.JwtTokenProvider;
import com.example.customSe.security.authentication.OTPAuthentication;
import com.example.customSe.service.CustomUserDetailsService;
import com.example.customSe.utils.GoogleAuthenticatorService;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.time.Duration;
import java.util.*;

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final RoleRepository roleRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;
    private final GoogleAuthenticatorService googleAuthenticatorService;

    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, RoleRepository roleRepository, JwtTokenProvider jwtTokenProvider, CustomUserDetailsService userDetailsService, GoogleAuthenticatorService googleAuthenticatorService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.roleRepository = roleRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
        this.googleAuthenticatorService = googleAuthenticatorService;
    }

    @Value("${otp.channel}")
    private String channel;

    @PostMapping("/signin")
    public ResponseEntity<String> authenticateUser(@RequestBody LoginDto loginDto, HttpServletRequest request) {
        try {
//            Authentication authentication = authenticationManager.authenticate(
//                    new UsernamePasswordAuthenticationToken(
//                            loginDto.getUsernameOrEmail(),
//                            loginDto.getPassword()
//                    )
//            );
            Authentication authentication = authenticationManager.authenticate(
                    new ExtendedUsernamePasswordAuthenticationToken(
                            loginDto.getUsernameOrEmail(),
                            loginDto.getPassword(),
                            channel// <- passed in from client
                    )
            );

            MDC.put("requestID", UUID.randomUUID().toString());
            if (authentication != null) {
                // Check if OTP is required - for example, you might check if the authorities contain ROLE_OTP
                boolean otpRequired = authentication.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_OTP"));

                HttpSession session = request.getSession(true);

                if (otpRequired) {
                    // Password OK, OTP step pending
                    // Save username and auth details in session for OTP verification step
                    session.setAttribute("OTP_PENDING_USER", loginDto.getUsernameOrEmail());
                    // Save partial authentication but NOT full auth context yet
                    session.setAttribute("PARTIAL_AUTHENTICATION", authentication);


                    return ResponseEntity.ok("OTP_REQUIRED"); // Client should prompt for OTP input
                } else {
                    // Full authentication done (no OTP needed)
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

                    MDC.put("User", loginDto.getUsernameOrEmail());
                    return ResponseEntity.ok("User authenticated and session created");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            MDC.clear();
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication failed");
    }

    //@PreAuthorize("hasAuthority('ROLE_OTP')")
    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(@RequestBody OtpRequest otpRequest, HttpServletRequest request) {
        try {
            // Retrieve session data
            HttpSession session = request.getSession(false);
            if (session == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Session expired. Please login again.");
            }

            String sessionUsername = (String) session.getAttribute("OTP_PENDING_USER");
            Authentication partialAuth = (Authentication) session.getAttribute("PARTIAL_AUTHENTICATION");

            if (sessionUsername == null || partialAuth == null || !sessionUsername.equals(otpRequest.getUsername())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired OTP session");
            }
            Object principalObj = partialAuth.getPrincipal();
            if (!(principalObj instanceof Serializable principal)) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Principal is not serializable");
            }
            UserDetails userDetails = userDetailsService.loadUserByUsername(sessionUsername);
            // Construct OTPAuthentication token
            OTPAuthentication otpAuthentication = new OTPAuthentication(
                    userDetails,                       // principal (MyUserDetails or username)
                    otpRequest.getOtp(),                              // credentials (the OTP)
                    userDetails.getAuthorities(), // or fetch real roles
                    false,                                             // isAuthenticated
                    channel,                                           // or dynamic if needed
                    request.getHeader("Device-Id"),
                    request.getRemoteAddr(),
                    request.getHeader("User-Agent"),
                    System.currentTimeMillis()
            );

            // Perform authentication
            Authentication result = authenticationManager.authenticate(otpAuthentication);

            if (result.isAuthenticated()) {
                // Set full authentication in context
                SecurityContextHolder.getContext().setAuthentication(result);
                session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

                // Clean up
                session.removeAttribute("OTP_PENDING_USER");
                session.removeAttribute("PARTIAL_AUTHENTICATION");

                return ResponseEntity.ok("OTP verified. User fully authenticated.");
            }

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("OTP verification failed");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("OTP verification failed: " + e.getMessage());
        }
    }


//    @PostMapping("/signin")
//    public ResponseEntity<String> authenticateUser(@RequestBody LoginDto loginDto, HttpServletRequest request, HttpServletResponse response) {
//        try {
//
//            Authentication authentication = authenticationManager.authenticate(
//                    new UsernamePasswordAuthenticationToken(
//                            loginDto.getUsernameOrEmail(),
//                            loginDto.getPassword()
//                    )
//            );
//            MDC.put("requestID", UUID.randomUUID().toString());
//            if (authentication.isAuthenticated()) {
//                SecurityContextHolder.getContext().setAuthentication(authentication);
//                String aceestoken = jwtTokenProvider.generateToken(authentication);
//                HttpSession session = request.getSession(true);
//                session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
//                MDC.put("User", loginDto.getUsernameOrEmail());
//                ResponseCookie responseCookie = ResponseCookie.from("aceestoken", aceestoken)
//                        .httpOnly(true)
//                        .maxAge(Duration.ofMinutes(2))
//                        .path("/")
//                        .build();
//                response.addHeader(HttpHeaders.SET_COOKIE, responseCookie.toString());
//                log.info("abhiii");
//                return ResponseEntity.ok("User authenticated and session created");
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            MDC.clear();
//        }
//        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication failed");
//
//    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody SignupDto signupDto) throws Exception {

        User user = new User();
        user.setName(signupDto.getName());
        user.setUsername(signupDto.getUsername());
        user.setEmail(signupDto.getEmail());
        user.setPassword(passwordEncoder.encode(signupDto.getPassword()));
        Role roles = roleRepository.findByName("ROLE_ADMIN").get();
        user.setRoles(Collections.singleton(roles));
        GoogleAuthenticatorKey key = googleAuthenticatorService.generateSecret();
        user.setSecretKey(key.getKey());
        userRepository.save(user);
        String otpUrl = googleAuthenticatorService.getOtpAuthUrl(user.getUsername(), "MODUS INFORMATION SYSTEMS", user.getSecretKey());
        String qrCode=googleAuthenticatorService.generateQRCodeBase64(otpUrl);
        Map<String ,Object> response=new HashMap<>();
        response.put("message","User created");
        response.put("qrCode",qrCode);
        return ResponseEntity.ok(response);
//        return new ResponseEntity<>("User is created", HttpStatus.CREATED);
    }

    @GetMapping("/ad")
    public String getRoles(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            return "No authentication info found in session";
        }
        HttpSession session = request.getSession(false);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("Session ID: " + (session != null ? session.getId() : "No session") +
                ", Authenticated: " + (auth != null && auth.isAuthenticated() ? auth.getName() : "Anonymous"));
        if (session != null) {
            Enumeration<String> attrs = session.getAttributeNames();
            while (attrs.hasMoreElements()) {
                String attr = attrs.nextElement();
                System.out.println("Session attribute: " + attr + " = " + session.getAttribute(attr));
            }
        }

        // Get roles from authorities
        String roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .reduce("", (acc, role) -> acc + role + " ");
        System.out.println(roles.trim());
        return "User roles: " + roles.trim();
    }
}
