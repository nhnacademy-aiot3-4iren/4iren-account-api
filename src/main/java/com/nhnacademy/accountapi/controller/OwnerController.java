package com.nhnacademy.accountapi.controller;

import com.nhnacademy.accountapi.dto.AdminCreateRequest;
import com.nhnacademy.accountapi.dto.UserResponse;
import com.nhnacademy.accountapi.service.OwnerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/owner")
public class OwnerController {

    private final OwnerService ownerService;

    // 관리자 생성
    @PostMapping("signup")
    public ResponseEntity<Void> signUp(
            @RequestHeader("X-USER-ID") Long requesterId,
            @Valid @RequestBody AdminCreateRequest request
    ) {
        ownerService.createAdmin(request, requesterId);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // 관리자 목록 조회
    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getUsers(
            @RequestHeader("X-USER-ID") Long requesterId
    ) {
        List<UserResponse> users = ownerService.getUsers(requesterId);

        return ResponseEntity.ok(users);
    }

    // 관리자 상세 조회
    @GetMapping("/{user-id}")
    public ResponseEntity<UserResponse> getUser(
        @RequestHeader("X-USER-ID") Long requesterId,
        @PathVariable("user-id") Long userId
    ) {
        UserResponse user= ownerService.getUser(requesterId, userId);

        return ResponseEntity.ok(user);
    }

    // 관리자 삭제
    @PatchMapping("/{user-id}")
    public ResponseEntity<Void> deleteUser(
        @RequestHeader("X-USER-ID") Long requesterId,
        @PathVariable("user-id") Long userId
    ) {
        ownerService.withdraw(requesterId, userId);

        return ResponseEntity.noContent().build();
    }

    // 관리자 복구
    @PatchMapping("/{user-id}/restore")
    public ResponseEntity<Void> restoreUser(
        @RequestHeader("X-USER-ID") Long requesterId,
        @PathVariable("user-id") Long userId
    ) {
        ownerService.restore(requesterId, userId);

        return ResponseEntity.noContent().build();
    }

    // 비밀번호 초기화
    @PatchMapping("/{user-id}/reset-password")
    public ResponseEntity<Void> resetPassword(
        @RequestHeader("X-USER-ID") Long requesterId,
        @PathVariable("user-id") Long userId
    ) {
        ownerService.resetPassword(requesterId, userId);

        return ResponseEntity.noContent().build();
    }
}
