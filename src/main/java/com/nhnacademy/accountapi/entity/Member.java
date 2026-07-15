package com.nhnacademy.accountapi.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name="members")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column( length = 50, nullable = false, unique = true)
    private String userLoginId;

    @Column(length = 50, nullable = false,unique = true)
    private String userPassword;

    @Column(length = 100, nullable = false, unique = true)
    private String userEmail;

    @Column(length =20, nullable = false)
    private String userRole;

    @Column(length = 50, nullable = false)
    private String userName;

    @Enumerated(EnumType.STRING)
    @Column(length=10, nullable = false)
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

    @PreUpdate
    protected void onUpdate(){
        lastLoginAt=LocalDateTime.now();
    }

}
