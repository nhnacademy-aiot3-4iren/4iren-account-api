package com.nhnacademy.accountapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.accountapi.controller.UserController;
import com.nhnacademy.accountapi.dto.RegisterRequest;
import com.nhnacademy.accountapi.entity.User;
import com.nhnacademy.accountapi.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.assertj.core.api.BDDAssumptions.given;

import static org.springframework.mock.http.server.reactive.MockServerHttpRequest.post;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
 class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;
    // 자바 객체를 json으로 변환해줌

    @Autowired
    @MockitoBean
    private UserService userService;

    @Test
    @DisplayName("회원 가입 API 요청 시 정상적으로 201응답과 데이터를 내려받는다")
    void signUp_Api_Success()throws Exception{
        //given
        User requestUser= new User();
        requestUser.setUserName("apiUser");
        requestUser.setUserPassword("password123");

        User responseUser= new User();
        responseUser.setUserId(1L);
        responseUser.setUserName("apiUser");
        responseUser.setUserPassword("encryptedPassword123");

        given(userService.register(any(RegisterRequest.class))).willReturn(responseUser);

        //when&then
        mockMvc.perform(post("/api/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(responseUser)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.username").value("apiUser"));

    }

}
