package com.nhnacademy.accountapi.dto.message;

import java.time.LocalDateTime;

public record RoleChangeMessage(Long userId, String role, String jti, LocalDateTime updateAt) {
}
