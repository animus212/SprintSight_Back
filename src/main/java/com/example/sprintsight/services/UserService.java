package com.example.sprintsight.services;

import com.example.sprintsight.dtos.requests.UserRequest;
import com.example.sprintsight.dtos.responses.ImageUrlResponse;
import com.example.sprintsight.dtos.responses.UserResponse;
import com.example.sprintsight.dtos.responses.UserSummaryResponse;
import com.example.sprintsight.entities.User;
import com.example.sprintsight.exceptions.BusinessRuleViolationException;
import com.example.sprintsight.exceptions.ResourceConflictException;
import com.example.sprintsight.mappers.UserMapper;
import com.example.sprintsight.repositories.ProjectRepository;
import com.example.sprintsight.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProjectRepository projectRepository;
    private final RefreshTokenService refreshTokenService;
    private final CloudinaryImageService cloudinaryImageService;

    @Transactional(readOnly = true)
    public UserResponse getUser(UUID id) {
        return userMapper.toUserResponse(findUser(id));
    }

    @Transactional(readOnly = true)
    public UserSummaryResponse getUserByUsername(String username) {
        return userMapper.toUserSummaryResponse(userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found")));
    }

    @Transactional
    public UserResponse addUser(UserRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new ResourceConflictException("Username already taken");
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new ResourceConflictException("Email already registered");
        }

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.password()));

        User saved = userRepository.save(user);

        log.info("Registered new user: {}", saved.getId());

        return userMapper.toUserResponse(saved);
    }

    @Transactional
    public UserResponse updateUser(UserRequest request, UUID id) {
        User user = findUser(id);

        if (request.username() != null
                && !request.username().equals(user.getUsername())
                && userRepository.existsByUsername(request.username())) {
            throw new ResourceConflictException("Username already taken");
        }

        if (request.email() != null
                && !request.email().equals(user.getEmail())
                && userRepository.existsByEmail(request.email())) {
            throw new ResourceConflictException("Email already registered");
        }

        userMapper.updateUserFromRequest(request, user);

        if (request.password() != null) {
            user.setPassword(passwordEncoder.encode(request.password()));

            refreshTokenService.deleteByUserId(id);
        }

        User saved = userRepository.save(user);

        log.info("Updated user: {}", saved.getId());

        return userMapper.toUserResponse(saved);
    }

    @Transactional
    public void deleteUser(UUID id) {
        User user = findUser(id);

        if (!projectRepository.findByCreatedBy_Id(id).isEmpty()) {
            throw new BusinessRuleViolationException(
                    "User owns projects — reassign or delete them before removing this account");
        }

        refreshTokenService.deleteByUserId(id);

        cloudinaryImageService.deleteByUrl(user.getProfilePictureUrl());

        userRepository.delete(user);

        log.info("Deleted user: {}", id);
    }

    @Transactional
    public ImageUrlResponse updateProfilePicture(UUID userId, MultipartFile file) {
        User user = findUser(userId);
        String oldUrl = user.getProfilePictureUrl();

        String newUrl = cloudinaryImageService.uploadAvatar(file).url();
        user.setProfilePictureUrl(newUrl);
        userRepository.save(user);

        cloudinaryImageService.deleteByUrl(oldUrl);

        log.info("Updated profile picture for user {}", userId);
        return new ImageUrlResponse(newUrl);
    }


    @Transactional
    public void deleteProfilePicture(UUID userId) {
        User user = findUser(userId);
        String oldUrl = user.getProfilePictureUrl();

        if (oldUrl == null || oldUrl.isBlank()) {
            return;
        }

        user.setProfilePictureUrl(null);
        userRepository.save(user);

        cloudinaryImageService.deleteByUrl(oldUrl);
        log.info("Removed profile picture for user {}", userId);
    }

    public User findUser(UUID id) {
        return userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("User not found"));
    }
}
