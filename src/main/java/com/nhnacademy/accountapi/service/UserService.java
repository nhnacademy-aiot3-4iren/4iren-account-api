package com.nhnacademy.accountapi.service;

import com.nhnacademy.accountapi.dto.*;
import com.nhnacademy.accountapi.dto.login.LoginRequest;
import com.nhnacademy.accountapi.dto.login.LoginResponse;

import java.util.List;

public interface UserService {

    UserResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    UserResponse updateUser(String userLoginId, Long requesterId, UpdateRequest request);

    List<UserResponse> getAllUsers(Long requesterId);

    UserResponse getUser(String userLoginId, Long requesterId);

    void withdraw(String userLoginId, Long requesterId);

    void dormant(String userLoginId);

    void active(String userLoginId);
}
