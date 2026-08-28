package com.nhnacademy.accountapi.dto.message;

public record PaymentCompleteMessage(Long userId, String role, String jti) {
}
