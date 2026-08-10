package com.nhnacademy.accountapi.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserStatus {
    ACTIVE("활성화 상태"),
    DORMANT("휴면 상태"),
    WITHDRAWN("탈퇴 상태");

    private final String description;
}
