package com.daengddang.daengdong_map.analysis;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "analysis.rabbitmq")
public class AnalysisRabbitMqProperties {

    private boolean enabled = false;

    private String exchange = "analysis.task.exchange";

    private String queue = "analysis.task.queue";

    private String routingKey = "analysis.task.created";

    private String retryExchange = "analysis.task.retry.exchange";

    private String retryQueue = "analysis.task.retry.queue";

    private String retryRoutingKey = "analysis.task.retry";

    private long retryDelayMs = 60000L;

    private int retryMaxAttempts = 1;

    private String deadLetterExchange = "analysis.task.dlx";

    private String deadLetterQueue = "analysis.task.dlq";

    private String deadLetterRoutingKey = "analysis.task.failed";
}
