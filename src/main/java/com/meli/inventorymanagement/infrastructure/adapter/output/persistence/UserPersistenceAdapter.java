package com.meli.inventorymanagement.infrastructure.adapter.output.persistence;

import com.meli.inventorymanagement.domain.model.User;
import com.meli.inventorymanagement.domain.port.UserPort;
import com.meli.inventorymanagement.infrastructure.adapter.output.persistence.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserPersistenceAdapter implements UserPort {

    private final UserRepository userRepository;

    @Override
    public Mono<User> findByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return Mono.empty();
        }

        return userRepository.findByUsername(username)
                .map(this::toDomain)
                .doOnError(error -> log.error("Error finding user by username {}: {}", username, error.getMessage()));
    }

    private User toDomain(UserEntity entity) {
        if (entity == null) {
            return null;
        }

        return User.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .passwordHash(entity.getPasswordHash())
                .role(entity.getRole())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
