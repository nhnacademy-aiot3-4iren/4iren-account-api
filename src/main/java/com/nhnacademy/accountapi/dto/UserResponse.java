package com.nhnacademy.accountapi.dto;

import java.time.LocalDateTime;

// 회원 정보 응답 DTO
public record UserResponse (
        Long userId,
        String userLoginId,
        String userEmail,
        String userName,
        String userStatus,
        LocalDateTime createdAt
) {}
