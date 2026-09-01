package com.nhnacademy.accountapi.service;

import com.nhnacademy.accountapi.dto.*;
import com.nhnacademy.accountapi.dto.login.LoginRequest;
import com.nhnacademy.accountapi.dto.login.LoginResponse;

public interface UserService {

    void register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    UserResponse updateUser(Long userId, Long requesterId, UpdateRequest request);

    UserResponse getUser(Long userId, Long requesterId);

    void withdraw(Long userId, Long requesterId);

    void dormant(Long userId);

    void active(Long userId);

    String resetPassword(ResetPasswordRequest request);

    void updateUserRole(Long userId, String role);

    void publishRoleChangeEvent(Long userId, String role, String jti);

    String getEmail(Long userId);
}
