package com.nhnacademy.accountapi.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "members")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @Setter(AccessLevel.NONE)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(name = "user_login_id", length = 50, nullable = false, unique = true)
    private String loginId;

    @Column(name = "user_password", length = 200, nullable = false)
    private String password;

    @Column(name = "user_email", length = 100, nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_role", nullable = false)
    private UserRole role;

    @Column(name = "user_name", length = 50, nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_status", nullable = false)
    private UserStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime lastLoginAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.status = UserStatus.ACTIVE;
    }

    @Builder
    public User(String loginId, String email, String password, String name) {
        this.loginId = loginId;
        this.email = email;
        this.password = password;
        this.name = name;
        this.role = UserRole.NORMAL;
    }

    public void updateLoginAt() {
        lastLoginAt = LocalDateTime.now();
    }
}
