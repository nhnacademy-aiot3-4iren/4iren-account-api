package com.nhnacademy.accountapi.service;

import com.nhnacademy.accountapi.dto.*;
import com.nhnacademy.accountapi.dto.login.LoginRequest;
import com.nhnacademy.accountapi.dto.login.LoginResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface UserService {

    void register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    UserResponse updateUser(Long userId, Long requesterId, UpdateRequest request);

    UserResponse getUser(Long userId, Long requesterId);

    void withdraw(Long userId, Long requesterId);

    void dormant(Long userId);

    void active(Long userId);
}
