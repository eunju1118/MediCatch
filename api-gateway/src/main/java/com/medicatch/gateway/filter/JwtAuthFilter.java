package com.medicatch.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicatch.gateway.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * JWT Bearer 토큰 검증 게이트웨이 필터.
 *
 * <p>동작:</p>
 * <ol>
 *   <li>Authorization: Bearer {token} 헤더 추출</li>
 *   <li>HS256 서명 + 만료시간 검증</li>
 *   <li>성공 시 X-User-Id / X-User-Role 헤더를 하위 서비스에 전달</li>
 *   <li>실패 시 401 JSON 응답 반환 (토큰 없음 / 만료 / 위변조 구분)</li>
 * </ol>
 */
@Slf4j
@Component
public class JwtAuthFilter extends AbstractGatewayFilterFactory<JwtAuthFilter.Config> {

    private final JwtProperties jwtProperties;
    private final ObjectMapper  objectMapper;

    public JwtAuthFilter(JwtProperties jwtProperties, ObjectMapper objectMapper) {
        super(Config.class);
        this.jwtProperties = jwtProperties;
        this.objectMapper  = objectMapper;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path       = request.getPath().value();
            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("JWT 없음: {} {}", request.getMethod(), path);
                return onError(exchange, HttpStatus.UNAUTHORIZED, "인증 토큰이 필요합니다");
            }

            String token = authHeader.substring(7);
            try {
                Claims claims = parseToken(token);

                String userId = claims.getSubject();
                String role   = claims.get("role", String.class);

                log.debug("JWT 인증 성공: userId={}, role={}, path={}", userId, role, path);

                ServerHttpRequest mutated = request.mutate()
                        .header("X-User-Id",   userId != null ? userId : "")
                        .header("X-User-Role", role   != null ? role   : "")
                        .build();

                return chain.filter(exchange.mutate().request(mutated).build());

            } catch (ExpiredJwtException e) {
                log.warn("JWT 만료: path={}", path);
                return onError(exchange, HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다");
            } catch (JwtException e) {
                log.warn("JWT 검증 실패: path={}, error={}", path, e.getMessage());
                return onError(exchange, HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다");
            }
        };
    }

    // ── 내부 유틸 ──────────────────────────────────────────────────────────

    private Claims parseToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(
                jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus status, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", false);
        body.put("message", message);
        body.put("data",    null);

        try {
            byte[]     bytes  = objectMapper.writeValueAsBytes(body);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            return response.setComplete();
        }
    }

    public static class Config {
        // 라우트별 필터 설정이 필요할 때 필드 추가
    }
}
