package com.example.reactivespring.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.util.annotation.NonNull;

@Component
@Slf4j
public class RequestLoggingFilter implements WebFilter {

    @Override
    @NonNull
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        var request = exchange.getRequest();
        
        log.info("Request: {} {} \nHeaders: {} \nQuery Params: {}", 
            request.getMethod(),
            request.getPath(),
            request.getHeaders(),
            request.getQueryParams()
        );

        return chain.filter(exchange);
    }
}