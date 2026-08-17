package com.backend.couriersyncfeat4.config.ratelimit;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@ConfigurationProperties(prefix = "app.rate-limit")
public class RateLimitProperties {

    private boolean enabled = true;

    /** Capacidad del bucket de autenticación (login/register) por IP. */
    private long authCapacity = 10;

    /** Capacidad del bucket general (graphql/rest) por usuario. */
    private long apiCapacity = 120;

    /** Ventana de recarga de los buckets. */
    private Duration refillDuration = Duration.ofMinutes(1);

    /** Profundidad máxima de una query GraphQL. */
    private int graphqlMaxDepth = 10;

    /** Complejidad máxima de una query GraphQL. */
    private int graphqlMaxComplexity = 100;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public long getAuthCapacity() {
        return authCapacity;
    }

    public void setAuthCapacity(long authCapacity) {
        this.authCapacity = authCapacity;
    }

    public long getApiCapacity() {
        return apiCapacity;
    }

    public void setApiCapacity(long apiCapacity) {
        this.apiCapacity = apiCapacity;
    }

    public Duration getRefillDuration() {
        return refillDuration;
    }

    public void setRefillDuration(Duration refillDuration) {
        this.refillDuration = refillDuration;
    }

    public int getGraphqlMaxDepth() {
        return graphqlMaxDepth;
    }

    public void setGraphqlMaxDepth(int graphqlMaxDepth) {
        this.graphqlMaxDepth = graphqlMaxDepth;
    }

    public int getGraphqlMaxComplexity() {
        return graphqlMaxComplexity;
    }

    public void setGraphqlMaxComplexity(int graphqlMaxComplexity) {
        this.graphqlMaxComplexity = graphqlMaxComplexity;
    }
}
