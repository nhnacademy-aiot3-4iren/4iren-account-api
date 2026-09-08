package com.nhnacademy.accountapi.service;

import com.nhnacademy.accountapi.exception.MailSendFailedException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MailServiceTest {

    @Mock
    private JavaMailSender javaMailSender;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private MailService mailService;

    @BeforeEach
    void setUp() {
        // @Value 필드 수동 주입
        ReflectionTestUtils.setField(mailService, "fromEmail", "admin@4iren.com");
    }

    @Test
    @DisplayName("1. 임시 비밀번호 이메일 발송 성공")
    void sendTemporaryPassword_Success() {
        // given
        String toEmail = "user@test.com";
        String tempPassword = "TEMP_PASSWORD";

        given(javaMailSender.createMimeMessage()).willReturn(mimeMessage);
        doNothing().when(javaMailSender).send(any(MimeMessage.class));

        // when
        mailService.sendTemporaryPassword(toEmail, tempPassword);

        // then
        verify(javaMailSender, times(1)).createMimeMessage();
        verify(javaMailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("2. 발신자 이메일(fromEmail)이 비어있으면 발송을 무시하고 리턴")
    void sendTemporaryPassword_SenderEmailBlank() {
        // given
        ReflectionTestUtils.setField(mailService, "fromEmail", "");
        String toEmail = "user@test.com";
        String tempPassword = "TEMP_PASSWORD";

        // when
        mailService.sendTemporaryPassword(toEmail, tempPassword);

        // then
        // fromEmail이 비어있으면 createMimeMessage() 호출 자체를 안 함
        verify(javaMailSender, never()).createMimeMessage();
        verify(javaMailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("3. 메일 발송 중 예외 발생 시 MailSendFailedException 던짐")
    void sendTemporaryPassword_ThrowsMailSendFailedException() {
        // given
        String toEmail = "user@test.com";
        String tempPassword = "TEMP_PASSWORD";

        given(javaMailSender.createMimeMessage()).willReturn(mimeMessage);
        // send() 시 예외 발생시키기
        doThrow(new RuntimeException("SMTP Server Down"))
                .when(javaMailSender).send(any(MimeMessage.class));

        // when & then
        assertThatThrownBy(() -> mailService.sendTemporaryPassword(toEmail, tempPassword))
                .isInstanceOf(MailSendFailedException.class)
                .hasMessage("이메일 발송에 실패했습니다.");
    }
}
