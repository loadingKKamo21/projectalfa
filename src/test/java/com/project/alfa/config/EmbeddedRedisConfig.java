package com.project.alfa.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import redis.embedded.RedisServer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Slf4j
@Profile("test")
@Configuration
public class EmbeddedRedisConfig {
    
    @Value("${spring.redis.port}")
    private int         port;
    private RedisServer redisServer;
    
    @PostConstruct
    public void startRedis() {
        redisServer = new RedisServer(port);
        redisServer.start();
    }
    
    @PreDestroy
    public void stopRedis() {
        if (redisServer != null) redisServer.stop();
    }
    
}
