package com.nhnacademy.accountapi.dto;
//회원가입 요청

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//회원가입 요청
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    @NotBlank(message = "아이디는 필수입니다")
    private String userLoginId;

    @NotBlank(message = "비밀번호는 필수입니다")
    @Email
    private String userEmail;

    @NotBlank(message = "비밀번호는 필수입니다")
    private String userPassword;

    @NotBlank(message = "이름은 필수입니다")
    private String userName;



}
