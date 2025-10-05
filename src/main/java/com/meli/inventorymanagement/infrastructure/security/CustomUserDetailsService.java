package com.meli.inventorymanagement.infrastructure.security;

import com.meli.inventorymanagement.domain.model.User;
import com.meli.inventorymanagement.infrastructure.adapter.output.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("User not found: {}", username);
                    return new UsernameNotFoundException("User not found: " + username);
                });

        if (!user.getIsActive()) {
            log.warn("Attempt to authenticate with inactive user: {}", username);
            throw new UsernameNotFoundException("User is not active: " + username);
        }

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPasswordHash())
                .authorities(authorities)
                .build();
    }

    public boolean authenticate(String username, String password) {
        try {
            User user = userRepository.findByUsername(username).orElse(null);
            if (user == null) {
                log.warn("Authentication failed: User not found: {}", username);
                return false;
            }
            if (!user.getIsActive()) {
                log.warn("Authentication failed: User is inactive: {}", username);
                return false;
            }
            boolean matches = passwordEncoder.matches(password, user.getPasswordHash());
            if (!matches) {
                log.warn("Authentication failed: Invalid password for user: {}", username);
            }
            return matches;
        } catch (Exception e) {
            log.error("Authentication error for user {}: {}", username, e.getMessage(), e);
            return false;
        }
    }

    public boolean hasStorePermission(String username, Long storeId) {
        try {
            return userRepository.hasStorePermission(username, storeId);
        } catch (Exception e) {
            log.error("Error checking store permission for user {} and store {}: {}",
                     username, storeId, e.getMessage(), e);
            return false;
        }
    }
}
