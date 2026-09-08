package com.nhnacademy.accountapi.service;

import com.nhnacademy.accountapi.dto.internal.UserRoleResponse;
import com.nhnacademy.accountapi.dto.internal.UserStatusResponse;
import com.nhnacademy.accountapi.entity.UserRole;
import com.nhnacademy.accountapi.entity.UserStatus;
import com.nhnacademy.accountapi.exception.UserBadRequestException;
import com.nhnacademy.accountapi.exception.UserNotFoundException;
import com.nhnacademy.accountapi.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

// 다른 서버에서 유저의 권한이나 상태를 안전하게 조회해 갈 때 호출되는 전용 서비스 테스트
@ExtendWith(MockitoExtension.class)
class InternalUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private InternalUserService internalUserService;

    @Test
    @DisplayName("1. 유저 권한 조회 성공 - 유효한 id로 조회시 UserRoleResponse가 정상 반환된다")
    void getUserRole_Success() {
        given(userRepository.findRoleByUserId(1L)).willReturn(Optional.of(new UserRoleResponse(1L, UserRole.NORMAL)));

        //when
        UserRoleResponse response = internalUserService.getUserRole(1L);

        //then
        assertThat(response).isNotNull();
        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.role()).isEqualTo(UserRole.NORMAL);
    }

    @Test
    @DisplayName("2. 유저 권한 조회 실패 - 유저 id가 음수이거나 0인 경우 예외 발생")
    void getUserRole_InvalidUserId() {
        //when&then
        assertThatThrownBy(() -> internalUserService.getUserRole(-1L))
                .isInstanceOf(UserBadRequestException.class)
                .hasMessageContaining("사용자 ID는 양수여야 합니다.");
    }

    @Test
    @DisplayName("3. 유저 권한 조회 실패 - 존재하지 않는 회원인 경우 예외 발생")
    void getUserRole_UserNotFound() {
        given(userRepository.findRoleByUserId(999L)).willReturn(Optional.empty());

        //when&then
        assertThatThrownBy(() -> internalUserService.getUserRole(999L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("존재하지 않는 회원입니다.");
    }

    @Test
    @DisplayName("4. 유저 상태 조회 성공 - 유효한 id로 조회 시 UserStatusResponse가 정상 반환된다")
    void getUserStatus_Success() {
        //given
        given(userRepository.findStatusByUserId(1L)).willReturn(Optional.of(new UserStatusResponse(1L, UserStatus.ACTIVE)));

        //when
        UserStatusResponse response = internalUserService.getUserStatus(1L);

        //then
        assertThat(response).isNotNull();
        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.status()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("5. 유저 상태 조회 실패 - 유저 id가 음수인 경우 예외 발생")
    void getUserStatus_InvalidUserId() {
        //when&then
        assertThatThrownBy(() -> internalUserService.getUserStatus(-5L))
                .isInstanceOf(UserBadRequestException.class)
                .hasMessageContaining("사용자 ID는 양수여야 합니다.");
    }

    @Test
    @DisplayName("6. 유저 상태 조회 실패 - 존재하지 않는 회원인 경우 예외 발생")
    void getUserStatus_UserNotFound() {
        //given
        given(userRepository.findStatusByUserId(999L)).willReturn(Optional.empty());

        //when&then
        assertThatThrownBy(() -> internalUserService.getUserStatus(999L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("존재하지 않는 회원입니다.");
    }

    @Test
    @DisplayName("7. 유저 상태 배치 조회 성공 - 중복 ID가 들어와도 자동 중복 제거 후 정상 반환")
    void getUserStatuses_Success_WithDeduplication() {
        //given - 중복이 포함된 유저 ID 목록 [1L, 1L, 2L]
        List<Long> requestUserIds = List.of(1L, 1L, 2L);
        given(userRepository.findStatusesByUserIdIn(List.of(1L, 2L)))
                .willReturn(List.of(
                        new UserStatusResponse(1L, UserStatus.ACTIVE),
                        new UserStatusResponse(2L, UserStatus.ACTIVE)
                ));

        //when
        List<UserStatusResponse> responses = internalUserService.getUserStatuses(requestUserIds);

        //then
        assertThat(responses).hasSize(2);
        assertThat(responses).extracting(UserStatusResponse::userId)
                .containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    @DisplayName("8. 유저 상태 배치 조회 성공 - 빈 리스트 전달 시 즉시 빈 리스트 반환")
    void getUserStatuses_EmptyList_ReturnsEmpty() {
        //when
        List<UserStatusResponse> responses = internalUserService.getUserStatuses(List.of());

        //then
        assertThat(responses).isEmpty();
    }

    @Test
    @DisplayName("9. 유저 상태 배치 조회 실패 - null 목록 전달 시 예외 발생")
    void getUserStatuses_NullList_ThrowsException() {
        //when&then
        assertThatThrownBy(() -> internalUserService.getUserStatuses(null))
                .isInstanceOf(UserBadRequestException.class)
                .hasMessageContaining("사용자 ID 목록은 필수입니다.");
    }

    @Test
    @DisplayName("10. 유저 상태 배치 조회 실패 - 한 번에 1000개 초과하여 요청 시 예외 발생")
    void getUserStatuses_ExceedMaxBatchSize_ThrowsException() {
        //given - 1001개의 ID 리스트
        List<Long> tooManyUserIds = Collections.nCopies(1001, 1L);

        //when&then
        assertThatThrownBy(() -> internalUserService.getUserStatuses(tooManyUserIds))
                .isInstanceOf(UserBadRequestException.class)
                .hasMessageContaining("사용자 ID는 한 번에 최대 1000개까지 조회할 수 있습니다.");
    }

    @Test
    @DisplayName("11. 유저 상태 배치 조회 실패 - 목록 중 음수 ID가 포함된 경우 예외 발생")
    void getUserStatuses_ContainsInvalidId_ThrowsException() {
        //given
        List<Long> invalidUserIds = List.of(1L, -1L);

        //when&then
        assertThatThrownBy(() -> internalUserService.getUserStatuses(invalidUserIds))
                .isInstanceOf(UserBadRequestException.class)
                .hasMessageContaining("사용자 ID는 양수여야 합니다.");
    }
}