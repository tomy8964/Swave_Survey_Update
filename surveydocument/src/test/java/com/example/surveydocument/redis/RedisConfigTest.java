package com.example.surveydocument.redis;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class RedisConfigTest {
    @Autowired
    private RedisConnectionFactory redisConnectionFactory;
    @Autowired
    private CacheManager cacheManager;

    @Test
    public void redisConnectionFactoryBeanExists() {
        assertThat(redisConnectionFactory).isNotNull();
    }

    @Test
    public void cacheManagerBeanExists() {
        assertThat(cacheManager).isNotNull();
    }
}