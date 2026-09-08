package com.nhnacademy.accountapi.controller.api;

import com.nhnacademy.accountapi.dto.internal.UserRoleResponse;
import com.nhnacademy.accountapi.dto.internal.UserStatusBatchRequest;
import com.nhnacademy.accountapi.dto.internal.UserStatusResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import java.util.List;

@Tag(name = "Internal User API", description = "내부 서버 간 통신을 위한 사용자 정보 API")
public interface InternalUserApi {

    @Operation(summary = "사용자 권한 조회", description = "사용자의 현재 역할(Role)을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 사용자")
    })
    ResponseEntity<UserRoleResponse> getUserRole(
            @Parameter(description = "사용자 ID") Long userId
    );

    @Operation(summary = "사용자 상태 조회", description = "사용자의 현재 상태(ACTIVE, DORMANT, WITHDRAWN 등)를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 사용자")
    })
    ResponseEntity<UserStatusResponse> getUserStatus(
            @Parameter(description = "사용자 ID") Long userId
    );

    @Operation(summary = "다수 사용자 상태 배치 조회", description = "여러 사용자의 상태를 한 번에 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "입력값 검증 실패 (빈 목록 등)")
    })
    ResponseEntity<List<UserStatusResponse>> getUserStatuses(
            UserStatusBatchRequest request
    );
}
