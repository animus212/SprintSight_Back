package com.example.SprintSight.Services;

import com.example.SprintSight.Entities.User;
import com.example.SprintSight.Repositories.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserPrincipalService implements UserDetailsService {
    private final UserRepo userRepository;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user;
        user = userRepository.findByUsername(username);
        if(user.isEmpty()){
            throw new UsernameNotFoundException("user not found");
        }
        return new UserPrincipal(user.get());
    }
}
