package com.nhnacademy.accountapi.service;
import com.nhnacademy.accountapi.dto.*;
import com.nhnacademy.accountapi.entity.Member;
import com.nhnacademy.accountapi.entity.UserStatus;
import com.nhnacademy.accountapi.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder; //비번 암호화 전담

    //1. 회원가입(create)
    @Transactional
    public Response register(RegisterRequest request) {
        //a. 로그인 ID 중복 체크
        if (memberRepository.existsByUserLoginId(request.getUserLoginId())) {
            throw new IllegalArgumentException("이미 존재하는 아이디입니다" + request.getUserLoginId());
        }

        //b. 이메일 중복 체크
        if (memberRepository.existsByUserEmail(request.getUserEmail())) {

            throw new IllegalArgumentException("이미 사용중인 아이디입니다"+ request.getUserEmail());
        }

        //c. 내용물 다꺼내와서 조립하기
        Member member = Member.builder()
                .userLoginId(request.getUserLoginId())
                .userEmail(request.getUserEmail())
                .userPassword(passwordEncoder.encode(request.getUserPassword()))
                .userName(request.getUserName())
                .userRole("USER") //기본 권한 부여 (필요 시 request에서 받도록 변경 가능)
                .build();

        return toResponse(memberRepository.save(member));
    }


    //[공통 내부 메서드] Entity 장부를 Response 안전 가방으로 변환
    private Response toResponse(Member m) {
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
        Member member = memberRepository.findByUserLoginId(request.getUserLoginId())
                .orElseThrow(() -> new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다"));

        if(!passwordEncoder.matches(request.getUserPassword(), member.getUserPassword())){
            throw new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다");
        }

        //계정 상태가 ACTIVE가 아니면 로그인 거부
        if(member.getUserStatus() != UserStatus.ACTIVE){
            throw new IllegalArgumentException("로그인 할 수 없는 계정 상태입니다:" +member.getUserStatus());
        }

        return new LoginResponse(
                member.getUserId(),
                member.getUserLoginId(),
                member.getUserName()
        );
    }


    //3.회원정보 수정
    @Transactional
    public Response updateMember(Long userid, UpdateRequest request){
        Member member= findByUserId(userid);

        if(request.getUserName() != null) member.setUserName(request.getUserName());
        if(request.getUserEmail() != null) member.setUserEmail(request.getUserEmail());

        return toResponse(member);
    }

    //[공통 내부 메서드] 고유 ID로 회원 찾기

    private Member findByUserId(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(()-> new IllegalArgumentException("회원을 찾을 수 없습니다. id=" +id));
    }

    //4. 전체 회원 조회
   public List<Response> getAllMembers(){
        return memberRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
   }

    //5. 단건 회원 조회
    public Response getMember(String userLoginId){
        Member member= memberRepository.findByUserLoginId(userLoginId)
                .orElseThrow(()->new IllegalArgumentException("존재하지 않는 회원입니다. userLoginId=" +userLoginId));
        return toResponse(member);
    }

    //6. 이메일 중복 여부 확인
    public boolean existsByEmail(String userEmail){
        return memberRepository.existsByUserEmail(userEmail);
    }

    //7. 회원 탈퇴(상태 변경)
    @Transactional
    public void withdraw(Long userId){
        Member member=findByUserId(userId);
        member.setUserStatus(UserStatus.WITHDRAWN);
    }

    //8. 회원 휴면 처리
    @Transactional
    public void dormant(Long userId){
        Member member=findByUserId(userId);
        member.setUserStatus(UserStatus.DORMANT);
    }

    //9. 회원 휴면 해제(재활성화)
    @Transactional
    public void active(Long userId){
        Member member=findByUserId(userId);
        member.setUserStatus(UserStatus.ACTIVE);
    }




}
