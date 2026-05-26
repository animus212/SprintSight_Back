package com.example.sprintsight.security;

import com.example.sprintsight.services.CustomUserDetailsService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String jwt = jwtService.getJwtFromCookies(request);

        if (jwt == null || jwt.isBlank()) {
            filterChain.doFilter(request, response);

            return;
        }

        try {
            String username = jwtService.extractUsername(jwt);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (jwtService.validateJwtToken(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());

                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (ExpiredJwtException e) {
            log.warn("Expired JWT for {} {}: {}", request.getMethod(), request.getRequestURI(), e.getMessage());

            clearJwtCookie(response);
        } catch (JwtException e) {
            log.warn("Invalid JWT for {} {}: {}", request.getMethod(), request.getRequestURI(), e.getMessage());

            clearJwtCookie(response);
        } catch (UsernameNotFoundException e) {
            log.warn("JWT for deleted user on {} {}: {}", request.getMethod(), request.getRequestURI(), e.getMessage());

            clearJwtCookie(response);
        }

        filterChain.doFilter(request, response);
    }

    private void clearJwtCookie(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, jwtService.getCleanJwtCookie().toString());
    }

    @Configuration
    static class JwtFilterRegistration {
        @Bean
        FilterRegistrationBean<JwtFilter> disableAutoRegistration(JwtFilter filter) {
            FilterRegistrationBean<JwtFilter> reg = new FilterRegistrationBean<>(filter);

            reg.setEnabled(false);

            return reg;
        }
    }
}
