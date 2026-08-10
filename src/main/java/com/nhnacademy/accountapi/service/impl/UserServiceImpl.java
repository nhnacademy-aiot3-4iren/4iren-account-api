package com.nhnacademy.accountapi.service.impl;

import com.nhnacademy.accountapi.dto.*;
import com.nhnacademy.accountapi.dto.login.LoginRequest;
import com.nhnacademy.accountapi.dto.login.LoginResponse;
import com.nhnacademy.accountapi.entity.User;
import com.nhnacademy.accountapi.entity.UserRole;
import com.nhnacademy.accountapi.entity.UserStatus;
import com.nhnacademy.accountapi.exception.UserAlreadyExistsException;
import com.nhnacademy.accountapi.exception.UserNotAllowException;
import com.nhnacademy.accountapi.exception.UserNotFoundException;
import com.nhnacademy.accountapi.repository.UserRepository;
import com.nhnacademy.accountapi.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Objects;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; //비번 암호화 전담

    // 회원가입(create)
    @Override
    @Transactional
    public void register(RegisterRequest request) {
        //a. 로그인 ID 중복 체크
        if (userRepository.existsByLoginId(request.loginId())) {
            throw new UserAlreadyExistsException("이미 존재하는 아이디입니다" + request.loginId());
        }

        //b. 이메일 중복 체크
        if (userRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException("이미 사용중인 이메일입니다." + request.email());
        }

        //c. 내용물 다꺼내와서 조립하기
        User user = User.builder()
                .loginId(request.loginId())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .name(request.name())
                .build();

        userRepository.save(user);
    }

    // 로그인
    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        // 로그인 ID로 회원 조회
        User user = userRepository.findByLoginId(request.loginId())
                .orElseThrow(() -> new UserNotFoundException("아이디 또는 비밀번호가 올바르지 않습니다"));

        // 비밀번호 일치 여부 확인
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new UserNotFoundException("아이디 또는 비밀번호가 올바르지 않습니다");
        }

        //계정 상태가 ACTIVE가 아니면 로그인 거부
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UserNotAllowException("로그인 할 수 없는 계정 상태입니다:" + user.getStatus());
        }

        // 마지막 로그인 시간 업데이트
        user.updateLoginAt();

        return new LoginResponse(
                user.getUserId(),
                user.getLoginId(),
                user.getName(),
                user.getRole().name()
        );
    }


    // 회원정보 수정
    @Override
    @Transactional
    public UserResponse updateUser(Long userId, Long requesterId, UpdateRequest request) {
        // 요청자와 수정 대상이 다를 경우, 예외 처리
        if (!Objects.equals(userId, requesterId)) {
            throw new UserNotAllowException("본인만 회원정보를 수정할 수 있습니다.");
        }

        // 회원 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("존재하지 않는 회원입니다. userId=" + userId));

        if(request.loginId()!=null&& !request.loginId().isEmpty() && !Objects.equals(user.getLoginId(), request.loginId())) {
            if (userRepository.existsByLoginId(request.loginId())) {
                throw new UserAlreadyExistsException("이미 사용중인 로그인 ID입니다." + request.loginId());
            }
            user.setLoginId(request.loginId());
        }

        // 유저 이메일 변경
        if (request.email() != null && !request.email().isEmpty() && !Objects.equals(user.getEmail(), request.email())) {
            if (userRepository.existsByEmail(request.email())) {
                throw new UserAlreadyExistsException("이미 사용중인 이메일입니다." + request.email());
            }

            user.setEmail(request.email());
        }

        // 유저 패스워드 변경
        if (request.password() != null && !request.password().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }

        return toResponse(user);
    }

    // 전체 회원 조회
    @Override
    public Page<UserResponse> getAllUsers(Long requesterId, Pageable pageable) {
        // 요청자가 관리자 권한인지 확인
        if (!checkSuperAdmin(requesterId)) {
            throw new UserNotAllowException("관리자만 모든 회원 정보를 조회할 수 있습니다.");
        }

        return userRepository.findAll(pageable)
                .map(this::toResponse);
    }

    // 단건 회원 조회
    @Override
    public UserResponse getUser(Long userId, Long requesterId) {
        // 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("존재하지 않는 회원입니다. userId=" + userId));

        // 요청자와 조회 대상이 다를 경우, 요청자가 관리자 권한을 가지고 있는지 확인
        if (!Objects.equals(userId, requesterId) && !checkSuperAdmin(requesterId)) {
            throw new UserNotAllowException("본인 또는 관리자만 회원정보를 조회할 수 있습니다.");
        }

        // 요청자와 조회 대상이 같거나, 요청자가 관리자 권한을 가지고 있는 경우에만 회원 정보를 반환
        return toResponse(user);
    }

    // 회원 탈퇴(상태 변경)
    @Override
    @Transactional
    public void withdraw(Long userId, Long requesterId) {
        if (!Objects.equals(userId, requesterId)) {
            throw new UserNotAllowException("본인만 회원 탈퇴를 진행할 수 있습니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("존재하지 않는 회원입니다. userId=" + userId));

        user.setStatus(UserStatus.WITHDRAWN);
    }

    // 회원 휴면 처리
    @Override
    @Transactional
    public void dormant(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("존재하지 않는 회원입니다. userId=" + userId));

        user.setStatus(UserStatus.DORMANT);
    }

    // 회원 휴면 해제(재활성화)
    @Override
    @Transactional
    public void active(Long userId) {
        User user = userRepository.findById(userId)
                        .orElseThrow(() -> new UserNotFoundException("존재하지 않는 회원입니다. userId=" + userId));
        user.setStatus(UserStatus.ACTIVE);
    }

    // [공통 내부 메서드] Entity 장부를 UserResponse 안전 가방으로 변환
    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getUserId(),
                user.getLoginId(),
                user.getRole().toString(),
                user.getEmail(),
                user.getName(),
                user.getStatus().name(),
                user.getCreatedAt()
        );
    }

    // [공통 내부 메서드] 요청자가 총 관리자 권한을 가지고 있는지 확인
    private boolean checkSuperAdmin(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("유저를 찾을 수 없습니다."));

        return user.getRole().equals(UserRole.SUPER_ADMIN);
    }
}
