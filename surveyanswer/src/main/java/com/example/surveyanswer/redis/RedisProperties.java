package com.example.surveyanswer.redis;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@ConfigurationProperties(prefix = "spring.redis")
@Component
public class RedisProperties {
    @Value("${spring.cache.redis.host}")
    private String host;
    @Value("${spring.cache.redis.port}")
    private int port;
}
