package com.example.sprintsight.services;

import com.example.sprintsight.entities.User;
import com.example.sprintsight.exceptions.*;
import com.example.sprintsight.dtos.requests.RegisterRequest;
import com.example.sprintsight.dtos.requests.UpdateUserRequest;
import com.example.sprintsight.dtos.responses.UserResponse;
import com.example.sprintsight.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponse getUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse addUser(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new UsernameAlreadyExistsException("Username is already taken");
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException("Email is already registered");
        }

        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName());
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());

        User saved = userRepository.save(user);

        log.info("Registered new user: {}", saved.getId());

        return UserResponse.from(saved);
    }

    @Transactional
    public UserResponse updateUser(UpdateUserRequest request) {
        User user = userRepository.findById(request.id())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!user.getUsername().equals(request.username()) && userRepository.existsByUsername(request.username())) {
            throw new UsernameAlreadyExistsException("Username is already taken");
        }

        if (!user.getEmail().equals(request.email()) && userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException("Email is already registered");
        }

        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setFullName(request.fullName());
        user.setBio(request.bio());
        user.setUpdatedAt(Instant.now());

        if (request.password() != null) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }

        User saved = userRepository.save(user);

        log.info("Updated user: {}", saved.getId());

        return UserResponse.from(saved);
    }

    @Transactional
    public void deleteUser(UUID id) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found"));

        userRepository.delete(user);

        log.info("Deleted user: {}", id);
    }
}
