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

/**
 * Servicio personalizado para manejar la autenticación y autorización de usuarios de forma reactiva.
 * Proporciona métodos para cargar usuarios, autenticar y verificar permisos de tienda.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Busca un usuario por su nombre de usuario y lo convierte en UserDetails de Spring Security.
     *
     * @param username El nombre de usuario a buscar
     * @return Mono con UserDetails del usuario encontrado
     */
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

    /**
     * Autentica un usuario verificando su contraseña.
     *
     * @param username El nombre de usuario
     * @param password La contraseña en texto plano
     * @return Mono<Boolean> true si la autenticación es exitosa, false en caso contrario
     */
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

    /**
     * Verifica si un usuario tiene permiso para acceder a una tienda específica.
     * Los usuarios ADMIN tienen acceso a todas las tiendas automáticamente.
     *
     * @param username El nombre de usuario
     * @param storeId El ID de la tienda
     * @return Mono<Boolean> true si el usuario tiene permiso, false en caso contrario
     */
    public Mono<Boolean> hasStorePermission(String username, Long storeId) {
        return userRepository.hasStorePermission(username, storeId)
                .doOnError(e -> log.error("Error checking store permission for user {} and store {}: {}",
                        username, storeId, e.getMessage(), e))
                .onErrorReturn(false);
    }
}

