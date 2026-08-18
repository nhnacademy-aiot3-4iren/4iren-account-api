package com.nhnacademy.accountapi.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
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

    @Column(name = "user_email", length = 100, nullable = true, unique = true)
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

    @Column(nullable = true)
    private Long createdBy;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.status = UserStatus.ACTIVE;
    }

    @Builder
    public User(String loginId, String password, String email, UserRole role, String name, Long createdBy) {
        this.loginId = loginId;
        this.password = password;
        this.email = email;
        this.role = role;
        this.name = name;
        this.createdBy = createdBy;
    }

    // 일반 사용자용 회원가입
    public static User createNormalUser(String loginId, String email, String password, String name) {
        return User.builder()
                .loginId(loginId)
                .email(email)
                .password(password)
                .name(name)
                .role(UserRole.NORMAL)
                .createdBy(null)
                .build();
    }

    // 관리자용 회원가입
    public static User createAdminUser(String loginId, String password, String name, Long createdBy) {
        return User.builder()
                .loginId(loginId)
                .password(password)
                .email(null)
                .role(UserRole.ADMIN)
                .name(name)
                .createdBy(createdBy)
                .build();
    }

    public void updateLoginAt() {
        lastLoginAt = LocalDateTime.now();
    }
}
