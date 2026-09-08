package com.nhnacademy.accountapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.accountapi.config.OwnerRoleInterceptor;
import com.nhnacademy.accountapi.config.WebConfig;
import com.nhnacademy.accountapi.dto.internal.UserRoleResponse;
import com.nhnacademy.accountapi.dto.internal.UserStatusBatchRequest;
import com.nhnacademy.accountapi.dto.internal.UserStatusResponse;
import com.nhnacademy.accountapi.entity.UserRole;
import com.nhnacademy.accountapi.entity.UserStatus;
import com.nhnacademy.accountapi.service.InternalUserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InternalUserController.class)
@Import(WebConfig.class) // /api/account 경로 접두어 설정 적용
class InternalUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private InternalUserService internalUserService;

    @MockitoBean
    private OwnerRoleInterceptor ownerRoleInterceptor;

    //  1. 권한 조회 GET /api/account/internal/users/{user-id}/role

    @Test
    @DisplayName("1. 내부 유저 권한 조회 성공 - GET /api/account/internal/users/{user-id}/role 요청 시 200 OK 및 UserRoleResponse 반환")
    void getUserRole_Success() throws Exception {
        // given [준비]
        UserRoleResponse response = new UserRoleResponse(1L, UserRole.NORMAL);
        given(internalUserService.getUserRole(1L)).willReturn(response);

        // when & then [실행 및 검증]
        mockMvc.perform(get("/api/account/internal/users/{user-id}/role", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.role").value("NORMAL"));
    }

    //2. 상태 단건 조회 GET /api/account/internal/users/{user-id}/status

    @Test
    @DisplayName("2. 내부 유저 상태 단건 조회 성공 - GET /api/account/internal/users/{user-id}/status 요청 시 200 OK 및 UserStatusResponse 반환")
    void getUserStatus_Success() throws Exception {
        // given [준비]
        UserStatusResponse response = new UserStatusResponse(1L, UserStatus.ACTIVE);
        given(internalUserService.getUserStatus(1L)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/account/internal/users/{user-id}/status", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    //  3. 상태 배치 조회 POST /api/account/internal/users/statuses

    @Test
    @DisplayName("3. 내부 유저 상태 배치 조회 성공 - POST /api/account/internal/users/statuses 요청 시 200 OK 및 목록 반환")
    void getUserStatuses_Success() throws Exception {
        // given [준비]
        UserStatusBatchRequest request = new UserStatusBatchRequest(List.of(1L, 2L));
        List<UserStatusResponse> responses = List.of(
                new UserStatusResponse(1L, UserStatus.ACTIVE),
                new UserStatusResponse(2L, UserStatus.ACTIVE)
        );

        given(internalUserService.getUserStatuses(any())).willReturn(responses);

        // when & then
        mockMvc.perform(post("/api/account/internal/users/statuses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].userId").value(1L))
                .andExpect(jsonPath("$[1].userId").value(2L));
    }

    @Test
    @DisplayName("4. 내부 유저 상태 배치 조회 실패 - userIds가 null인 경우 400 Bad Request 반환")
    void getUserStatuses_ValidationError_Returns400() throws Exception {
        // given [준비]: userIds가 null인 부적절한 요청 DTO
        UserStatusBatchRequest request = new UserStatusBatchRequest(null);

        // when & then
        mockMvc.perform(post("/api/account/internal/users/statuses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}