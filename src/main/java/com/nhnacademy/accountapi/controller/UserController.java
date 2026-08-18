package com.nhnacademy.accountapi.controller;

import com.nhnacademy.accountapi.dto.*;
import com.nhnacademy.accountapi.dto.login.LoginRequest;
import com.nhnacademy.accountapi.dto.login.LoginResponse;
import com.nhnacademy.accountapi.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController //이 클래스가 JSON 데이터를 반환하는 REST API 대문임을 선언
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    // 회원가입 POST /api/account/signup
    @PostMapping("/signup")
    public ResponseEntity<Void> signUp(
            @Valid @RequestBody RegisterRequest request
    ){
        userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // 로그인 POST /api/account/login
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ){
        LoginResponse response= userService.login(request);
        return ResponseEntity.ok(response);
    }

    // 사용자 정보 수정 PUT /api/account/{user-id}
    @PutMapping("/{user-id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable("user-id") Long userId,
            @Valid @RequestBody UpdateRequest request,
            @RequestHeader("X-USER-ID") Long requesterId
    ){
        UserResponse userResponse = userService.updateUser(userId, requesterId, request);
        return ResponseEntity.ok(userResponse);
    }

    // 사용자 정보 조회 GET /api/account/{user-id}
    @GetMapping("/{user-id}")
    public ResponseEntity<UserResponse> getUser(
            @PathVariable("user-id") Long userId,
            @RequestHeader("X-USER-ID") Long requesterId
    ){
        UserResponse userResponse =userService.getUser(userId, requesterId);
        return ResponseEntity.ok(userResponse);
    }

    // 사용자 탈퇴 PATCH /api/account/{user-id}
    @PatchMapping("/{user-id}")
    public ResponseEntity<Void> withdraw(
            @PathVariable("user-id") Long userId,
            @RequestHeader("X-USER-ID") Long requesterId
    ){
        userService.withdraw(userId, requesterId);
        return ResponseEntity.noContent().build();
    }
}
