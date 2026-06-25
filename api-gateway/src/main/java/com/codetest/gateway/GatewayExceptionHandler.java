package com.codetest.gateway;

import com.codetest.common.Result;
import com.codetest.common.ResultCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Order(-1)
@Configuration
@RequiredArgsConstructor
public class GatewayExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Result<Void> result;
        if (ex instanceof org.springframework.web.server.ResponseStatusException rse) {
            response.setStatusCode(rse.getStatusCode());
            result = Result.fail(rse.getStatusCode().value(), rse.getReason());
        } else {
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            result = Result.fail(ResultCode.INTERNAL_ERROR);
        }

        log.error("[Gateway异常] {} -> ", exchange.getRequest().getPath(), ex);

        return response.writeWith(Mono.fromSupplier(() -> {
            try {
                byte[] bytes = objectMapper.writeValueAsBytes(result);
                return response.bufferFactory().wrap(bytes);
            } catch (JsonProcessingException e) {
                return response.bufferFactory().wrap(new byte[0]);
            }
        }));
    }
}
