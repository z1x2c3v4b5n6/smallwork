package com.example.gaokao.controller.student;

import com.example.gaokao.domain.StudentProfile;
import com.example.gaokao.domain.User;
import com.example.gaokao.service.StudentProfileService;
import com.example.gaokao.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student/profile")
public class StudentProfileController {

    private final StudentProfileService studentProfileService;
    private final UserService userService;

    public StudentProfileController(StudentProfileService studentProfileService, UserService userService) {
        this.studentProfileService = studentProfileService;
        this.userService = userService;
    }

    @GetMapping
    public StudentProfile profile(Authentication authentication) {
        Long userId = getUserId(authentication);
        if (userId == null) {
            return null;
        }
        return studentProfileService.lambdaQuery().eq(StudentProfile::getUserId, userId).one();
    }

    private Long getUserId(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        String username;
        if (principal instanceof UserDetails userDetails) {
            username = userDetails.getUsername();
        } else if (principal instanceof User user) {
            username = user.getUsername();
        } else {
            username = principal.toString();
        }
        User user = userService.lambdaQuery().eq(User::getUsername, username).one();
        return user != null ? user.getId() : null;
    }
}
