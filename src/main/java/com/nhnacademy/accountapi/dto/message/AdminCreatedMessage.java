package com.nhnacademy.accountapi.dto.message;

public record AdminCreatedMessage(
        Long adminId,
        Long ownerId
) {}
