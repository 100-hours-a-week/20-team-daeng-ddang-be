package com.daengddang.daengdong_map.config;

import java.time.Duration;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
public class RedissonConfig {

    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    @Value("${spring.data.redis.password:}")
    private String password;

    @Value("${spring.data.redis.database:0}")
    private int database;

    @Value("${spring.data.redis.ssl.enabled:false}")
    private boolean sslEnabled;

    @Value("${spring.data.redis.timeout:3s}")
    private Duration timeout;

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        Config config = new Config();
        String protocol = sslEnabled ? "rediss://" : "redis://";

        SingleServerConfig single = config.useSingleServer()
                .setAddress(protocol + host + ":" + port)
                .setDatabase(database)
                .setConnectionPoolSize(64)
                .setConnectionMinimumIdleSize(16)
                .setRetryAttempts(3)
                .setPingConnectionInterval(30000);

        if (StringUtils.hasText(password)) {
            single.setPassword(password);
        }

        if (timeout != null) {
            int timeoutMillis = (int) timeout.toMillis();
            single.setConnectTimeout(timeoutMillis);
            single.setTimeout(timeoutMillis);
        }

        return Redisson.create(config);
    }
}
