package com.nhnacademy.accountapi.dto.internal;

import com.nhnacademy.accountapi.entity.UserRole;

public record UserRoleResponse(
        Long userId,
        UserRole role
) {
}
