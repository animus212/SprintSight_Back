package com.example.SprintSight.RestControllers;

import com.example.SprintSight.Entities.User;
import com.example.SprintSight.Services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final AuthenticationManager authManager;
    private final UserService userService;

    @PostMapping("/register")
    public User register(@RequestBody User newUser, HttpServletRequest request){

         newUser = userService.registerNewUser(newUser);

       // Authentication authentication = authManager.authenticate(new UsernamePasswordAuthenticationToken(newUser,null));
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("USER")); // replace with actual roles if needed

// Create an authenticated token manually
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(newUser, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            HttpSession session = request.getSession(true);
            session.setAttribute("SPRING_SECURITY_CONTEXT",SecurityContextHolder.getContext());

        return newUser;
    }
}
