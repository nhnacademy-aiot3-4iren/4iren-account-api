package com.nhnacademy.accountapi.dto;

import java.time.LocalDateTime;

// 회원 정보 응답 DTO
public record UserResponse (
        Long userId,
        String loginId,
        String role,
        String email,
        String name,
        String status,
        LocalDateTime createdAt
) {}
