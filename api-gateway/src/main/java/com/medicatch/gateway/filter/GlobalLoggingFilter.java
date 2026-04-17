package com.medicatch.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 게이트웨이를 통과하는 모든 요청/응답을 로깅하는 전역 필터.
 * 최상위 우선순위로 실행되어 실제 라우팅 전/후 시간을 측정한다.
 */
@Slf4j
@Component
public class GlobalLoggingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String method = request.getMethod().name();
        String path   = request.getPath().value();
        long start    = System.currentTimeMillis();

        log.info("[→] {} {}", method, path);

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            var status  = exchange.getResponse().getStatusCode();
            long elapsed = System.currentTimeMillis() - start;
            log.info("[←] {} {} {} ({}ms)",
                    method, path,
                    status != null ? status.value() : "?",
                    elapsed);
        }));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
