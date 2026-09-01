package com.nhnacademy.accountapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.accountapi.config.OwnerRoleInterceptor;
import com.nhnacademy.accountapi.config.WebConfig;
import com.nhnacademy.accountapi.dto.RegisterRequest;
import com.nhnacademy.accountapi.dto.ResetPasswordRequest;
import com.nhnacademy.accountapi.dto.UpdateRequest;
import com.nhnacademy.accountapi.dto.UserResponse;
import com.nhnacademy.accountapi.dto.login.LoginRequest;
import com.nhnacademy.accountapi.dto.login.LoginResponse;
import com.nhnacademy.accountapi.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(WebConfig.class) // WebConfig의 /api/account 경로 접두어 설정 적용
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private OwnerRoleInterceptor ownerRoleInterceptor; // WebConfig에 포함된 인터셉터 가짜 빈 주입

    //  1. 회원가입 POST /api/account/signup

    @Test
    @DisplayName("1. 회원가입 성공 - POST /api/account/signup 요청 시 201 Created 응답")
    void signUp_Success() throws Exception {
        // given
        RegisterRequest request = new RegisterRequest("user1", "user1@nhn.com", "pw1234", "홍길동");
        doNothing().when(userService).register(any(RegisterRequest.class));

        // when & then
        mockMvc.perform(post("/api/account/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(userService).register(any(RegisterRequest.class));
    }

    @Test
    @DisplayName("2. 회원가입 실패 - 필수 파라미터(loginId) 누락 시 400 Bad Request 응답")
    void signUp_ValidationError_Returns400() throws Exception {
        // given: loginId가 빈값("")인 유효하지 않은 요청 데이터
        RegisterRequest request = new RegisterRequest("", "user1@nhn.com", "pw1234", "홍길동");

        // when & then
        mockMvc.perform(post("/api/account/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    //  2. 로그인 POST /api/account/login

    @Test
    @DisplayName("3. 로그인 성공 - POST /api/account/login 요청 시 200 OK 및 LoginResponse JSON 반환")
    void login_Success() throws Exception {
        // given
        LoginRequest request = new LoginRequest("user1", "pw1234");
        LoginResponse response = new LoginResponse(1L, "user1", "홍길동", "NORMAL", true);

        given(userService.login(any(LoginRequest.class))).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.loginId").value("user1"))
                .andExpect(jsonPath("$.name").value("홍길동"))
                .andExpect(jsonPath("$.role").value("NORMAL"))
                .andExpect(jsonPath("$.firstLogin").value(true));
    }

    //  3. 회원 수정 PUT /api/account/{user-id}

    @Test
    @DisplayName("4. 회원 정보 수정 성공 - PUT /api/account/{user-id} 요청 시 200 OK 반환")
    void updateUser_Success() throws Exception {
        // given
        UpdateRequest request = new UpdateRequest("newId", "new@nhn.com", "newPw");
        UserResponse response = new UserResponse(1L, "newId", "NORMAL", "new@nhn.com", "홍길동", "ACTIVE", LocalDateTime.now());

        given(userService.updateUser(eq(1L), eq(1L), any(UpdateRequest.class))).willReturn(response);

        // when & then
        mockMvc.perform(put("/api/account/{user-id}", 1L)
                        .header("X-USER-ID", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loginId").value("newId"))
                .andExpect(jsonPath("$.email").value("new@nhn.com"));
    }

    //  4. 회원 상세 조회 GET /api/account/{user-id}

    @Test
    @DisplayName("5. 회원 상세 조회 성공 - GET /api/account/{user-id} 요청 시 200 OK 반환")
    void getUser_Success() throws Exception {
        // given
        UserResponse response = new UserResponse(1L, "user1", "NORMAL", "user1@nhn.com", "홍길동", "ACTIVE", LocalDateTime.now());

        given(userService.getUser(1L, 1L)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/account/{user-id}", 1L)
                        .header("X-USER-ID", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.loginId").value("user1"));
    }

    //5. 회원 탈퇴 PATCH /api/account/{user-id}

    @Test
    @DisplayName("6. 회원 탈퇴 성공 - PATCH /api/account/{user-id} 요청 시 204 No Content 반환")
    void withdraw_Success() throws Exception {
        // given
        doNothing().when(userService).withdraw(1L, 1L);

        // when & then
        mockMvc.perform(patch("/api/account/{user-id}", 1L)
                        .header("X-USER-ID", 1L))
                .andExpect(status().isNoContent());
    }

    // 6. 비밀번호 초기화 POST /api/account/reset-password

    @Test
    @DisplayName("7. 비밀번호 초기화 성공 - POST /api/account/reset-password 요청 시 200 OK 반환")
    void resetPassword_Success() throws Exception {
        // given
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setLoginId("user1");
        request.setEmail("user1@nhn.com");

        doNothing().when(userService).resetPassword(any(ResetPasswordRequest.class));

        // when & then
        mockMvc.perform(post("/api/account/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}