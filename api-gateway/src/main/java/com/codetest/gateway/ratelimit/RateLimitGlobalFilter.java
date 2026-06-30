package com.codetest.gateway.ratelimit;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.codetest.common.Result;
import com.codetest.common.ResultCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
@EnableConfigurationProperties(RateLimitProperties.class)
public class RateLimitGlobalFilter implements GlobalFilter, Ordered {

    private final RateLimitProperties properties;
    private final ObjectMapper objectMapper;
    private final Clock clock = Clock.systemUTC();
    private final Map<String, RequestCounter> counters = new ConcurrentHashMap<>();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 网关最前置统一拦截，关闭限流或命中白名单时直接放行，不影响后续路由规则。
        if (!properties.isEnabled() || isExcluded(exchange.getRequest())) {
            return chain.filter(exchange);
        }

        String key = buildLimitKey(exchange.getRequest());
        // 使用固定时间窗口计数：窗口过期后替换为新的计数器，未过期则沿用当前计数器。
        RequestCounter counter = counters.compute(key, (ignored, current) -> current == null || current.isExpired(clock.millis())
                ? RequestCounter.create(clock.millis(), properties.getWindow().toMillis())
                : current);

        int currentCount = counter.incrementAndGet();
        if (currentCount <= properties.getMaxRequests()) {
            addRateLimitHeaders(exchange.getResponse(), counter, currentCount);
            return chain.filter(exchange);
        }

        log.warn("[Gateway限流] key={}, path={}, count={}, limit={}", key, exchange.getRequest().getPath(),
                currentCount, properties.getMaxRequests());
        return writeRateLimitedResponse(exchange.getResponse(), counter);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    private boolean isExcluded(ServerHttpRequest request) {
        String path = request.getPath().pathWithinApplication().value();
        return properties.getExcludedPaths().stream().anyMatch(path::startsWith);
    }

    private String buildLimitKey(ServerHttpRequest request) {
        // 默认按 URL 限流；USER_ID 和 IP 的分支先保留，后续只需调整配置即可切换限流维度。
        return switch (properties.getKeyType()) {
            case URL -> "url:" + request.getMethod() + ":" + request.getPath().pathWithinApplication().value();
            case USER_ID -> "user:" + resolveUserId(request);
            case IP -> "ip:" + resolveClientIp(request);
        };
    }

    private String resolveUserId(ServerHttpRequest request) {
        String userId = request.getHeaders().getFirst("X-User-Id");
        return userId == null || userId.isBlank() ? "anonymous" : userId;
    }

    private String resolveClientIp(ServerHttpRequest request) {
        String forwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddress() == null ? "unknown" : request.getRemoteAddress().getAddress().getHostAddress();
    }

    private void addRateLimitHeaders(ServerHttpResponse response, RequestCounter counter, int currentCount) {
        int remaining = Math.max(properties.getMaxRequests() - currentCount, 0);
        response.getHeaders().set("X-RateLimit-Limit", String.valueOf(properties.getMaxRequests()));
        response.getHeaders().set("X-RateLimit-Remaining", String.valueOf(remaining));
        response.getHeaders().set("X-RateLimit-Reset", String.valueOf(counter.getResetEpochSecond()));
    }

    private Mono<Void> writeRateLimitedResponse(ServerHttpResponse response, RequestCounter counter) {
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        // 告诉调用方建议多久后重试，同时保持项目统一的 Result 响应格式。
        response.getHeaders().set(HttpHeaders.RETRY_AFTER, String.valueOf(counter.getRetryAfterSeconds(clock.millis())));
        addRateLimitHeaders(response, counter, properties.getMaxRequests());

        byte[] bytes = serialize(Result.fail(ResultCode.TOO_MANY_REQUESTS));
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }

    private byte[] serialize(Result<Void> result) {
        try {
            return objectMapper.writeValueAsBytes(result);
        } catch (JsonProcessingException e) {
            return "{\"code\":429,\"message\":\"请求过于频繁，请稍后再试\"}".getBytes(StandardCharsets.UTF_8);
        }
    }

    private static class RequestCounter {
        private final long resetAtMillis;
        private final AtomicInteger count = new AtomicInteger();

        private RequestCounter(long resetAtMillis) {
            this.resetAtMillis = resetAtMillis;
        }

        private static RequestCounter create(long nowMillis, long windowMillis) {
            return new RequestCounter(nowMillis + windowMillis);
        }

        private boolean isExpired(long nowMillis) {
            return nowMillis >= resetAtMillis;
        }

        private int incrementAndGet() {
            return count.incrementAndGet();
        }

        private long getResetEpochSecond() {
            return resetAtMillis / 1000;
        }

        private long getRetryAfterSeconds(long nowMillis) {
            return Math.max((resetAtMillis - nowMillis + 999) / 1000, 1);
        }
    }
}
