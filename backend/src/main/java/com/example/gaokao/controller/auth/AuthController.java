package com.example.gaokao.controller.auth;

import com.example.gaokao.config.JwtTokenProvider;
import com.example.gaokao.domain.StudentProfile;
import com.example.gaokao.domain.User;
import com.example.gaokao.domain.dto.LoginRequest;
import com.example.gaokao.domain.dto.LoginResponse;
import com.example.gaokao.domain.dto.RegisterRequest;
import com.example.gaokao.service.StudentProfileService;
import com.example.gaokao.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final StudentProfileService studentProfileService;

    public AuthController(AuthenticationManager authenticationManager, JwtTokenProvider tokenProvider, UserService userService,
                          PasswordEncoder passwordEncoder, StudentProfileService studentProfileService) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.studentProfileService = studentProfileService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        User user = userService.lambdaQuery().eq(User::getUsername, request.getUsername()).one();
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "账号不存在");
        }
        user.setLastLoginTime(java.time.LocalDateTime.now());
        userService.updateById(user);
        String token = tokenProvider.generateToken(user.getUsername(), user.getRole());
        return new LoginResponse(token, user.getUsername(), user.getRole());
    }

    @PostMapping("/register")
    public LoginResponse register(@Valid @RequestBody RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "两次密码输入不一致");
        }
        boolean exists = userService.lambdaQuery().eq(User::getUsername, request.getUsername()).exists();
        if (exists) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "用户名已被占用");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("ROLE_STUDENT");
        user.setEnabled(true);
        userService.save(user);

        StudentProfile profile = new StudentProfile();
        profile.setUserId(user.getId());
        profile.setRealName(request.getRealName());
        profile.setNickname(request.getNickname());
        profile.setScore(request.getScore());
        profile.setRank(request.getRank());
        profile.setFirstMockScore(request.getFirstMockScore());
        profile.setFirstMockRank(request.getFirstMockRank());
        profile.setSecondMockScore(request.getSecondMockScore());
        profile.setSecondMockRank(request.getSecondMockRank());
        profile.setSubjects(request.getSubjects());
        profile.setProvince(request.getProvince());
        profile.setTargetMajorType(request.getTargetMajorType());
        studentProfileService.save(profile);

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
