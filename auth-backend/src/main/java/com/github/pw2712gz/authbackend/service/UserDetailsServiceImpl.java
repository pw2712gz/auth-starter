package com.github.pw2712gz.authbackend.service;

import com.github.pw2712gz.authbackend.entity.User;
import com.github.pw2712gz.authbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

/**
 * Custom implementation of Spring Security's UserDetailsService.
 * Loads users by email for authentication.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) {
        log.debug("[UserDetailsService] Loading user by email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("[UserDetailsService] User not found: {}", email);
                    return new UsernameNotFoundException("No user found with email: " + email);
                });

        log.debug("[UserDetailsService] Found user: {} (enabled: {})", user.getEmail(), user.isEnabled());

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                user.isEnabled(),
                true,  // accountNonExpired
                true,  // credentialsNonExpired
                true,  // accountNonLocked
                Collections.emptyList() // authorities
        );
    }
}
