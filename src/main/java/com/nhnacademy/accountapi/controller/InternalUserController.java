package com.nhnacademy.accountapi.controller;

import com.nhnacademy.accountapi.controller.api.InternalUserApi;
import com.nhnacademy.accountapi.dto.internal.UserRoleResponse;
import com.nhnacademy.accountapi.dto.internal.UserStatusBatchRequest;
import com.nhnacademy.accountapi.dto.internal.UserStatusResponse;
import com.nhnacademy.accountapi.service.InternalUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/users")
public class InternalUserController implements InternalUserApi {

    private final InternalUserService internalUserService;

    @Override
    @GetMapping("/{user-id}/role")
    public ResponseEntity<UserRoleResponse> getUserRole(
            @PathVariable("user-id") Long userId
    ) {
        return ResponseEntity.ok(
                internalUserService.getUserRole(userId)
        );
    }

    @Override
    @GetMapping("/{user-id}/status")
    public ResponseEntity<UserStatusResponse> getUserStatus(
            @PathVariable("user-id") Long userId
    ) {
        return ResponseEntity.ok(
                internalUserService.getUserStatus(userId)
        );
    }

    @Override
    @PostMapping("/statuses")
    public ResponseEntity<List<UserStatusResponse>> getUserStatuses(
            @Valid @RequestBody UserStatusBatchRequest request
    ) {
        return ResponseEntity.ok(
                internalUserService.getUserStatuses(request.userIds())
        );
    }
}
