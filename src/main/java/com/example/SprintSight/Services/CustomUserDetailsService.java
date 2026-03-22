package com.example.SprintSight.Services;

import com.example.SprintSight.Repositories.UserRepository;
import com.example.SprintSight.Security.UserPrincipal;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    @NonNull
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        log.debug("Loading user by username: {}", username);

        return userRepository.findByUsername(username)
                .map(UserPrincipal::from)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid username or password"));
    }
}
