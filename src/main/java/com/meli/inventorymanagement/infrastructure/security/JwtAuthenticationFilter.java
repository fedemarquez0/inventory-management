package com.meli.inventorymanagement.infrastructure.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements WebFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        // Skip JWT validation for auth endpoints and public endpoints
        if (path.startsWith("/api/auth/") ||
            path.startsWith("/swagger-ui") ||
            path.startsWith("/v3/api-docs") ||
            path.startsWith("/webjars/") ||
            path.startsWith("/actuator/")) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("No JWT token found in request to: {}", path);
            return chain.filter(exchange);
        }

        String jwt = authHeader.substring(7);

        return Mono.fromCallable(() -> jwtUtil.extractUsername(jwt))
                .flatMap(username -> userDetailsService.findByUsername(username)
                        .flatMap(userDetails -> {
                            if (jwtUtil.validateToken(jwt, username)) {
                                UsernamePasswordAuthenticationToken authentication =
                                        new UsernamePasswordAuthenticationToken(
                                                userDetails,
                                                null,
                                                userDetails.getAuthorities()
                                        );

                                log.debug("JWT token validated successfully for user: {}", username);

                                return chain.filter(exchange)
                                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
                            } else {
                                log.warn("Invalid JWT token for user: {}", username);
                                return chain.filter(exchange);
                            }
                        })
                        .onErrorResume(e -> {
                            log.error("Error during JWT authentication: {}", e.getMessage());
                            return chain.filter(exchange);
                        })
                )
                .onErrorResume(e -> {
                    log.error("Error extracting username from token: {}", e.getMessage());
                    return chain.filter(exchange);
                });
    }
}
