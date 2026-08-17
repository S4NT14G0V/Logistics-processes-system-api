package com.backend.couriersyncfeat4.config.ratelimit;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConfigurationBuilder;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RedisRateLimitBucketProvider implements RateLimitBucketProvider {

    private final RedisClient redisClient;
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private volatile ProxyManager<byte[]> proxyManager;

    public RedisRateLimitBucketProvider(RedisClient redisClient) {
        this.redisClient = redisClient;
    }

    @Override
    public Bucket resolve(String key, long capacity, Duration refillDuration) {
        return buckets.computeIfAbsent(key, k -> {
            BucketConfiguration configuration = new ConfigurationBuilder()
                    .addLimit(limit -> limit.capacity(capacity).refillGreedy(capacity, refillDuration))
                    .build();
            return proxyManager().builder().build(k.getBytes(StandardCharsets.UTF_8), configuration);
        });
    }

    private ProxyManager<byte[]> proxyManager() {
        ProxyManager<byte[]> pm = proxyManager;
        if (pm == null) {
            synchronized (this) {
                pm = proxyManager;
                if (pm == null) {
                    pm = LettuceBasedProxyManager.builderFor(redisClient).build();
                    proxyManager = pm;
                }
            }
        }
        return pm;
    }
}
