package com.nhnacademy.accountapi.controller;

import com.nhnacademy.accountapi.dto.*;
import com.nhnacademy.accountapi.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController //이 클래스가 JSON 데이터를 반환하는 REST API 대문임을 선언
@RequestMapping("/users") //이 컨트롤러의 모든 주소는 "/users"로 시작함
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    //회원가입(POST/users/signup)
    @PostMapping("/signup")
    public ResponseEntity<Response> signUp(@RequestBody RegisterRequest request){
        Response response= userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    //로그인(POST/ users/login)
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request){
        LoginResponse response= userService.login(request);
        return ResponseEntity.ok(response);
    }

    //회원 정보 수정(PUT/ users/{userId}
    @PutMapping("/{userId}")
    public ResponseEntity<Response> updateUser(
            @PathVariable Long userId,
            @RequestBody UpdateRequest request

    ){
        Response response = userService.updateUser(userId,request);
        return ResponseEntity.ok(response);
    }

    //전체 회원 조회(GET/users)
//    @GetMapping
//    public ResponseEntity<List<Response>> getAllUsers(){
//        List<Response> responseList= userService.getAllUsers();
//        return ResponseEntity.ok(responseList);
//    }

    //단건 회원 조회(GET/users/user/{userLoginId}
    @GetMapping
    public ResponseEntity<Response> getUser(@PathVariable String userLoginId){
        Response response=userService.getUser(userLoginId);
        return ResponseEntity.ok(response);
    }

    //회원 탈퇴(PATCH/users/{userId}/withdraw)
    @PatchMapping
    public ResponseEntity<Void> withdraw(@PathVariable Long userId){
        userService.withdraw(userId);
        return ResponseEntity.noContent().build();
    }
}
