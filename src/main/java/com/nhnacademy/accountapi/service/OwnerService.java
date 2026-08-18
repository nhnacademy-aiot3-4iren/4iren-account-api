package com.nhnacademy.accountapi.service;

import com.nhnacademy.accountapi.dto.AdminCreateRequest;
import com.nhnacademy.accountapi.dto.UserResponse;
import jakarta.validation.Valid;

import java.util.List;

public interface OwnerService {
    void createAdmin(@Valid AdminCreateRequest request, Long requesterId);

    List<UserResponse> getUsers(Long requesterId);

    UserResponse getUser(Long requesterId, Long userId);

    void withdraw(Long requesterId, Long userId);

    void restore(Long requesterId, Long userId);

    void resetPassword(Long requesterId, Long userId);
}
