package com.nhnacademy.accountapi.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

//로그인 요청
//아이디랑 비번만 필요, 게이트웨이가 요청 보내면 account-api가 인증처리
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    @NotBlank
    private String userLoginId;

    @NotBlank
    private String userPassword;
}
