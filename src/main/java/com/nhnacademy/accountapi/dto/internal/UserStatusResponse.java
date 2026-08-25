package com.nhnacademy.accountapi.dto.internal;

import com.nhnacademy.accountapi.entity.UserStatus;

public record UserStatusResponse(
        Long userId,
        UserStatus status
) {
}
