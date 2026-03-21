package com.daengddang.daengdong_map.config;

import com.daengddang.daengdong_map.analysis.AnalysisRabbitMqProperties;
import com.daengddang.daengdong_map.analysis.AnalysisTaskRetryMessageRecoverer;
import org.aopalliance.aop.Advice;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "analysis.rabbitmq", name = "enabled", havingValue = "true")
public class AnalysisRabbitMqConfig {

    @Bean
    public TopicExchange analysisTaskExchange(AnalysisRabbitMqProperties properties) {
        return new TopicExchange(properties.getExchange(), true, false);
    }

    @Bean
    public TopicExchange analysisTaskRetryExchange(AnalysisRabbitMqProperties properties) {
        return new TopicExchange(properties.getRetryExchange(), true, false);
    }

    @Bean
    public Queue analysisTaskQueue(AnalysisRabbitMqProperties properties) {
        return QueueBuilder.durable(properties.getQueue())
                .deadLetterExchange(properties.getDeadLetterExchange())
                .deadLetterRoutingKey(properties.getDeadLetterRoutingKey())
                .build();
    }

    @Bean
    public Queue analysisTaskRetryQueue(AnalysisRabbitMqProperties properties) {
        return QueueBuilder.durable(properties.getRetryQueue())
                .ttl((int) properties.getRetryDelayMs())
                .deadLetterExchange(properties.getExchange())
                .deadLetterRoutingKey(properties.getRoutingKey())
                .build();
    }

    @Bean
    public TopicExchange analysisTaskDeadLetterExchange(AnalysisRabbitMqProperties properties) {
        return new TopicExchange(properties.getDeadLetterExchange(), true, false);
    }

    @Bean
    public Queue analysisTaskDeadLetterQueue(AnalysisRabbitMqProperties properties) {
        return QueueBuilder.durable(properties.getDeadLetterQueue()).build();
    }

    @Bean
    public Binding analysisTaskBinding(
            Queue analysisTaskQueue,
            TopicExchange analysisTaskExchange,
            AnalysisRabbitMqProperties properties
    ) {
        return BindingBuilder.bind(analysisTaskQueue)
                .to(analysisTaskExchange)
                .with(properties.getRoutingKey());
    }

    @Bean
    public Binding analysisTaskRetryBinding(
            Queue analysisTaskRetryQueue,
            TopicExchange analysisTaskRetryExchange,
            AnalysisRabbitMqProperties properties
    ) {
        return BindingBuilder.bind(analysisTaskRetryQueue)
                .to(analysisTaskRetryExchange)
                .with(properties.getRetryRoutingKey());
    }

    @Bean
    public Binding analysisTaskDeadLetterBinding(
            Queue analysisTaskDeadLetterQueue,
            TopicExchange analysisTaskDeadLetterExchange,
            AnalysisRabbitMqProperties properties
    ) {
        return BindingBuilder.bind(analysisTaskDeadLetterQueue)
                .to(analysisTaskDeadLetterExchange)
                .with(properties.getDeadLetterRoutingKey());
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);
        rabbitAdmin.setAutoStartup(true);
        return rabbitAdmin;
    }

    @Bean
    public MessageConverter rabbitMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory connectionFactory,
            MessageConverter rabbitMessageConverter
    ) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(rabbitMessageConverter);
        return rabbitTemplate;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter rabbitMessageConverter,
            AnalysisTaskRetryMessageRecoverer analysisTaskRetryMessageRecoverer
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(rabbitMessageConverter);
        factory.setAcknowledgeMode(AcknowledgeMode.AUTO);
        factory.setDefaultRequeueRejected(false);
        Advice retryAdvice = RetryInterceptorBuilder.stateless()
                .maxRetries(1)
                .recoverer(analysisTaskRetryMessageRecoverer)
                .build();
        factory.setAdviceChain(retryAdvice);
        return factory;
    }
}
