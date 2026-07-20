package com.nhnacademy.accountapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.web.SecurityFilterChain;

@Configuration
//@EnableWebSecurity //스프링 시큐리티의 웹 보안 기능을 활성화함
public class SecurityConfig {

    // 비번 암호화 기계(bean) 등록
    // 서비스(MemberService)에서 사용할 암호화 인코더임
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    // API 주소별 대문 개방 및 잠금 설정
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
//        http
//                .csrf(csrf-> csrf.disable())
//                .authorizeHttpRequests(authorize-> authorize
//                .requestMatchers("/api/register","/api/login").permitAll()
//                .requestMatchers("/api/members/**").authenticated()
//                .anyRequest().authenticated()
//                );
//        return http.build();
//    }



}
