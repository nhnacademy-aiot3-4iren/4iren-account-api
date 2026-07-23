package com.nhnacademy.accountapi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// 회원가입 요청 DTO
public record RegisterRequest (
    @NotBlank(message = "아이디는 필수입니다")
    String userLoginId,

    @NotBlank(message = "이메일은 필수입니다")
    @Email
    String userEmail,

    @NotBlank(message = "비밀번호는 필수입니다")
    String userPassword,

    @NotBlank(message = "이름은 필수입니다")
    String userName
) {}
