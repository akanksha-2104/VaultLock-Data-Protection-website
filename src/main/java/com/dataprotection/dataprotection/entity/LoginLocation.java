package com.dataprotection.dataprotection.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "login_locations")
public class LoginLocation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "login_location_seq")
    @SequenceGenerator(name = "login_location_seq", sequenceName = "login_location_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 45)
    private String ipAddress;

    @Column(nullable = false, length = 255)
    private String location;

    @Column(length = 255)
    private String deviceInfo;

    @Column(nullable = false)
    private LocalDateTime loginTime;

    @Column(length = 255)
    private String previousLocation;

    @Column(nullable = false)
    private boolean newLocationDetected;

    public LoginLocation() {}

    public LoginLocation(User user, String ipAddress, String location, String deviceInfo, LocalDateTime loginTime) {
        this.user = user;
        this.ipAddress = ipAddress;
        this.location = location;
        this.deviceInfo = deviceInfo;
        this.loginTime = loginTime;
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getDeviceInfo() { return deviceInfo; }
    public void setDeviceInfo(String deviceInfo) { this.deviceInfo = deviceInfo; }
    public LocalDateTime getLoginTime() { return loginTime; }
    public void setLoginTime(LocalDateTime loginTime) { this.loginTime = loginTime; }
    public String getPreviousLocation() { return previousLocation; }
    public void setPreviousLocation(String previousLocation) { this.previousLocation = previousLocation; }
    public boolean isNewLocationDetected() { return newLocationDetected; }
    public void setNewLocationDetected(boolean newLocationDetected) { this.newLocationDetected = newLocationDetected; }
}
