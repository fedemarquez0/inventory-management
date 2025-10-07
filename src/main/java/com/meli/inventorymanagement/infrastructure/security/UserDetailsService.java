package com.meli.inventorymanagement.infrastructure.security;

import com.meli.inventorymanagement.common.constant.ErrorCode;
import com.meli.inventorymanagement.domain.exception.BusinessException;
import com.meli.inventorymanagement.domain.port.UserPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsService {

    private final UserPort userPort;

    public Mono<UserDetails> findByUsername(String username) {
        return userPort.findByUsername(username)
                .switchIfEmpty(Mono.error(() -> {
                    log.warn("User not found: {}", username);
                    return new BusinessException(ErrorCode.USER_NOT_FOUND, "User not found: " + username);
                }))
                .flatMap(user -> {
                    if (!user.getIsActive()) {
                        log.warn("Attempt to authenticate with inactive user: {}", username);
                        return Mono.error(new BusinessException(ErrorCode.USER_ACCOUNT_INACTIVE,
                                "User account is inactive: " + username));
                    }

                    List<SimpleGrantedAuthority> authorities = List.of(
                            new SimpleGrantedAuthority("ROLE_" + user.getRole())
                    );

                    return Mono.just(org.springframework.security.core.userdetails.User.builder()
                            .username(user.getUsername())
                            .password(user.getPasswordHash())
                            .authorities(authorities)
                            .build());
                });
    }
}
