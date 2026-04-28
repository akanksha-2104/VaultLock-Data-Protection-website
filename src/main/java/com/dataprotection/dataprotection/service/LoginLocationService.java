package com.dataprotection.dataprotection.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dataprotection.dataprotection.dto.login.LoginLocationResponse;
import com.dataprotection.dataprotection.entity.LoginLocation;
import com.dataprotection.dataprotection.entity.User;
import com.dataprotection.dataprotection.exception.ResourceNotFoundException;
import com.dataprotection.dataprotection.repository.LoginLocationRepository;
import com.dataprotection.dataprotection.repository.UserRepository;

@Service
public class LoginLocationService {
    private final LoginLocationRepository loginLocationRepository;
    private final UserRepository userRepository;

    public LoginLocationService(LoginLocationRepository loginLocationRepository, UserRepository userRepository) {
        this.loginLocationRepository = loginLocationRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public LoginLocation saveLoginLocation(User user, String ipAddress, String location, String deviceInfo,
            String previousLocation, boolean newLocationDetected) {
        LoginLocation loginLocation = new LoginLocation(user, ipAddress, location, deviceInfo, LocalDateTime.now());
        loginLocation.setPreviousLocation(previousLocation);
        loginLocation.setNewLocationDetected(newLocationDetected);
        return loginLocationRepository.save(loginLocation);
    }

    public Optional<LoginLocation> getLastLoginLocation(User user) {
        return loginLocationRepository.findTopByUserOrderByLoginTimeDesc(user);
    }

    @Transactional(readOnly = true)
    public List<LoginLocationResponse> listCurrentUserLocations(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return loginLocationRepository.findAllByUserOrderByLoginTimeDesc(user).stream()
                .map(location -> new LoginLocationResponse(
                        location.getId(),
                        location.getIpAddress(),
                        location.getLocation(),
                        location.getDeviceInfo(),
                        location.getLoginTime(),
                        location.getPreviousLocation(),
                        location.isNewLocationDetected()))
                .toList();
    }
}
