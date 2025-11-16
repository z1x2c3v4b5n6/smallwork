package com.example.gaokao.controller.admin;

import com.example.gaokao.domain.StudentProfile;
import com.example.gaokao.domain.User;
import com.example.gaokao.domain.dto.ResetPasswordRequest;
import com.example.gaokao.domain.dto.StudentOverview;
import com.example.gaokao.domain.dto.StudentProfileRequest;
import com.example.gaokao.domain.dto.StudentStatusRequest;
import com.example.gaokao.service.StudentProfileService;
import com.example.gaokao.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/students")
public class StudentAdminController {

    private final UserService userService;
    private final StudentProfileService studentProfileService;
    private final PasswordEncoder passwordEncoder;

    public StudentAdminController(UserService userService, StudentProfileService studentProfileService,
                                  PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.studentProfileService = studentProfileService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public List<StudentOverview> list(@RequestParam(required = false) String username,
                                      @RequestParam(required = false) String realName,
                                      @RequestParam(required = false) String province,
                                      @RequestParam(required = false) Integer minScore,
                                      @RequestParam(required = false) Integer maxScore) {
        List<User> students = userService.lambdaQuery()
                .eq(User::getRole, "ROLE_STUDENT")
                .like(StringUtils.hasText(username), User::getUsername, username)
                .list();
        List<Long> userIds = students.stream().map(User::getId).collect(Collectors.toList());
        Map<Long, StudentProfile> profileMap = studentProfileService.lambdaQuery()
                .in(!userIds.isEmpty(), StudentProfile::getUserId, userIds)
                .list()
                .stream()
                .collect(Collectors.toMap(StudentProfile::getUserId, p -> p));
        return students.stream()
                .map(user -> {
                    StudentProfile profile = profileMap.get(user.getId());
                    return StudentOverview.builder()
                            .userId(user.getId())
                            .username(user.getUsername())
                            .realName(profile != null ? profile.getRealName() : null)
                            .nickname(profile != null ? profile.getNickname() : null)
                            .province(profile != null ? profile.getProvince() : null)
                            .score(profile != null ? profile.getScore() : null)
                            .rank(profile != null ? profile.getRank() : null)
                            .firstMockScore(profile != null ? profile.getFirstMockScore() : null)
                            .secondMockScore(profile != null ? profile.getSecondMockScore() : null)
                            .subjects(profile != null ? profile.getSubjects() : null)
                            .targetMajorType(profile != null ? profile.getTargetMajorType() : null)
                            .enabled(user.getEnabled())
                            .lastLoginTime(user.getLastLoginTime())
                            .build();
                })
                .filter(item -> !StringUtils.hasText(realName) || (item.getRealName() != null && item.getRealName().contains(realName)))
                .filter(item -> !StringUtils.hasText(province) || province.equals(item.getProvince()))
                .filter(item -> minScore == null || (item.getScore() != null && item.getScore() >= minScore))
                .filter(item -> maxScore == null || (item.getScore() != null && item.getScore() <= maxScore))
                .collect(Collectors.toList());
    }

    @PutMapping("/{userId}/profile")
    public StudentProfile updateProfile(@PathVariable Long userId, @RequestBody StudentProfileRequest request) {
        User user = validateStudent(userId);
        StudentProfile profile = studentProfileService.lambdaQuery().eq(StudentProfile::getUserId, user.getId()).one();
        if (profile == null) {
            profile = new StudentProfile();
            profile.setUserId(user.getId());
        }
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
        studentProfileService.saveOrUpdate(profile);
        return profile;
    }

    @PostMapping("/{userId}/reset-password")
    public void resetPassword(@PathVariable Long userId, @RequestBody(required = false) ResetPasswordRequest request) {
        User user = validateStudent(userId);
        String newPassword = request != null && StringUtils.hasText(request.getNewPassword())
                ? request.getNewPassword() : "123456";
        user.setPassword(passwordEncoder.encode(newPassword));
        userService.updateById(user);
    }

    @PatchMapping("/{userId}/status")
    public void updateStatus(@PathVariable Long userId, @RequestBody StudentStatusRequest request) {
        User user = validateStudent(userId);
        if (request.getEnabled() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "缺少启用状态");
        }
        user.setEnabled(request.getEnabled());
        userService.updateById(user);
    }

    private User validateStudent(Long userId) {
        User user = userService.getById(userId);
        if (user == null || !"ROLE_STUDENT".equals(user.getRole())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "学生不存在");
        }
        return user;
    }
}
