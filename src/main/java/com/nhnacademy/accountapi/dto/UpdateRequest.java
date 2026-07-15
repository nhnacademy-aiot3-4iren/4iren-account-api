package com.nhnacademy.accountapi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
//회원 정보 수정 요청
//아이디, 비번은 보안 이유로 제외
//이름, 이메일만 수정가능
public class UpdateRequest {
    @NotBlank
    private String UserName;

    @Email(message = "이메일 형식이 올바르지 않습니다")
    private String UserEmail;



}
