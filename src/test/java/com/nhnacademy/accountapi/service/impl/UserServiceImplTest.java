package com.nhnacademy.accountapi.service.impl;

import com.nhnacademy.accountapi.dto.RegisterRequest;
import com.nhnacademy.accountapi.dto.ResetPasswordRequest;
import com.nhnacademy.accountapi.dto.UpdateRequest;
import com.nhnacademy.accountapi.dto.UserResponse;
import com.nhnacademy.accountapi.dto.login.LoginRequest;
import com.nhnacademy.accountapi.dto.login.LoginResponse;
import com.nhnacademy.accountapi.dto.message.RoleChangeMessage;
import com.nhnacademy.accountapi.entity.User;
import com.nhnacademy.accountapi.entity.UserRole;
import com.nhnacademy.accountapi.entity.UserStatus;
import com.nhnacademy.accountapi.exception.UserAlreadyExistsException;
import com.nhnacademy.accountapi.exception.UserNotAllowException;
import com.nhnacademy.accountapi.exception.UserNotFoundException;
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

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Mock //  RabbitMQ 가짜 객체
    private org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;
    @Mock //  RabbitMQ 속성 가짜 객체
    private com.nhnacademy.accountapi.config.properties.RabbitAccountProperties accountProperties;
    @Mock //  메일 서비스 가짜 객체
    private com.nhnacademy.accountapi.service.MailService mailService;

    private User testUser;

    @BeforeEach
    void setUp() {
        //이미 DB에 가입되어 있는 유저라고 가정할 객체 (save()는 호출하지 않음)
        testUser=User.createNormalUser("user1","user1@nhn.com","encoded_pw","홍길동");
        testUser.setStatus(UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("회원가입 성공- 중복이 없으면 암호화 후 저장된다")
    void register_Success() {
        //given
        RegisterRequest registerRequest=new RegisterRequest("user1","pw1234","user1@nhn.com","홍길동");

        given(userRepository.existsByLoginId(registerRequest.loginId())).willReturn(false);
        given(userRepository.existsByEmail(registerRequest.email())).willReturn(false);
        given(passwordEncoder.encode(registerRequest.password())).willReturn("encoded_pw");

        //when
        userService.register(registerRequest);

        //then
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("회원가입 실패- 이미 존재하는 로그인ID인 경우 예외 발생")
    void register_LoginId_Exception(){
        //given
        RegisterRequest registerRequest=new RegisterRequest("user1","pw1234","user1@nhn.com","홍길동");

        given(userRepository.existsByLoginId(registerRequest.loginId())).willReturn(true);

        //when&then
        assertThatThrownBy(()->userService.register(registerRequest))
                .isInstanceOf(UserAlreadyExistsException.class);
    }

    @Test
    @DisplayName("회원가입 실패- 이미 사용중인 이메일인 경우 예외 발생")
    void register_Email_Exception(){
        //given
        RegisterRequest registerRequest=new RegisterRequest("user1","pw1234","user1@nhn.com","홍길동");

        given(userRepository.existsByLoginId(registerRequest.loginId())).willReturn(false);
        given(userRepository.existsByEmail(registerRequest.email())).willReturn(true);

        //when&then
        assertThatThrownBy(()->userService.register(registerRequest))
                .isInstanceOf(UserAlreadyExistsException.class);
    }

    @Test
    @DisplayName("로그인 성공- 정보가 일치하고 ACTIVE 상태이면 로그인 성공")
    void login_Success() {
        //given - 로그인 요청 id 준비
        LoginRequest loginRequest=new LoginRequest("user1","encoded_pw");

        given(userRepository.findByLoginId(loginRequest.loginId())).willReturn(Optional.of(testUser));
        given(passwordEncoder.matches(loginRequest.password(),testUser.getPassword())).willReturn(true);

        //when
        LoginResponse response=userService.login(loginRequest);

        //then
        assertThat(response).isNotNull();
        assertThat(response.loginId()).isEqualTo("user1");
        assertThat(response.name()).isEqualTo("홍길동");
        assertThat(response.role()).isEqualTo("NORMAL");
        assertThat(response.firstLogin()).isTrue();
        assertThat(testUser.getLastLoginAt()).isNotNull();
    }

    @Test
    @DisplayName("로그인 실패- 존재하지 않는 로그인 id인 경우 예외 발생")
    void login_UserNotFofund() {
        //given
        LoginRequest loginRequest=new LoginRequest("ghost","pw1234");

        //없는 아이디이므로 Optional.empty() 반환
        given(userRepository.findByLoginId(loginRequest.loginId())).willReturn(Optional.empty());

        //when&then
        assertThatThrownBy(()->userService.login(loginRequest))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("로그인 실패- 비밀번호가 일치하지 않는 경우 예외 발생")
    void login_PasswordMisMatch() {
        //given
        LoginRequest loginRequest=new LoginRequest("user1","wrong_pw");

        given(userRepository.findByLoginId(loginRequest.loginId())).willReturn(Optional.of(testUser));
        given(passwordEncoder.matches(loginRequest.password(),testUser.getPassword())).willReturn(false);

        //when&then
        assertThatThrownBy(()->userService.login(loginRequest))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("로그인 실패- 계정 상태가 ACTIVE가 아닌 경우 예외 발생")
    void login_StatusNotActive() {
        //given
        LoginRequest loginRequest=new LoginRequest("user1","pw1234");
        testUser.setStatus(UserStatus.WITHDRAWN);

        given(userRepository.findByLoginId(loginRequest.loginId())).willReturn(Optional.of(testUser));
        given(passwordEncoder.matches(loginRequest.password(),testUser.getPassword())).willReturn(true);

        //when&then
        assertThatThrownBy(()->userService.login(loginRequest))
                .isInstanceOf(UserNotAllowException.class);
    }

    @Test
    @DisplayName("회원정보 수정 성공- 로그인id, 이메일, 비밀번호 모두 정상 변경")
    void updateUser_Success() {
        //given
        UpdateRequest updateRequest=new UpdateRequest("newId","new@nhn.com","newPw");
        ReflectionTestUtils.setField(testUser,"userId",1L);

        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(userRepository.existsByLoginId("newId")).willReturn(false);
        given(userRepository.existsByEmail("new@nhn.com")).willReturn(false);
        given(passwordEncoder.encode("newPw")).willReturn("encoded_newPw");

        //when
        UserResponse response= userService.updateUser(1L,1L,updateRequest);

        //then
        assertThat(response).isNotNull();
        assertThat(response.loginId()).isEqualTo("newId");
        assertThat(response.email()).isEqualTo("new@nhn.com");
        assertThat(testUser.getPassword()).isEqualTo("encoded_newPw");
    }

    @Test
    @DisplayName("회원정보 수정 실패- 요청자와 수정 대상이 다르면 예외 발생")
    void updateUser_NotOwner() {
        //given
        UpdateRequest updateRequest=new UpdateRequest("newId","new@nhn.com","newPw");

        //when&then
        assertThatThrownBy(()->userService.updateUser(1L,2L,updateRequest))
                .isInstanceOf(UserNotAllowException.class);
    }

    @Test
    @DisplayName("회원정보 수정 실패- 존재하지 않는 회원인 경우 예외 발생")
    void updateUser_UserNotFound() {
        //given
        UpdateRequest updateRequest=new UpdateRequest("newId","new@nhn.com","newPw");
        given(userRepository.findById(1L)).willReturn(Optional.empty());

        //when&then
        assertThatThrownBy(()->userService.updateUser(1L,1L,updateRequest))
                .isInstanceOf(UserNotFoundException.class);

    }

    @Test
    @DisplayName("회원정보 수정 실패- 변경하려는 로그인 id가 이미 존재하는 경우 예외 발생")
    void updateUser_DuplicateLoginId() {
        //given
        UpdateRequest updateRequest=new UpdateRequest("alreadyExistId","nhn.com","newPw");
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(userRepository.existsByLoginId("alreadyExistId")).willReturn(true);

        //when&then
        assertThatThrownBy(()-> userService.updateUser(1L,1L,updateRequest))
                .isInstanceOf(UserAlreadyExistsException.class);
    }


    @Test
    @DisplayName("회원정보 수정 실패- 변경하려는 이메일이 이미 존재하는 경우 예외 발생")
    void updateUser_DuplicateEmail() {
        //given
        UpdateRequest updateRequest=new UpdateRequest("newId","alreadyExist.com","newPw");

        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(userRepository.existsByLoginId("newId")).willReturn(false);
        given(userRepository.existsByEmail("alreadyExist.com")).willReturn(true);

        //when&then
        assertThatThrownBy(()->userService.updateUser(1L,1L,updateRequest))
                .isInstanceOf(UserAlreadyExistsException.class);

    }


    @Test
    @DisplayName("회원 정보 조회 성공- 본인이 본인 정보 조회 시 정상 반환")
    void getUser_Success() {
        //given
        ReflectionTestUtils.setField(testUser,"userId",1L);
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));

        //when
        UserResponse response=userService.getUser(1L,1L);

        //then
        assertThat(response).isNotNull();
        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.loginId()).isEqualTo("user1");
        assertThat(response.name()).isEqualTo("홍길동");
    }

    @Test
    @DisplayName("회원 정보 조회 실패- 존재하지 않는 회원 ID로 조회 시 예외 발생")
    void getUser_UserNotFound() {
        //given
        given(userRepository.findById(1L)).willReturn(Optional.empty());

        //when
        assertThatThrownBy(()-> userService.getUser(1L,1L))
                .isInstanceOf(UserNotFoundException.class);

    }

    @Test
    @DisplayName("회원정보 조회 실패- 남의 회원 정보를 조회하려고 할 시 예외 발생")
    void getUser_NotOwner() {
        //given
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));

        //when&then
        assertThatThrownBy(()->userService.getUser(1L,2L))
                .isInstanceOf(UserNotAllowException.class);

    }

    @Test
    @DisplayName("회원탈퇴 성공- 상태가 WITHDRAWN으로 변경됨")
    void withdraw_Success() {
        //given
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));

        //when
        userService.withdraw(1L,1L);

        //then
        assertThat(testUser.getStatus()).isEqualTo(UserStatus.WITHDRAWN);
    }

    @Test
    @DisplayName("회원탈퇴 실패- 타인이 탈퇴 시도 시 예외 발생")
    void withdraw_NotOwner() {
        //when&then
        assertThatThrownBy(()->userService.withdraw(1L,2L))
                .isInstanceOf(UserNotAllowException.class);
    }

    @Test
    @DisplayName("회원탈퇴 실패- 존재하지 않는 회원 id인 경우 예외 발생")
    void withdraw_UserNotFound() {
        //given
        given(userRepository.findById(1L)).willReturn(Optional.empty());

        //when&then
        assertThatThrownBy(()->userService.withdraw(1L,1L))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("회원휴면처리 성공- 상태가 DORMANT로 변경됨")
    void dormant() {
        //given
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));

        //when
        userService.dormant(1L);

        //then
        assertThat(testUser.getStatus()).isEqualTo(UserStatus.DORMANT);
    }

    @Test
    @DisplayName("회원계정 재활성화 성공- 상태가 ACTIVE로 변경됨")
    void active() {
        //given- testUser의 상태가 휴면 상태였다고 설정
        testUser.setStatus(UserStatus.DORMANT);
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));

        //when
        userService.active(1L);

        //then
        assertThat(testUser.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("비밀번호 재설정 성공 - 임시 비밀번호 발급 및 메일 전송")
    void resetPassword_Success() {
        // given
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setLoginId("user1");
        request.setEmail("user1@nhn.com");

        given(userRepository.findByLoginId("user1")).willReturn(Optional.of(testUser));
        given(passwordEncoder.encode(any(String.class))).willReturn("encoded_temp_pw");

        // when
        userService.resetPassword(request);

        // then
        assertThat(testUser.getPassword()).isEqualTo("encoded_temp_pw");
        verify(mailService).sendTemporaryPassword(eq("user1@nhn.com"), any(String.class));
    }

    @Test
    @DisplayName("비밀번호 재설정 실패 - 존재하지 않는 회원 로그인 ID")
    void resetPassword_UserNotFound() {
        // given
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setLoginId("ghost");
        request.setEmail("ghost@nhn.com");

        given(userRepository.findByLoginId("ghost")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.resetPassword(request))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("일치하는 회원 정보가 없습니다.");
    }

    @Test
    @DisplayName("비밀번호 재설정 실패 - 등록된 이메일 불일치")
    void resetPassword_EmailMismatch() {
        // given
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setLoginId("user1");
        request.setEmail("wrong@nhn.com");

        given(userRepository.findByLoginId("user1")).willReturn(Optional.of(testUser));

        // when & then
        assertThatThrownBy(() -> userService.resetPassword(request))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("일치하는 회원 정보가 없습니다.");
    }

    @Test
    @DisplayName("유저 권한 변경 성공 - Role이 변경됨")
    void updateUserRole_Success() {
        // given
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));

        // when
        userService.updateUserRole(1L, "ADMIN");

        // then
        assertThat(testUser.getRole()).isEqualTo(UserRole.ADMIN);
    }

    @Test
    @DisplayName("유저 권한 변경 실패 - 존재하지 않는 회원 ID")
    void updateUserRole_UserNotFound() {
        // given
        given(userRepository.findById(99L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.updateUserRole(99L, "ADMIN"))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("존재하지 않는 회원입니다.");
    }

    @Test
    @DisplayName("유저 권한 변경 실패 - 잘못된 Role 값 전달 시 예외 발생")
    void updateUserRole_InvalidRole() {
        // given
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));

        // when & then
        assertThatThrownBy(() -> userService.updateUserRole(1L, "INVALID_ROLE"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("권한 변경 이벤트 발행 성공 - RabbitMQ 메세지 전송")
    void publishRoleChangeEvent_Success() {
        // given
        given(accountProperties.getExchange()).willReturn("4iren.account.events");
        given(accountProperties.getRoutingKey()).willReturn("4iren.account.role-change");

        // when
        userService.publishRoleChangeEvent(1L, "ADMIN", "jti-1234");

        // then
        verify(rabbitTemplate).convertAndSend(
                eq("4iren.account.events"),
                eq("4iren.account.role-change"),
                any(RoleChangeMessage.class)
        );
    }
}