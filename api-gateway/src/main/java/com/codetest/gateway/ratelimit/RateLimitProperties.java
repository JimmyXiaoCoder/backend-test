package com.codetest.gateway.ratelimit;

import java.time.Duration;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "gateway.rate-limit")
public class RateLimitProperties {

    private boolean enabled = true;

    // 每个限流 key 在一个窗口内允许通过的最大请求数。
    private int maxRequests = 100;

    // 固定窗口长度，例如 1m、30s；由 Spring Boot 自动绑定为 Duration。
    private Duration window = Duration.ofMinutes(1);

    // 当前默认使用 URL，USER_ID 和 IP 用于后续扩展。
    private KeyType keyType = KeyType.URL;

    private Set<String> excludedPaths = new HashSet<>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getMaxRequests() {
        return maxRequests;
    }

    public void setMaxRequests(int maxRequests) {
        this.maxRequests = maxRequests;
    }

    public Duration getWindow() {
        return window;
    }

    public void setWindow(Duration window) {
        this.window = window;
    }

    public KeyType getKeyType() {
        return keyType;
    }

    public void setKeyType(KeyType keyType) {
        this.keyType = keyType;
    }

    public Set<String> getExcludedPaths() {
        return Collections.unmodifiableSet(excludedPaths);
    }

    public void setExcludedPaths(Set<String> excludedPaths) {
        this.excludedPaths = excludedPaths == null ? new HashSet<>() : new HashSet<>(excludedPaths);
    }

    public enum KeyType {
        URL,
        USER_ID,
        IP
    }
}
