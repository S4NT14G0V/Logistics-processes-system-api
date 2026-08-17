package com.backend.couriersyncfeat4.config.ratelimit;

import io.github.bucket4j.Bucket;

import java.time.Duration;

/**
 * Resuelve el bucket asociado a una clave (IP o usuario). La implementación es
 * distribuida con Redis ({@link RedisRateLimitBucketProvider}).
 */
public interface RateLimitBucketProvider {

    Bucket resolve(String key, long capacity, Duration refillDuration);
}
