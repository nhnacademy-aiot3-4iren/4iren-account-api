package com.nhnacademy.accountapi.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

//로그인 성공 응답
//로그인 성공 시 반환
@Getter
@AllArgsConstructor
public class LoginResponse {
    private Long userId;
    private String userLoginId;
    private String userName;
}
