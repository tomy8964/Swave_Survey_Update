package com.example.user.user.redis;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.util.StringUtils;
import redis.embedded.RedisServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Duration;

@Slf4j
@Profile("test")
@Configuration
public class TestRedisConfig {
    private int redisPort = 6379;

    private RedisServer redisServer;
    private int changedPort;

    @PostConstruct
    public void startRedis() throws IOException {
        changedPort = isRedisRunning() ? findAvailablePort() : redisPort;
        redisServer = RedisServer.builder()
                .port(changedPort)
                .setting("maxmemory 128M")
                .build();
        try {
            redisServer.start();
        } catch (Exception e) {
            log.error("Redis Server Can't Start", e);
        }
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory("localhost", changedPort);
    }

    @Bean
    public CacheManager cacheManager() {
        RedisCacheManager.RedisCacheManagerBuilder builder =
                RedisCacheManager.RedisCacheManagerBuilder.fromConnectionFactory(redisConnectionFactory());
        RedisCacheConfiguration configuration = RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new GenericJackson2JsonRedisSerializer())) // Value Serializer 변경
                .entryTtl(Duration.ofMinutes(30)); // 캐시 수명 30분
        builder.cacheDefaults(configuration);
        return builder.build();
    }

    @PreDestroy
    public void stopRedis() {
        redisServer.stop();
    }

    public int findAvailablePort() throws IOException {
        for (int port = 10000; port <= 65535; port++) {
            Process process = executeGrepProcessCommand(port);
            if (!isRunning(process)) {
                return port;
            }
        }

        throw new RuntimeException("NOT_FOUND_AVAILABLE_PORT");
    }

    /**
     * Embedded Redis가 현재 실행중인지 확인
     */
    private boolean isRedisRunning() throws IOException {
        return isRunning(executeGrepProcessCommand(redisPort));
    }

    /**
     * 해당 port를 사용중인 프로세스를 확인하는 sh 실행
     */
    private Process executeGrepProcessCommand(int redisPort) throws IOException {
        String command = String.format("netstat -nat | grep LISTEN|grep %d", redisPort);

        // linux 용
        // String[] shell = {"/bin/sh", "-c", command};
        // window 용
        String[] shell = {"cmd.exe", "/c", command};

        return Runtime.getRuntime().exec(shell);
    }

    /**
     * 해당 Process가 현재 실행중인지 확인
     */
    private boolean isRunning(Process process) {
        String line;
        StringBuilder pidInfo = new StringBuilder();

        try (BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            while ((line = input.readLine()) != null) {
                pidInfo.append(line);
            }
        } catch (Exception e) {
            throw new RuntimeException("ERROR_EXECUTING_EMBEDDED_REDIS");
        }
        return StringUtils.hasText(pidInfo.toString());
    }
}