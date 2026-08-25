package com.nhnacademy.accountapi.dto.internal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public record UserStatusBatchRequest(
        @NotNull(message = "사용자 ID 목록은 필수입니다.")
        @Size(max = 1000, message = "사용자 ID는 한 번에 최대 1000개까지 조회할 수 있습니다.")
        List<@NotNull(message = "사용자 ID는 null일 수 없습니다.")
                @Positive(message = "사용자 ID는 양수여야 합니다.") Long> userIds
) {
}
