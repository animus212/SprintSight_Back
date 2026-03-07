package com.example.SprintSight.Controllers;

import com.example.SprintSight.Entities.User;
import com.example.SprintSight.Security.UserPrincipal;
import com.example.SprintSight.Services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/register")
    public User register(@Valid @RequestBody User user, HttpServletRequest httpRequest) {
         user = userService.AddUser(user);

        UserPrincipal principal = new UserPrincipal(user);

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        httpRequest.getSession(true)
                .setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

        return user;
    }
}
