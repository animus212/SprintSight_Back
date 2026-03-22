package com.example.SprintSight.Services;

import com.example.SprintSight.Entities.User;
import com.example.SprintSight.Exceptions.*;
import com.example.SprintSight.Payloads.Requests.RegisterRequest;
import com.example.SprintSight.Payloads.Requests.UpdateUserRequest;
import com.example.SprintSight.Payloads.Responses.UserResponse;
import com.example.SprintSight.Repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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
