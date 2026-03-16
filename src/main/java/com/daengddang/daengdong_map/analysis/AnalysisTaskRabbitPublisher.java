package com.daengddang.daengdong_map.analysis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "analysis.rabbitmq", name = "enabled", havingValue = "true")
public class AnalysisTaskRabbitPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final AnalysisRabbitMqProperties properties;
    private final AnalysisTaskRabbitMetrics analysisTaskRabbitMetrics;

    public void publish(AnalysisTaskMessage message) {
        try {
            rabbitTemplate.convertAndSend(properties.getExchange(), properties.getRoutingKey(), message);
            analysisTaskRabbitMetrics.recordPublishSuccess();
            log.info("분석 작업 메시지 발행 완료. taskId={}, type={}, traceId={}, exchange={}, routingKey={}",
                    message.taskId(), message.type(), message.traceId(),
                    properties.getExchange(), properties.getRoutingKey());
        } catch (RuntimeException ex) {
            analysisTaskRabbitMetrics.recordPublishFail();
            throw ex;
        }
    }
}
