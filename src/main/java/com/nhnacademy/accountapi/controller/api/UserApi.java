package com.nhnacademy.accountapi.controller.api;

import com.nhnacademy.accountapi.dto.*;
import com.nhnacademy.accountapi.dto.login.LoginRequest;
import com.nhnacademy.accountapi.dto.login.LoginResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "User API", description = "사용자 계정 및 인증 관련 API")
public interface UserApi {

    @Operation(summary = "회원가입", description = "새로운 일반 사용자 계정을 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "회원가입 성공"),
            @ApiResponse(responseCode = "400", description = "입력값 유효성 검사 실패"),
            @ApiResponse(responseCode = "409", description = "이미 존재하는 아이디 또는 이메일")
    })
    ResponseEntity<Void> signUp(RegisterRequest request);

    @Operation(summary = "로그인", description = "아이디와 비밀번호로 로그인합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "400", description = "입력값 유효성 검사 실패"),
            @ApiResponse(responseCode = "403", description = "로그인할 수 없는 계정 상태"),
            @ApiResponse(responseCode = "404", description = "아이디 또는 비밀번호 불일치")
    })
    ResponseEntity<LoginResponse> login(LoginRequest request);

    @Operation(summary = "사용자 정보 수정", description = "이메일, 비밀번호 등 사용자 정보를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "입력값 유효성 검사 실패"),
            @ApiResponse(responseCode = "403", description = "본인만 수정 가능 (권한 없음)"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 회원"),
            @ApiResponse(responseCode = "409", description = "이미 사용 중인 아이디 또는 이메일")
    })
    ResponseEntity<UserResponse> updateUser(
            @Parameter(description = "수정할 사용자 ID") Long userId,
            UpdateRequest request,
            @Parameter(hidden = true) Long requesterId
    );

    @Operation(summary = "사용자 정보 조회", description = "특정 사용자의 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "조회 권한 없음 (본인 아님)"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 회원")
    })
    ResponseEntity<UserResponse> getUser(
            @Parameter(description = "조회할 사용자 ID") Long userId,
            @Parameter(hidden = true) Long requesterId
    );

    @Operation(summary = "회원 탈퇴", description = "사용자의 상태를 탈퇴 상태로 변경합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "탈퇴 처리 완료"),
            @ApiResponse(responseCode = "403", description = "탈퇴 권한 없음 (본인 아님)"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 회원")
    })
    ResponseEntity<Void> withdraw(
            @Parameter(description = "탈퇴할 사용자 ID") Long userId,
            @Parameter(hidden = true) Long requesterId
    );

    @Operation(summary = "비밀번호 초기화", description = "비밀번호를 초기화하고 등록된 이메일로 임시 비밀번호를 발송합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "초기화 이메일 발송 성공"),
            @ApiResponse(responseCode = "400", description = "입력값 유효성 검사 실패"),
            @ApiResponse(responseCode = "404", description = "일치하는 회원 정보 없음"),
            @ApiResponse(responseCode = "500", description = "이메일 발송 실패 (SMTP 서버 에러 등)")
    })
    ResponseEntity<Void> resetPassword(ResetPasswordRequest request);

    @Operation(summary = "이메일 조회", description = "사용자 ID를 기반으로 이메일을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "이메일 조회 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 회원")
    })
    ResponseEntity<String> getEmail(Long userId);
}
