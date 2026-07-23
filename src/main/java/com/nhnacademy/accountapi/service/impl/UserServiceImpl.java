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

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; //비번 암호화 전담

    // 회원가입(create)
    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        //a. 로그인 ID 중복 체크
        if (userRepository.existsByUserLoginId(request.userLoginId())) {
            throw new UserAlreadyExistsException("이미 존재하는 아이디입니다" + request.userLoginId());
        }

        //b. 이메일 중복 체크
        if (userRepository.existsByUserEmail(request.userEmail())) {
            throw new UserAlreadyExistsException("이미 사용중인 이메일입니다."+ request.userEmail());
        }

        //c. 내용물 다꺼내와서 조립하기
        User user = User.builder()
                .loginId(request.userLoginId())
                .email(request.userEmail())
                .password(passwordEncoder.encode(request.userPassword()))
                .name(request.userName())
                .build();

        return toResponse(userRepository.save(user));
    }

    // 로그인
    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        // 로그인 ID로 회원 조회
        User user = userRepository.findByUserLoginId(request.userLoginId())
                .orElseThrow(() -> new UserNotFoundException("아이디 또는 비밀번호가 올바르지 않습니다"));

        // 비밀번호 일치 여부 확인
        if(!passwordEncoder.matches(request.userPassword(), user.getUserPassword())){
            throw new UserNotFoundException("아이디 또는 비밀번호가 올바르지 않습니다");
        }

        //계정 상태가 ACTIVE가 아니면 로그인 거부
        if(user.getUserStatus() != UserStatus.ACTIVE){
            throw new UserNotAllowException("로그인 할 수 없는 계정 상태입니다:" + user.getUserStatus());
        }

        // 마지막 로그인 시간 업데이트
        user.updateLoginAt();

        return new LoginResponse(
                user.getUserId(),
                user.getUserLoginId(),
                user.getUserName(),
                user.getUserRole().name()
        );
    }


    // 회원정보 수정
    @Override
    @Transactional
    public UserResponse updateUser(String userLoginId, Long requesterId , UpdateRequest request){
        // 회원 조회
        User user = findByUserLoginId(userLoginId);

        // 요청자와 수정 대상이 다를 경우, 예외 처리
        if(!Objects.equals(user.getUserId(), requesterId)) {
            throw new UserNotAllowException("본인만 회원정보를 수정할 수 있습니다.");
        }

        // 유저 이름 변경
        if(request.userName()!=null && !request.userName().isEmpty()){
            user.setUserName(request.userName());
        }

        // 유저 이메일 변경
        if(request.userEmail()!=null && !request.userEmail().isEmpty() && !Objects.equals(user.getUserEmail(), request.userEmail())) {
            if (userRepository.existsByUserEmail(request.userEmail())) {
                throw new UserAlreadyExistsException("이미 사용중인 이메일입니다."+ request.userEmail());
            }

            user.setUserEmail(request.userEmail());
        }

        // 유저 패스워드 변경
        if(request.userPassword()!=null && !request.userPassword().isEmpty()){
            user.setUserPassword(passwordEncoder.encode(request.userPassword()));
        }

        return toResponse(user);
    }

    // 전체 회원 조회
    @Override
   public List<UserResponse> getAllUsers(Long requesterId){
        // 요청자가 관리자 권한인지 확인
        if(!checkUserAdmin(requesterId)) {
            throw new UserNotAllowException("관리자만 모든 회원 정보를 조회할 수 있습니다.");
        }

        return userRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
   }

    // 단건 회원 조회
    @Override
    public UserResponse getUser(String userLoginId, Long requesterId){
        // 유저 조회
        User user = userRepository.findByUserLoginId(userLoginId)
                .orElseThrow(()->new UserNotFoundException("존재하지 않는 회원입니다. userLoginId=" +userLoginId));

        // 요청자와 조회 대상이 다를 경우, 요청자가 관리자 권한을 가지고 있는지 확인
        if(!Objects.equals(user.getUserId(), requesterId) && !checkUserAdmin(requesterId)) {
            throw new UserNotAllowException("본인 또는 관리자만 회원정보를 조회할 수 있습니다.");
        }

        // 요청자와 조회 대상이 같거나, 요청자가 관리자 권한을 가지고 있는 경우에만 회원 정보를 반환
        return toResponse(user);
    }

    // 회원 탈퇴(상태 변경)
    @Override
    @Transactional
    public void withdraw(String userLoginId, Long requesterId){
        User user = findByUserLoginId(userLoginId);

        if(!Objects.equals(user.getUserId(), requesterId)) {
            throw new UserNotAllowException("본인만 회원 탈퇴를 진행할 수 있습니다.");
        }

        user.setUserStatus(UserStatus.WITHDRAWN);
    }

    // 회원 휴면 처리
    @Override
    @Transactional
    public void dormant(String userLoginId){
        User user = findByUserLoginId(userLoginId);
        user.setUserStatus(UserStatus.DORMANT);
    }

    // 회원 휴면 해제(재활성화)
    @Override
    @Transactional
    public void active(String userLoginId){
        User user = findByUserLoginId(userLoginId);
        user.setUserStatus(UserStatus.ACTIVE);
    }

    // [공통 내부 메서드] Entity 장부를 UserResponse 안전 가방으로 변환
    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getUserId(),
                user.getUserLoginId(),
                user.getUserEmail(),
                user.getUserName(),
                user.getUserStatus().name(),
                user.getCreatedAt()

        );
    }

    // [공통 내부 메서드] 요청자가 관리자 권한을 가지고 있는지 확인
    private boolean checkUserAdmin(Long userId) {
        User user= userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("유저를 찾을 수 없습니다."));

        return user.getUserRole().equals(UserRole.ADMIN);
    }

    //[공통 내부 메서드] 고유 ID로 회원 찾기
    private User findByUserLoginId(String id) {
        return userRepository.findByUserLoginId(id)
                .orElseThrow(()-> new UserNotFoundException("회원을 찾을 수 없습니다. id=" +id));
    }
}
