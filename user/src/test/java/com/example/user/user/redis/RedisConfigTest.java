package com.example.user.user.redis;

import com.example.user.redis.RedisConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("local")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(
        initializers = {ConfigDataApplicationContextInitializer.class},
        classes = RedisConfig.class)
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