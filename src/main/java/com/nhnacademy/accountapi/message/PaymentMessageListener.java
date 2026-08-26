package com.nhnacademy.accountapi.message;

import com.nhnacademy.accountapi.dto.message.PaymentCompleteMessage;
import com.nhnacademy.accountapi.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentMessageListener {

    private final UserService userService;

    @RabbitListener(queues = "${rabbitmq.payment.role-change.queue:account.payment.role-change.queue}")
    public void handlePaymentCompleteEvent(PaymentCompleteMessage message) {
        log.info("결제 완료 이벤트 수신 - userId: {}, role: {}, jti: {}", message.userId(), message.role(), message.jti());

        // 1. DB 권한 업데이트 (트랜잭션 환경에서 실행 후 즉시 커밋)
        userService.updateUserRole(message.userId(), message.role());
        
        // 2. 이벤트 발행 (DB 커넥션 반환 후 실행되므로, RabbitMQ 지연이 DB에 영향을 주지 않음)
        userService.publishRoleChangeEvent(message.userId(), message.role(), message.jti());
    }
}
