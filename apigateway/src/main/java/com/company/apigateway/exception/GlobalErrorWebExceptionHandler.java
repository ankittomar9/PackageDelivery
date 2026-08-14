package com.company.apigateway.exception;

import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Configuration
@Order(-2) // High priority error handler in WebFlux pipeline
public class GlobalErrorWebExceptionHandler implements ErrorWebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        exchange.getResponse().setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String jsonError = String.format(
                "{\"status\": 503, \"error\": \"Service Unavailable\", \"message\": \"Target microservice is temporarily unreachable or starting up: %s\", \"path\": \"%s\"}",
                ex.getMessage() != null ? ex.getMessage().replace("\"", "'") : "Connection Failure",
                exchange.getRequest().getURI().getPath()
        );

        byte[] bytes = jsonError.getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);

        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
}