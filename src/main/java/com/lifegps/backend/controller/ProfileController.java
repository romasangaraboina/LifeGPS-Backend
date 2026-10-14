
package com.lifegps.backend.controller;

import com.lifegps.backend.entity.User;
import com.lifegps.backend.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final UserRepository userRepository;

    public ProfileController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<?> getProfile(Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Please log in first.");
        }

        User user = userRepository.findByEmail(authentication.getName())
                .orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("User not found.");
        }

        ProfileResponse response = new ProfileResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setAccountStatus("Active");
        response.setDeadlineNotifications(user.getDeadlineNotifications());
        response.setExpiryAlerts(user.getExpiryAlerts());

        return ResponseEntity.ok(response);
    }

    @PutMapping
    public ResponseEntity<?> updateProfile(
            @RequestBody UpdateRequest request,
            Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Please log in first.");
        }
        User user = userRepository.findByEmail(authentication.getName()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }
        if (request.name() == null || request.name().isBlank()) {
            return ResponseEntity.badRequest().body("Name is required.");
        }
        if (request.name().trim().length() > 100) {
            return ResponseEntity.badRequest().body("Name must be 100 characters or fewer.");
        }
        user.setName(request.name().trim());
        user.setPhone(request.phone() == null ? null : request.phone().trim());
        if (request.deadlineNotifications() != null) user.setDeadlineNotifications(request.deadlineNotifications());
        if (request.expiryAlerts() != null) user.setExpiryAlerts(request.expiryAlerts());
        userRepository.save(user);

        ProfileResponse response = new ProfileResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setAccountStatus("Active");
        response.setDeadlineNotifications(user.getDeadlineNotifications());
        response.setExpiryAlerts(user.getExpiryAlerts());
        return ResponseEntity.ok(response);
    }

    public record UpdateRequest(String name, String phone, Boolean deadlineNotifications, Boolean expiryAlerts) {}

    public static class ProfileResponse {

        private Long id;
        private String name;
        private String email;
        private String phone;
        private String accountStatus;
        private Boolean deadlineNotifications;
        private Boolean expiryAlerts;

        public ProfileResponse() {
        }

        public Boolean getDeadlineNotifications() { return deadlineNotifications; }
        public void setDeadlineNotifications(Boolean v) { this.deadlineNotifications = v; }
        public Boolean getExpiryAlerts() { return expiryAlerts; }
        public void setExpiryAlerts(Boolean v) { this.expiryAlerts = v; }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getAccountStatus() {
            return accountStatus;
        }

        public void setAccountStatus(String accountStatus) {
            this.accountStatus = accountStatus;
        }
    }
}