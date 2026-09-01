package com.nhnacademy.accountapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.accountapi.config.OwnerRoleInterceptor;
import com.nhnacademy.accountapi.config.WebConfig;
import com.nhnacademy.accountapi.dto.AdminCreateRequest;
import com.nhnacademy.accountapi.dto.UserResponse;
import com.nhnacademy.accountapi.service.OwnerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OwnerController.class)
@Import(WebConfig.class)
class OwnerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OwnerService ownerService;

    @MockitoBean
    private OwnerRoleInterceptor ownerRoleInterceptor; // 오너 권한 검증 인터셉터 가짜 빈

    @BeforeEach
    void setUp() throws Exception {
        // 📌 인터셉터가 요청을 무사히 컨트롤러로 통과(true)시키도록 사전 대본 설정
        given(ownerRoleInterceptor.preHandle(any(), any(), any())).willReturn(true);
    }

    // 1. 관리자 생성 POST /api/account/owner/signup

    @Test
    @DisplayName("1. 관리자 생성 성공 - POST /api/account/owner/signup 요청 시 201 Created 응답")
    void createAdmin_Success() throws Exception {
        // given
        AdminCreateRequest request = new AdminCreateRequest("admin1", "password1234", "관리자1");
        doNothing().when(ownerService).createAdmin(any(AdminCreateRequest.class), eq(999L));

        // when & then
        mockMvc.perform(post("/api/account/owner/signup")
                        .header("X-USER-ID", 999L) // 오너 헤더 필수
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(ownerService).createAdmin(any(AdminCreateRequest.class), eq(999L));
    }

    //  2. 관리자 목록 조회 GET /api/account/owner/list

    @Test
    @DisplayName("2. 관리자 목록 조회 성공 - GET /api/account/owner/list 요청 시 200 OK 및 목록 반환")
    void getUsers_Success() throws Exception {
        // given
        UserResponse admin1 = new UserResponse(10L, "admin1", "ADMIN", "admin1@nhn.com", "관리자1", "ACTIVE", LocalDateTime.now());
        given(ownerService.getUsers(999L)).willReturn(List.of(admin1));

        // when & then
        mockMvc.perform(get("/api/account/owner/list")
                        .header("X-USER-ID", 999L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].loginId").value("admin1"))
                .andExpect(jsonPath("$[0].role").value("ADMIN"));
    }

    // 3. 관리자 상세 조회 GET /api/account/owner/{user-id}

    @Test
    @DisplayName("3. 관리자 상세 조회 성공 - GET /api/account/owner/{user-id} 요청 시 200 OK 반환")
    void getUser_Success() throws Exception {
        // given
        UserResponse admin1 = new UserResponse(10L, "admin1", "ADMIN", "admin1@nhn.com", "관리자1", "ACTIVE", LocalDateTime.now());
        given(ownerService.getUser(999L, 10L)).willReturn(admin1);

        // when & then
        mockMvc.perform(get("/api/account/owner/{user-id}", 10L)
                        .header("X-USER-ID", 999L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(10L))
                .andExpect(jsonPath("$.loginId").value("admin1"));
    }

    // 4. 관리자 삭제 PATCH /api/account/owner/{user-id}

    @Test
    @DisplayName("4. 관리자 삭제 성공 - PATCH /api/account/owner/{user-id} 요청 시 204 No Content 반환")
    void deleteUser_Success() throws Exception {
        // given
        doNothing().when(ownerService).withdraw(999L, 10L);

        // when & then
        mockMvc.perform(patch("/api/account/owner/{user-id}", 10L)
                        .header("X-USER-ID", 999L))
                .andExpect(status().isNoContent());
    }

    // 5. 관리자 복구 PATCH /api/account/owner/{user-id}/restore

    @Test
    @DisplayName("5. 관리자 계정 복구 성공 - PATCH /api/account/owner/{user-id}/restore 요청 시 204 No Content 반환")
    void restoreUser_Success() throws Exception {
        // given
        doNothing().when(ownerService).restore(999L, 10L);

        // when & then
        mockMvc.perform(patch("/api/account/owner/{user-id}/restore", 10L)
                        .header("X-USER-ID", 999L))
                .andExpect(status().isNoContent());
    }

    // 6. 관리자 비번 초기화 PATCH /api/account/owner/{user-id}/reset-password

    @Test
    @DisplayName("6. 관리자 비밀번호 초기화 성공 - PATCH /api/account/owner/{user-id}/reset-password 요청 시 204 No Content 반환")
    void resetPassword_Success() throws Exception {
        // given
        doNothing().when(ownerService).resetPassword(999L, 10L);

        // when & then
        mockMvc.perform(patch("/api/account/owner/{user-id}/reset-password", 10L)
                        .header("X-USER-ID", 999L))
                .andExpect(status().isNoContent());
    }
}