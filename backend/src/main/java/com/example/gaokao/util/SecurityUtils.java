package com.example.gaokao.util;

import com.example.gaokao.domain.User;
import com.example.gaokao.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Long getUserId(Authentication authentication, UserService userService) {
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
