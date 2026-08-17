package com.backend.couriersyncfeat4.config.ratelimit;

import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.backend.couriersyncfeat4.integration.Operation;
import com.backend.couriersyncfeat4.integration.OperationLoggerExtension;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(OperationLoggerExtension.class)
class RateLimitFilterTest {

    private RateLimitProperties properties() {
        RateLimitProperties props = new RateLimitProperties();
        props.setEnabled(true);
        props.setAuthCapacity(1);
        props.setApiCapacity(1);
        props.setRefillDuration(Duration.ofMinutes(1));
        return props;
    }

    private RateLimitBucketProvider localBucketProvider() {
        Map<String, Bucket> buckets = new ConcurrentHashMap<>();
        return (key, capacity, refillDuration) -> buckets.computeIfAbsent(key, k -> Bucket.builder()
                .addLimit(limit -> limit.capacity(capacity).refillGreedy(capacity, refillDuration))
                .build());
    }

    @Test
    @Operation("Auth Limit Exceeded (429 - Too many requests)")
    void shouldReturn429WhenAuthLimitExceeded() throws Exception {
        RateLimitFilter filter = new RateLimitFilter(properties(), localBucketProvider());
        FilterChain chain = mock(FilterChain.class);

        MockHttpServletResponse res1 = new MockHttpServletResponse();
        filter.doFilter(new MockHttpServletRequest("POST", "/auth/login"), res1, chain);
        assertThat(res1.getStatus()).isEqualTo(200);

        MockHttpServletResponse res2 = new MockHttpServletResponse();
        filter.doFilter(new MockHttpServletRequest("POST", "/auth/login"), res2, chain);
        assertThat(res2.getStatus()).isEqualTo(429);
        assertThat(res2.getContentAsString()).contains("TOO_MANY_REQUESTS");

        verify(chain, times(1)).doFilter(any(), any());
    }

    @Test
    @Operation("Rate Limit skips the /events endpoint")
    void shouldSkipEventsEndpoint() throws Exception {
        RateLimitFilter filter = new RateLimitFilter(properties(), localBucketProvider());
        FilterChain chain = mock(FilterChain.class);

        MockHttpServletResponse res = new MockHttpServletResponse();
        filter.doFilter(new MockHttpServletRequest("GET", "/events"), res, chain);

        assertThat(res.getStatus()).isEqualTo(200);
        verify(chain, times(1)).doFilter(any(), any());
    }

    @Test
    @Operation("When rate limit is disabled then it should pass through")
    void shouldPassThroughWhenDisabled() throws Exception {
        RateLimitProperties props = properties();
        props.setEnabled(false);
        RateLimitFilter filter = new RateLimitFilter(props, localBucketProvider());
        FilterChain chain = mock(FilterChain.class);

        MockHttpServletResponse res1 = new MockHttpServletResponse();
        filter.doFilter(new MockHttpServletRequest("POST", "/auth/login"), res1, chain);
        MockHttpServletResponse res2 = new MockHttpServletResponse();
        filter.doFilter(new MockHttpServletRequest("POST", "/auth/login"), res2, chain);

        assertThat(res1.getStatus()).isEqualTo(200);
        assertThat(res2.getStatus()).isEqualTo(200);
        verify(chain, times(2)).doFilter(any(), any());
    }
}
