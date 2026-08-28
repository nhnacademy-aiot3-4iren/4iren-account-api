package com.nhnacademy.accountapi.service.impl;

import com.nhnacademy.accountapi.dto.AdminCreateRequest;
import com.nhnacademy.accountapi.dto.UserResponse;
import com.nhnacademy.accountapi.entity.User;
import com.nhnacademy.accountapi.entity.UserStatus;
import com.nhnacademy.accountapi.exception.UserAlreadyExistsException;
import com.nhnacademy.accountapi.exception.UserNotAllowException;
import com.nhnacademy.accountapi.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OwnerServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private OwnerServiceImpl ownerService;

    private User testAdmin;
    private final Long ownerId=999L;

    @BeforeEach
    void setUp(){
        testAdmin=User.createAdminUser("admin1","encoded_pw","관리자1",ownerId);
        testAdmin.setStatus(UserStatus.ACTIVE);
        ReflectionTestUtils.setField(testAdmin,"userId",10L);
        ReflectionTestUtils.setField(ownerService,"defaultPassword","1234");

    }

    @Test
    @DisplayName("관리자 생성 성공- 아이디 중복이 없으면 암호화 후 관리자 계정이 저장됨")
    void createAdmin_Success() {
        //given
        AdminCreateRequest request=new AdminCreateRequest("admin1","pw1234","관리자1");

        given(userRepository.existsByLoginId(request.loginId())).willReturn(false);
        given(passwordEncoder.encode(request.password())).willReturn("encoded_pw");

        //when
        ownerService.createAdmin(request,ownerId);

        //then
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("관리자 생성 실패- 아이디가 이미 존재하는 경우 예외 발생")
    void createAdmin_DuplicateLoginId() {
        //given
        AdminCreateRequest request=new AdminCreateRequest("admin1","pw1234","관리자1");
        given(userRepository.existsByLoginId(request.loginId())).willReturn(true);

        //when&then
        assertThatThrownBy(()-> ownerService.createAdmin(request,ownerId))
                .isInstanceOf(UserAlreadyExistsException.class);

    }

    @Test
    @DisplayName("관리자 목록 조회 성공- 오너가 생성한 관리자 목록이 DTO 리스트로 변환되어 반환")
    void getUsers_Success() {
        //given
        given(userRepository.getUsersByCreatedBy(ownerId)).willReturn(List.of(testAdmin));

        //when
        List<UserResponse> responses=ownerService.getUsers(ownerId);

        //then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).loginId()).isEqualTo("admin1");
    }

    @Test
    @DisplayName("")
    void getUser_Success() {
        //given
        given(userRepository.findById(1L)).willReturn(Optional.of(testAdmin));

        //when
        UserResponse response=ownerService.getUser(ownerId,1L);

        //then
        assertThat(response).isNotNull();
        assertThat(response.loginId()).isEqualTo("admin1");
        assertThat(response.name()).isEqualTo("관리자1");
    }

    @Test
    @DisplayName("관리자 상세 조회 실패- 다른 오너가 생성한 관리자를 조회하려 하면 권한 예외 발생")
    void getUser_NotOwner() {
        //given
        given(userRepository.findById(1L)).willReturn(Optional.of(testAdmin));

        //when&then
        assertThatThrownBy(()->ownerService.getUser(888L,1L))
                .isInstanceOf(UserNotAllowException.class);
    }


    @Test
    @DisplayName("관리자 삭제(탈퇴) 성공- 관리자 계정 상태가 WITHDRAWN으로 변경된다")
    void withdraw_Success() {
        given(userRepository.findById(1L)).willReturn(Optional.of(testAdmin));

        ownerService.withdraw(ownerId,1L);

        assertThat(testAdmin.getStatus()).isEqualTo(UserStatus.WITHDRAWN);
    }

    @Test
    @DisplayName("관리자 복구 성공- 탈퇴된 관리자 계정 상태가 다시 ACTIVE로 변경된다")
    void restore_Success() {
        testAdmin.setStatus(UserStatus.WITHDRAWN);
        given(userRepository.findById(1L)).willReturn(Optional.of(testAdmin));

        ownerService.restore(ownerId,1L);

        assertThat(testAdmin.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("비밀번호 초기화 성공- 비밀번호가 기본 비밀번호(1234)의 암호화 값으로 변경된다")
    void resetPassword_Success() {
        given(userRepository.findById(1L)).willReturn(Optional.of(testAdmin));

        given(passwordEncoder.encode("1234")).willReturn("encoded_default_1234");

        ownerService.resetPassword(ownerId,1L);

        assertThat(testAdmin.getPassword()).isEqualTo("encoded_default_1234");
        assertThat(testAdmin.getLastLoginAt()).isNull();
    }
}