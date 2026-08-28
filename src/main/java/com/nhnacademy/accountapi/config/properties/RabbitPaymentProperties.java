package com.nhnacademy.accountapi.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "rabbitmq.payment.role-change")
public class RabbitPaymentProperties {
    private String exchange;
    private String routingKey;
    private String queue;
    private String deadLetterExchange;
    private String deadLetterRoutingKey;
    private String deadLetterQueue;
}
