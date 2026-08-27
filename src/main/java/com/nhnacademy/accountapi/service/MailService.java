package com.nhnacademy.accountapi.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender javaMailSender;

    // SMTP 로그인 계정과 실제 보내는 사람(From) 주소가 다를 수 있으므로 별도 분리
    @Value("${sender.address:${spring.sender.address:${mail.sender.address:${spring.mail.username:}}}}")
    private String fromEmail;

    /**
     * 임시 비밀번호를 이메일로 전송합니다.
     *
     * @param toEmail 수신자 이메일
     * @param temporaryPassword 발급된 임시 비밀번호
     */
    @Async
    public void sendTemporaryPassword(String toEmail, String temporaryPassword) {
        if (fromEmail == null || fromEmail.isBlank()) {
            log.warn("이메일 발신자(spring.mail.username)가 설정되지 않아 메일을 전송할 수 없습니다.");
            return;
        }

        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("[4iren] 임시 비밀번호 발급 안내");

            // HTML 템플릿 작성
            String htmlContent = """
                    <div style="font-family: 'Apple SD Gothic Neo', 'Noto Sans KR', sans-serif; max-width: 600px; margin: 0 auto; padding: 40px 20px; background-color: #f9f9f9;">
                        <div style="background-color: #ffffff; padding: 40px; border-radius: 12px; box-shadow: 0 4px 6px rgba(0,0,0,0.05); text-align: center;">
                            <h1 style="color: #333333; font-size: 24px; margin-bottom: 20px;">임시 비밀번호 발급 안내</h1>
                            <p style="color: #666666; font-size: 16px; line-height: 1.6; margin-bottom: 30px;">
                                안녕하세요.<br>요청하신 <strong>임시 비밀번호</strong>가 성공적으로 발급되었습니다.<br>아래의 비밀번호를 사용하여 로그인해 주세요.
                            </p>
                            <div style="background-color: #f0f4f8; border: 1px solid #dce4ec; border-radius: 8px; padding: 20px; margin-bottom: 30px;">
                                <span style="font-size: 28px; font-weight: bold; color: #2c3e50; letter-spacing: 2px;">%s</span>
                            </div>
                            <p style="color: #e74c3c; font-size: 14px; font-weight: 500; margin-top: 20px;">
                                ⚠️ 보안을 위해 로그인 후 반드시 비밀번호를 변경해 주시기 바랍니다.
                            </p>
                            <div style="margin-top: 40px; padding-top: 20px; border-top: 1px solid #eeeeee;">
                                <p style="color: #999999; font-size: 12px; margin: 0;">
                                    본 메일은 발신 전용이며, 회신되지 않습니다.<br>
                                    © 2026 4iren. All rights reserved.
                                </p>
                            </div>
                        </div>
                    </div>
                    """.formatted(temporaryPassword);

            helper.setText(htmlContent, true); // true indicates HTML content

            javaMailSender.send(mimeMessage);
            log.info("임시 비밀번호 HTML 이메일 발송 완료: {}", toEmail);
        } catch (Exception e) {
            log.error("임시 비밀번호 HTML 이메일 발송 실패: {}", toEmail, e);
            throw new RuntimeException("이메일 발송에 실패했습니다.", e);
        }
    }
}
