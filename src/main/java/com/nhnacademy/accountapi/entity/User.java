package com.nhnacademy.accountapi.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity @Table(name="members")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id @Setter(AccessLevel.NONE)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column( length = 50, nullable = false, unique = true)
    private String userLoginId;

    @Column(length=200, nullable = false)
    private String userPassword;

    @Column(length = 100, nullable = false, unique = true)
    private String userEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole userRole;

    @Column(length = 50, nullable = false)
    private String userName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus userStatus;

    @Column( nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime lastLoginAt;

    @PrePersist
    protected void onCreate(){
        this.createdAt=LocalDateTime.now();
        this.userStatus= UserStatus.ACTIVE;
    }

    @Builder
    public User(String loginId, String email, String password, String name) {
        this.userLoginId=loginId;
        this.userEmail=email;
        this.userPassword=password;
        this.userName=name;
        this.userRole=UserRole.USER;
    }

    public void updateLoginAt() {
        lastLoginAt=LocalDateTime.now();
    }
}
