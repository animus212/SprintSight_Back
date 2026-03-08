package com.example.SprintSight.Controllers;

import com.example.SprintSight.DTOs.LoginDTO;
import com.example.SprintSight.Entities.User;
import com.example.SprintSight.Services.JWTService;
import com.example.SprintSight.Services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;

    @PostMapping("/register")
    public String register(@Valid @RequestBody User user, HttpServletRequest httpServletRequest) {
        user = userService.AddUser(user);

        return jwtService.generateToken(user.getUsername());
    }

    @PostMapping("/login")
    public String login(@Valid @RequestBody LoginDTO loginDTO, HttpServletRequest httpServletRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword())
        );

        if (authentication.isAuthenticated()){
            return jwtService.generateToken(loginDTO.getUsername());
        }
        else{
            return "failed";
        }



//        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
//
//        SecurityContextHolder.getContext().setAuthentication(authentication);
//
//        httpServletRequest.getSession(true)
//                .setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
//
//        assert principal != null;
//        return ResponseEntity.ok(principal.getUser());
    }

    @GetMapping("/hello")
    public String hello(){
        return "halloo";
    }
}
