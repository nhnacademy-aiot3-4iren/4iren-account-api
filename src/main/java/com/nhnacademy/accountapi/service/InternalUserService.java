package com.nhnacademy.accountapi.service;

import com.nhnacademy.accountapi.dto.internal.UserRoleResponse;
import com.nhnacademy.accountapi.dto.internal.UserStatusResponse;
import com.nhnacademy.accountapi.exception.UserBadRequestException;
import com.nhnacademy.accountapi.exception.UserNotFoundException;
import com.nhnacademy.accountapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InternalUserService {

    private static final int MAX_BATCH_SIZE = 1000;

    private final UserRepository userRepository;

    public UserRoleResponse getUserRole(Long userId) {
        validateUserId(userId);

        return userRepository.findRoleByUserId(userId)
                .orElseThrow(() -> userNotFound(userId));
    }

    public UserStatusResponse getUserStatus(Long userId) {
        validateUserId(userId);

        return userRepository.findStatusByUserId(userId)
                .orElseThrow(() -> userNotFound(userId));
    }

    public List<UserStatusResponse> getUserStatuses(List<Long> userIds) {
        validateUserIds(userIds);
        if (userIds.isEmpty()) {
            return List.of();
        }

        List<Long> distinctUserIds = List.copyOf(new LinkedHashSet<>(userIds));
        Map<Long, UserStatusResponse> statusesByUserId = userRepository
                .findStatusesByUserIdIn(distinctUserIds)
                .stream()
                .collect(Collectors.toMap(
                        UserStatusResponse::userId,
                        Function.identity()
                ));

        return distinctUserIds.stream()
                .map(statusesByUserId::get)
                .filter(Objects::nonNull)
                .toList();
    }

    private void validateUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new UserBadRequestException("사용자 ID는 양수여야 합니다.");
        }
    }

    private void validateUserIds(List<Long> userIds) {
        if (userIds == null) {
            throw new UserBadRequestException("사용자 ID 목록은 필수입니다.");
        }
        if (userIds.size() > MAX_BATCH_SIZE) {
            throw new UserBadRequestException(
                    "사용자 ID는 한 번에 최대 " + MAX_BATCH_SIZE + "개까지 조회할 수 있습니다."
            );
        }
        if (userIds.stream().anyMatch(userId -> userId == null || userId <= 0)) {
            throw new UserBadRequestException("사용자 ID는 양수여야 합니다.");
        }
    }

    private UserNotFoundException userNotFound(Long userId) {
        return new UserNotFoundException(
                "존재하지 않는 회원입니다. userId=" + userId
        );
    }
}
