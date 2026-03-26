package com.example.sprintsight.services;

import com.example.sprintsight.dtos.requests.UpdateUserPatchRequest;
import com.example.sprintsight.dtos.requests.UpdateUserPutRequest;
import com.example.sprintsight.entities.User;
import com.example.sprintsight.exceptions.*;
import com.example.sprintsight.dtos.requests.RegisterRequest;
import com.example.sprintsight.dtos.responses.UserResponse;
import com.example.sprintsight.mappers.UserMapper;
import com.example.sprintsight.repositories.UserRepository;
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
    private final UserMapper userMapper;

    public UserResponse getUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return userMapper.toUserResponse(user);
    }

    @Transactional
    public UserResponse addUser(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new UsernameAlreadyExistsException("Username is already taken");
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException("Email is already registered");
        }

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.password()));

        User saved = userRepository.save(user);

        log.info("Registered new user: {}", saved.getId());

        return userMapper.toUserResponse(user);
    }

    @Transactional
    public UserResponse updateUserPut(UpdateUserPutRequest request) {
        User user = userRepository.findById(request.id())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        validateUsernameChange(user, request.username());

        validateEmailChange(user, request.email());

        userMapper.updateUserFromPut(request, user);

        if (request.password() != null) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }

        User saved = userRepository.save(user);

        log.info("Updated user: {}", saved.getId());

        return userMapper.toUserResponse(saved);
    }

    @Transactional
    public UserResponse updateUserPatch(UpdateUserPatchRequest request) {
        User user = userRepository.findById(request.id())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        validateUsernameChange(user, request.username());

        validateEmailChange(user, request.email());

        userMapper.updateUserFromPatch(request, user);

        if (request.password() != null) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }

        return userMapper.toUserResponse(user);
    }

    @Transactional
    public void deleteUser(UUID id) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found"));

        userRepository.delete(user);

        log.info("Deleted user: {}", id);
    }

    private void validateUsernameChange(User user, String newUsername) {
        if (newUsername == null || user.getUsername().equals(newUsername)) return;

        if (userRepository.existsByUsername(newUsername)) {
            throw new UsernameAlreadyExistsException("Username already taken");
        }
    }

    private void validateEmailChange(User user, String newEmail) {
        if (newEmail == null || user.getEmail().equals(newEmail)) return;

        if (userRepository.existsByEmail(newEmail)) {
            throw new EmailAlreadyExistsException("Email is already registered");
        }
    }
}
