package com.backend.couriersyncfeat4.config;

import com.backend.couriersyncfeat4.config.ratelimit.RateLimitProperties;
import graphql.analysis.MaxQueryComplexityInstrumentation;
import graphql.analysis.MaxQueryDepthInstrumentation;
import org.springframework.boot.autoconfigure.graphql.GraphQlSourceBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class GraphQlLimitsConfig {

    @Bean
    public GraphQlSourceBuilderCustomizer graphQlLimitsCustomizer(RateLimitProperties properties) {
        return builder -> builder.instrumentation(List.of(
                new MaxQueryDepthInstrumentation(properties.getGraphqlMaxDepth()),
                new MaxQueryComplexityInstrumentation(properties.getGraphqlMaxComplexity())));
    }
}
