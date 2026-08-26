package com.nhnacademy.accountapi.dto.message;

public record RoleChangeMessage(Long userId, String role, String jti) {
}
