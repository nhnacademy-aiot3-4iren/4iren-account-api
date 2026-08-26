package com.nhnacademy.accountapi.config;

import com.nhnacademy.accountapi.config.properties.RabbitAccountProperties;
import com.nhnacademy.accountapi.config.properties.RabbitPaymentProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RabbitConfig {

    private final RabbitPaymentProperties paymentProperties;
    private final RabbitAccountProperties accountProperties;

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }

    // Payment 권한 변경 이벤트 수신용 큐 및 익스체인지
    @Bean
    public Queue paymentRoleChangeQueue() {
        return QueueBuilder.durable(paymentProperties.getQueue())
                .withArgument("x-dead-letter-exchange", paymentProperties.getDeadLetterExchange())
                .withArgument("x-dead-letter-routing-key", paymentProperties.getDeadLetterRoutingKey())
                .build();
    }

    @Bean
    public TopicExchange paymentExchange() {
        return new TopicExchange(paymentProperties.getExchange());
    }

    @Bean
    public Binding bindingPaymentQueue(Queue paymentRoleChangeQueue, TopicExchange paymentExchange) {
        return BindingBuilder.bind(paymentRoleChangeQueue).to(paymentExchange).with(paymentProperties.getRoutingKey());
    }

    // Payment DLQ 설정
    @Bean
    public Queue paymentDeadLetterQueue() {
        return QueueBuilder.durable(paymentProperties.getDeadLetterQueue()).build();
    }

    @Bean
    public TopicExchange paymentDeadLetterExchange() {
        return new TopicExchange(paymentProperties.getDeadLetterExchange());
    }

    @Bean
    public Binding paymentDeadLetterBinding(Queue paymentDeadLetterQueue, TopicExchange paymentDeadLetterExchange) {
        return BindingBuilder.bind(paymentDeadLetterQueue).to(paymentDeadLetterExchange).with(paymentProperties.getDeadLetterRoutingKey());
    }

    // Account Role Change 이벤트 발행용 익스체인지
    @Bean
    public TopicExchange accountExchange() {
        return new TopicExchange(accountProperties.getExchange());
    }
}
