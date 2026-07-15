package com.nhnacademy.accountapi.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

//회원정보 응답
//회원 정보 조회시 반환
@Getter
@AllArgsConstructor
public class Response {
    private Long userId;
    private String userLoginId;
    private String userEmail;
    private String userName;
    private String userStatus;
    private LocalDateTime createdAt;

}
