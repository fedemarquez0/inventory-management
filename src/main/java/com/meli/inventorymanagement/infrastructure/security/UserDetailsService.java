package com.meli.inventorymanagement.infrastructure.security;

import com.meli.inventorymanagement.infrastructure.adapter.output.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Mono<UserDetails> findByUsername(String username) {
        return userRepository.findByUsername(username)
                .switchIfEmpty(Mono.error(() -> {
                    log.warn("User not found: {}", username);
                    return new org.springframework.security.core.userdetails.UsernameNotFoundException("User not found: " + username);
                }))
                .flatMap(user -> {
                    if (!user.getIsActive()) {
                        log.warn("Attempt to authenticate with inactive user: {}", username);
                        return Mono.error(new org.springframework.security.core.userdetails.UsernameNotFoundException("User is not active: " + username));
                    }

                    List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole()));

                    return Mono.just(org.springframework.security.core.userdetails.User.builder()
                            .username(user.getUsername())
                            .password(user.getPasswordHash())
                            .authorities(authorities)
                            .build());
                });
    }

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

    public Mono<Boolean> hasStorePermission(String username, Long storeId) {
        return userRepository.hasStorePermission(username, storeId)
                .doOnError(e -> log.error("Error checking store permission for user {} and store {}: {}",
                        username, storeId, e.getMessage(), e))
                .onErrorReturn(false);
    }
}

