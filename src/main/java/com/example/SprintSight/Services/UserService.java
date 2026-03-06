package com.example.SprintSight.Services;

import com.example.SprintSight.Entities.User;
import com.example.SprintSight.Repositories.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;

    public User registerNewUser(User newUser){
        newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));
        return userRepo.save(newUser);

    }
}
