package com.nhnacademy.accountapi.service.impl;

import com.nhnacademy.accountapi.dto.AdminCreateRequest;
import com.nhnacademy.accountapi.dto.UserResponse;
import com.nhnacademy.accountapi.entity.User;
import com.nhnacademy.accountapi.entity.UserStatus;
import com.nhnacademy.accountapi.exception.UserAlreadyExistsException;
import com.nhnacademy.accountapi.exception.UserNotAllowException;
import com.nhnacademy.accountapi.exception.UserNotFoundException;
import com.nhnacademy.accountapi.repository.UserRepository;
import com.nhnacademy.accountapi.service.OwnerService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OwnerServiceImpl implements OwnerService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.default-password}")
    private String defaultPassword;

    // 관리자 생성
    @Override
    @Transactional
    public void createAdmin(AdminCreateRequest request, Long requesterId) {
        // 로그인 ID 중복 체크
        if (userRepository.existsByLoginId(request.loginId())) {
            throw new UserAlreadyExistsException("이미 존재하는 아이디입니다."+request.loginId());
        }

        String encodedPassword= passwordEncoder.encode(request.password());

        // 관리자 사용자 생성
        User user=User.createAdminUser(
                request.loginId(),
                encodedPassword,
                request.name(),
                requesterId
        );

        userRepository.save(user);

        // TODO Core API에 관리자 팀에 추가하라고 이벤트 발행 추가
    }

    // 관리자 목록 조회
    @Override
    public List<UserResponse> getUsers(Long requesterId) {
        List<User> users=userRepository.getUsersByCreatedBy(requesterId);

        return users.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // 관리자 상세 조회
    @Override
    public UserResponse getUser(Long requesterId, Long userId) {
        User user=userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        if(!Objects.equals(requesterId, user.getCreatedBy())) {
            throw new UserNotAllowException("해당 사용자의 정보를 조회할 권한이 없습니다.");
        }

        return toResponse(user);
    }

    // 관리자 삭제(탈퇴) 처리
    @Override
    @Transactional
    public void withdraw(Long requesterId, Long userId) {
        User user=userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        if(!Objects.equals(requesterId, user.getCreatedBy())) {
            throw new UserNotAllowException("해당 사용자의 정보를 조회할 권한이 없습니다.");
        }

        user.setStatus(UserStatus.WITHDRAWN);
    }

    // 관리자 복
    @Override
    @Transactional
    public void restore(Long requesterId, Long userId) {
        User user=userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        if(!Objects.equals(requesterId, user.getCreatedBy())) {
            throw new UserNotAllowException("해당 사용자의 정보를 조회할 권한이 없습니다.");
        }

        user.setStatus(UserStatus.ACTIVE);
    }

    // 비밀번호 초기화
    @Override
    @Transactional
    public void resetPassword(Long requesterId, Long userId) {
        User user=userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        if(!Objects.equals(requesterId, user.getCreatedBy())) {
            throw new UserNotAllowException("해당 사용자의 정보를 조회할 권한이 없습니다.");
        }

        String encodedPassword=passwordEncoder.encode(defaultPassword);
        user.setPassword(encodedPassword);
    }

    // [공통 내부 메서드] Entity 장부를 UserResponse 안전 가방으로 변환
    private UserResponse toResponse(User user) {
        String email=user.getEmail()!=null?user.getEmail():"등록안됨";

        return new UserResponse(
                user.getUserId(),
                user.getLoginId(),
                user.getRole().toString(),
                email,
                user.getName(),
                user.getStatus().name(),
                user.getCreatedAt()
        );
    }
}
