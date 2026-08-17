package com.backend.couriersyncfeat4.config.ratelimit;

import io.lettuce.core.RedisClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cliente Lettuce de Redis para el rate limiting distribuido.
 */
@Configuration
public class RedisRateLimitConfig {

    @Bean(destroyMethod = "shutdown")
    public RedisClient rateLimitRedisClient(
            @Value("${app.rate-limit.redis-url:redis://localhost:6379}") String url) {
        return RedisClient.create(url);
    }
}
