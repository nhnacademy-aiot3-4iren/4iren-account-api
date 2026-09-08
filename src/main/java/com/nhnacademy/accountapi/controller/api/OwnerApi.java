package com.nhnacademy.accountapi.controller.api;

import com.nhnacademy.accountapi.dto.AdminCreateRequest;
import com.nhnacademy.accountapi.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import java.util.List;

@Tag(name = "Owner API", description = "시스템 관리자(Owner) 전용 API")
public interface OwnerApi {

    @Operation(summary = "관리자 계정 생성", description = "Owner 권한으로 새로운 관리자(Admin) 계정을 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "관리자 계정 생성 성공"),
            @ApiResponse(responseCode = "400", description = "입력값 유효성 검사 실패"),
            @ApiResponse(responseCode = "403", description = "Owner 권한이 아님"),
            @ApiResponse(responseCode = "409", description = "이미 존재하는 아이디 또는 이메일")
    })
    ResponseEntity<Void> signUp(
            @Parameter(hidden = true) Long requesterId,
            AdminCreateRequest request
    );

    @Operation(summary = "관리자 목록 조회", description = "모든 관리자 계정 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "Owner 권한이 아님")
    })
    ResponseEntity<List<UserResponse>> getUsers(
            @Parameter(hidden = true) Long requesterId
    );

    @Operation(summary = "관리자 상세 조회", description = "특정 관리자의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "Owner 권한이 아님"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 사용자")
    })
    ResponseEntity<UserResponse> getUser(
            @Parameter(hidden = true) Long requesterId,
            @Parameter(description = "조회할 관리자 ID") Long userId
    );

    @Operation(summary = "관리자 계정 삭제", description = "특정 관리자 계정을 비활성화(탈퇴 처리)합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "삭제 완료"),
            @ApiResponse(responseCode = "403", description = "Owner 권한이 아님"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 사용자")
    })
    ResponseEntity<Void> deleteUser(
            @Parameter(hidden = true) Long requesterId,
            @Parameter(description = "삭제할 관리자 ID") Long userId
    );

    @Operation(summary = "관리자 계정 복구", description = "삭제된 관리자 계정을 다시 활성화합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "복구 완료"),
            @ApiResponse(responseCode = "403", description = "Owner 권한이 아님"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 사용자")
    })
    ResponseEntity<Void> restoreUser(
            @Parameter(hidden = true) Long requesterId,
            @Parameter(description = "복구할 관리자 ID") Long userId
    );

    @Operation(summary = "관리자 비밀번호 초기화", description = "특정 관리자의 비밀번호를 초기화합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "비밀번호 초기화 완료"),
            @ApiResponse(responseCode = "403", description = "Owner 권한이 아님"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 사용자")
    })
    ResponseEntity<Void> resetPassword(
            @Parameter(hidden = true) Long requesterId,
            @Parameter(description = "비밀번호를 초기화할 관리자 ID") Long userId
    );
}
