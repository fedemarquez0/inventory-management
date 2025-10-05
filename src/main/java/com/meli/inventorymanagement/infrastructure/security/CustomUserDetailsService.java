package com.meli.inventorymanagement.infrastructure.security;

import com.meli.inventorymanagement.domain.model.User;
import com.meli.inventorymanagement.infrastructure.adapter.output.persistence.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomUserDetailsService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        if (!user.getIsActive()) {
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
            if (user == null || !user.getIsActive()) {
                return false;
            }
            return passwordEncoder.matches(password, user.getPasswordHash());
        } catch (Exception e) {
            // Log the error and return false
            System.err.println("Authentication error for user " + username + ": " + e.getMessage());
            return false;
        }
    }

    public boolean hasStorePermission(String username, Long storeId) {
        try {
            return userRepository.hasStorePermission(username, storeId);
        } catch (Exception e) {
            System.err.println("Store permission check error for user " + username + ": " + e.getMessage());
            return false;
        }
    }
}
