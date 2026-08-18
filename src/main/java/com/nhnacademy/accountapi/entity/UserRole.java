package com.nhnacademy.accountapi.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter // description 가져오기
@RequiredArgsConstructor
public enum UserRole {
    OWNER("팀 생성자"),
    ADMIN("팀 관리자"),
    NORMAL("일반 사용자");

    private final String description;
}
