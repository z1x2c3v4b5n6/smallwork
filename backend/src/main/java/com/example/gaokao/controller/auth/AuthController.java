package com.example.gaokao.controller.auth;

import com.example.gaokao.config.JwtTokenProvider;
import com.example.gaokao.domain.User;
import com.example.gaokao.domain.dto.LoginRequest;
import com.example.gaokao.domain.dto.LoginResponse;
import com.example.gaokao.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserService userService;

    public AuthController(AuthenticationManager authenticationManager, JwtTokenProvider tokenProvider, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.userService = userService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        User user = userService.lambdaQuery().eq(User::getUsername, request.getUsername()).one();
        String token = tokenProvider.generateToken(user.getUsername(), user.getRole());
        return new LoginResponse(token, user.getUsername(), user.getRole());
    }

    @GetMapping("/me")
    public LoginResponse me(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        String username;
        String role;
        if (principal instanceof UserDetails userDetails) {
            username = userDetails.getUsername();
            role = userDetails.getAuthorities().stream().findFirst().map(Object::toString).orElse("");
        } else if (principal instanceof User user) {
            username = user.getUsername();
            role = user.getRole();
        } else {
            username = principal.toString();
            role = "";
        }
        return new LoginResponse(null, username, role);
    }
}
