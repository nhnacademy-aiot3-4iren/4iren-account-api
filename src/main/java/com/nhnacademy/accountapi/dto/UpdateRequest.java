package com.nhnacademy.accountapi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// 회원 정보 수정 요청 DTO
public record UpdateRequest (
    @NotBlank
    String userName,

    @Email(message = "이메일 형식이 올바르지 않습니다")
    String userEmail,

    String userPassword
) {}
