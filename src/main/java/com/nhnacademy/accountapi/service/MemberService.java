package com.nhnacademy.accountapi.service;
import com.nhnacademy.accountapi.entity.Member;
import com.nhnacademy.accountapi.dto.RegisterRequest;
import com.nhnacademy.accountapi.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemerService {

    private final MemberRepository memberRepository;

    //1. 회원가입(create)

    //a. 로그인 ID 중복 체크

    //b. 이메일 중복 체크

    //c. 내용물 다꺼내와서 조립하기

    //d. repository에 넘겨서 데베에 넣기

    //2. 회원 단건 조회


}
