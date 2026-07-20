package com.nhnacademy.accountapi.service;
import com.nhnacademy.accountapi.dto.*;
import com.nhnacademy.accountapi.entity.User;
import com.nhnacademy.accountapi.entity.UserStatus;
import com.nhnacademy.accountapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; //비번 암호화 전담

    //1. 회원가입(create)
    @Transactional
    public Response register(RegisterRequest request) {
        //a. 로그인 ID 중복 체크
        if (userRepository.existsByUserLoginId(request.getUserLoginId())) {
            throw new IllegalArgumentException("이미 존재하는 아이디입니다" + request.getUserLoginId());
        }

        //b. 이메일 중복 체크
        if (userRepository.existsByUserEmail(request.getUserEmail())) {

            throw new IllegalArgumentException("이미 사용중인 아이디입니다"+ request.getUserEmail());
        }

        //c. 내용물 다꺼내와서 조립하기
        User user = User.builder()
                .userLoginId(request.getUserLoginId())
                .userEmail(request.getUserEmail())
                .userPassword(passwordEncoder.encode(request.getUserPassword()))
                .userName(request.getUserName())
                .userRole("USER") //기본 권한 부여 (필요 시 request에서 받도록 변경 가능)
                .build();

        return toResponse(userRepository.save(user));
    }


    //[공통 내부 메서드] Entity 장부를 Response 안전 가방으로 변환
    private Response toResponse(User m) {
        return new Response(
                m.getUserId(),
                m.getUserLoginId(),
                m.getUserEmail(),
                m.getUserName(),
                m.getUserStatus().name(),
                m.getCreatedAt()

        );
    }


    //2. 로그인(인증 처리)
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUserLoginId(request.getUserLoginId())
                .orElseThrow(() -> new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다"));

        if(!passwordEncoder.matches(request.getUserPassword(), user.getUserPassword())){
            throw new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다");
        }

        //계정 상태가 ACTIVE가 아니면 로그인 거부
        if(user.getUserStatus() != UserStatus.ACTIVE){
            throw new IllegalArgumentException("로그인 할 수 없는 계정 상태입니다:" + user.getUserStatus());
        }

        return new LoginResponse(
                user.getUserId(),
                user.getUserLoginId(),
                user.getUserName()
        );
    }


    //3.회원정보 수정
    @Transactional
    public Response updateUser(Long userid, UpdateRequest request){
        User user = findByUserId(userid);

        if(request.getUserName() != null) user.setUserName(request.getUserName());
        if(request.getUserEmail() != null) user.setUserEmail(request.getUserEmail());

        return toResponse(user);
    }

    //[공통 내부 메서드] 고유 ID로 회원 찾기

    private User findByUserId(Long id) {
        return userRepository.findById(id)
                .orElseThrow(()-> new IllegalArgumentException("회원을 찾을 수 없습니다. id=" +id));
    }

    //4. 전체 회원 조회
   public List<Response> getAllUsers(){
        return userRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
   }

    //5. 단건 회원 조회
    public Response getUser(String userLoginId){
        User user = userRepository.findByUserLoginId(userLoginId)
                .orElseThrow(()->new IllegalArgumentException("존재하지 않는 회원입니다. userLoginId=" +userLoginId));
        return toResponse(user);
    }

    //6. 이메일 중복 여부 확인
    public boolean existsByEmail(String userEmail){
        return userRepository.existsByUserEmail(userEmail);
    }

    //7. 회원 탈퇴(상태 변경)
    @Transactional
    public void withdraw(Long userId){
        User user =findByUserId(userId);
        user.setUserStatus(UserStatus.WITHDRAWN);
    }

    //8. 회원 휴면 처리
    @Transactional
    public void dormant(Long userId){
        User user =findByUserId(userId);
        user.setUserStatus(UserStatus.DORMANT);
    }

    //9. 회원 휴면 해제(재활성화)
    @Transactional
    public void active(Long userId){
        User user =findByUserId(userId);
        user.setUserStatus(UserStatus.ACTIVE);
    }




}
