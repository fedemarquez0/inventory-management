package com.meli.inventorymanagement.infrastructure.adapter.output.persistence;

import com.meli.inventorymanagement.domain.port.AuthenticationPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationAdapter implements AuthenticationPort {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Mono<Boolean> authenticate(String username, String password) {
        return userRepository.findByUsername(username)
                .flatMap(user -> {
                    if (!user.getIsActive()) {
                        log.warn("Authentication failed: User is inactive: {}", username);
                        return Mono.just(false);
                    }
                    boolean matches = passwordEncoder.matches(password, user.getPasswordHash());
                    if (!matches) {
                        log.warn("Authentication failed: Invalid password for user: {}", username);
                    }
                    return Mono.just(matches);
                })
                .defaultIfEmpty(false)
                .doOnError(e -> log.error("Authentication error for user {}: {}", username, e.getMessage(), e))
                .onErrorReturn(false);
    }

    @Override
    public Mono<Boolean> hasStorePermission(String username, Long storeId) {
        return userRepository.hasStorePermission(username, storeId)
                .doOnError(e -> log.error("Error checking store permission for user {} and store {}: {}",
                        username, storeId, e.getMessage(), e))
                .onErrorReturn(false);
    }
}
