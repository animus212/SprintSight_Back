package com.example.sprintsight.services;

import com.example.sprintsight.dtos.requests.UpdateUserRequest;
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
    private final RefreshTokenService refreshTokenService;

    public UserResponse getUser(UUID id) {
        return userMapper.toUserResponse(findUser(id));
    }

    @Transactional
    public UserResponse addUser(RegisterRequest request) {
        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.password()));

        return saveUser(user, "Registered new user");
    }

    @Transactional
    public UserResponse updateUser(UpdateUserRequest request, UUID id, boolean isPut) {
        User user = findUser(id);

        if (isPut) {
            userMapper.updateUserFromPut(request, user);
        }
        else {
            userMapper.updateUserFromPatch(request, user);
        }

        if (request.password() != null) {
            user.setPassword(passwordEncoder.encode(request.password()));

            refreshTokenService.deleteByUserId(id);
        }

        return saveUser(user, "Updated user");
    }

    @Transactional
    public void deleteUser(UUID id) {
        User user = findUser(id);

        userRepository.delete(user);

        log.info("Deleted user: {}", id);
    }

    private User findUser(UUID id) {
        return userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    private UserResponse saveUser(User user, String logMessage) {
        User savedUser = userRepository.save(user);

        log.info("{}: {}", logMessage, savedUser.getId());

        return userMapper.toUserResponse(savedUser);
    }
}
