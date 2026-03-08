package com.example.SprintSight.Services;

import com.example.SprintSight.DTOs.UserDTO;
import com.example.SprintSight.Entities.User;
import com.example.SprintSight.Repositories.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    @Transactional
    public User AddUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username is already taken");
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email is already registered");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(UserDTO user) {
        User convertedUser;

        if (userRepository.existsById(user.getId())) {
            throw new IllegalArgumentException("Email is already registered");
        }
        Optional<User> userToUpdate = userRepository.findById(user.getId());
        if(user.getPassword() != null){
             convertedUser = objectMapper.convertValue(user, User.class);
            return userRepository.save(convertedUser);
        }else{
            objectMapper.updateValue(userToUpdate.get(), user);
            userToUpdate.get().setPassword(passwordEncoder.encode(user.getPassword()));
            return userRepository.save(userToUpdate.get());
        }
    }

    public void deleteUser(@Valid UUID id) {
        userRepository.deleteById(id);
    }
}
