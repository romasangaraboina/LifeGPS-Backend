package com.lifegps.backend.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    private String phone;

    private Boolean deadlineNotifications = true;

    private Boolean expiryAlerts = true;

    public User() {
    }

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Boolean getDeadlineNotifications() {
        return deadlineNotifications == null ? Boolean.TRUE : deadlineNotifications;
    }

    public void setDeadlineNotifications(Boolean deadlineNotifications) {
        this.deadlineNotifications = deadlineNotifications;
    }

    public Boolean getExpiryAlerts() {
        return expiryAlerts == null ? Boolean.TRUE : expiryAlerts;
    }

    public void setExpiryAlerts(Boolean expiryAlerts) {
        this.expiryAlerts = expiryAlerts;
    }
}
