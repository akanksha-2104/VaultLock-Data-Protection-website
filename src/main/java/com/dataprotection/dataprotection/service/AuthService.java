package com.dataprotection.dataprotection.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dataprotection.dataprotection.dto.auth.AuthResponse;
import com.dataprotection.dataprotection.dto.auth.LoginContext;
import com.dataprotection.dataprotection.dto.auth.LoginRequest;
import com.dataprotection.dataprotection.dto.auth.RegisterRequest;
import com.dataprotection.dataprotection.entity.LoginLocation;
import com.dataprotection.dataprotection.entity.User;
import com.dataprotection.dataprotection.enums.AuditAction;
import com.dataprotection.dataprotection.enums.Role;
import com.dataprotection.dataprotection.exception.DuplicateResourceException;
import com.dataprotection.dataprotection.exception.LoginAttemptFailedException;
import com.dataprotection.dataprotection.exception.SuspiciousLoginAttemptException;
import com.dataprotection.dataprotection.repository.UserRepository;
import com.dataprotection.dataprotection.util.GeoLocationUtil;
import com.dataprotection.dataprotection.util.JwtUtil;


@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final AuditLogService auditLogService;
    private final LoginAttemptService loginAttemptService;
    private final LoginLocationService loginLocationService;
    private final GeoLocationUtil geoLocationUtil;
    private final JwtUtil jwtUtil;                          // NEW

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       AuditLogService auditLogService,
                       LoginAttemptService loginAttemptService,
                       LoginLocationService loginLocationService,
                       GeoLocationUtil geoLocationUtil,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.auditLogService = auditLogService;
        this.loginAttemptService = loginAttemptService;
        this.loginLocationService = loginLocationService;
        this.geoLocationUtil = geoLocationUtil;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request, String ipAddress) {
        String normalizedEmail = request.email().trim().toLowerCase();
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateResourceException("Email is already registered");
        }

        User user = new User();
        user.setName(request.name().trim());
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.USER);    // default role — admins must be set manually in DB

        User savedUser = userRepository.save(user);
        auditLogService.log(savedUser, AuditAction.REGISTER, null, ipAddress);

        return new AuthResponse(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getRole().name(),
                "User registered successfully",
                null, null, false,
                null);              // no token on register — user must log in
    }

    @Transactional
    public AuthResponse login(LoginRequest request, LoginContext loginContext) {
        String normalizedEmail = request.email().trim().toLowerCase();
        String ipAddress = loginContext.ipAddress();
        String loginAttemptKey = normalizedEmail + "|" + ipAddress;

        // Check if this key should be sent to the decoy
        if (loginAttemptService.shouldRedirectToFakeUi(loginAttemptKey)) {
            userRepository.findByEmail(normalizedEmail)
                    .ifPresent(u -> auditLogService.log(u, AuditAction.DECOY_REDIRECT, null, ipAddress));
            throw new SuspiciousLoginAttemptException(
                    "Too many failed login attempts. Redirecting to secure decoy.");
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(normalizedEmail, request.password()));
        } catch (BadCredentialsException e) {
            int failedAttempts = loginAttemptService.recordFailure(loginAttemptKey);
            int remaining = loginAttemptService.getRemainingAttempts(failedAttempts);

            userRepository.findByEmail(normalizedEmail)
                    .ifPresent(u -> auditLogService.log(u, AuditAction.LOGIN_FAILED, null, ipAddress));

            if (remaining == 0) {
                userRepository.findByEmail(normalizedEmail)
                        .ifPresent(u -> auditLogService.log(u, AuditAction.DECOY_REDIRECT, null, ipAddress));
                throw new SuspiciousLoginAttemptException(
                        "Too many failed login attempts. Redirecting to secure decoy.");
            }
            throw new LoginAttemptFailedException(
                    "Invalid email or password. " + remaining + " attempts remaining.", remaining);
        }

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        loginAttemptService.reset(loginAttemptKey);
        auditLogService.log(user, AuditAction.LOGIN, null, ipAddress);

        // Location tracking
        String deviceInfo = loginContext.deviceInformation();
        String location = geoLocationUtil.getLocationFromIp(ipAddress);
        LoginLocation lastLogin = loginLocationService.getLastLoginLocation(user).orElse(null);
        String previousLocation = lastLogin == null ? null : lastLogin.getLocation();
        boolean newLocationDetected = previousLocation != null
                && !location.equalsIgnoreCase(previousLocation);

        loginLocationService.saveLoginLocation(
                user, ipAddress, location, deviceInfo, previousLocation, newLocationDetected);

        // STEP 1: Generate JWT token — includes email + role
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        return new AuthResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name(),
                newLocationDetected
                        ? "New login detected from a different location."
                        : "Login successful.",
                location,
                previousLocation,
                newLocationDetected,
                token);             // JWT token returned to frontend
    }

    @Transactional(readOnly = true)
    public AuthResponse getCurrentUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return new AuthResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name(),
                "Session restored",
                null, null, false,
                null);  // don't re-issue token here — frontend already has it
    }
}