package com.example.gaokao.controller.student;

import com.example.gaokao.domain.StudentProfile;
import com.example.gaokao.domain.dto.RecommendResponse;
import com.example.gaokao.domain.dto.StudentProfileRequest;
import com.example.gaokao.domain.dto.StudentProfileSummaryResponse;
import com.example.gaokao.service.RecommendationEngine;
import com.example.gaokao.service.StudentProfileService;
import com.example.gaokao.service.UserService;
import com.example.gaokao.util.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/student/profile")
public class StudentProfileController {

    private final StudentProfileService studentProfileService;
    private final UserService userService;
    private final RecommendationEngine recommendationEngine;

    public StudentProfileController(StudentProfileService studentProfileService, UserService userService,
                                    RecommendationEngine recommendationEngine) {
        this.studentProfileService = studentProfileService;
        this.userService = userService;
        this.recommendationEngine = recommendationEngine;
    }

    @GetMapping
    public StudentProfile profile(Authentication authentication) {
        Long userId = SecurityUtils.getUserId(authentication, userService);
        if (userId == null) {
            return null;
        }
        return studentProfileService.lambdaQuery().eq(StudentProfile::getUserId, userId).one();
    }

    @PutMapping
    public StudentProfile save(@RequestBody StudentProfileRequest request, Authentication authentication) {
        Long userId = SecurityUtils.getUserId(authentication, userService);
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "未登录");
        }
        StudentProfile profile = studentProfileService.lambdaQuery().eq(StudentProfile::getUserId, userId).one();
        if (profile == null) {
            profile = new StudentProfile();
            profile.setUserId(userId);
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

    @GetMapping("/summary")
    public StudentProfileSummaryResponse summary(Authentication authentication) {
        Long userId = SecurityUtils.getUserId(authentication, userService);
        if (userId == null) {
            return new StudentProfileSummaryResponse(false, 0, 0, 0);
        }
        StudentProfile profile = studentProfileService.lambdaQuery().eq(StudentProfile::getUserId, userId).one();
        if (profile == null || profile.getRank() == null) {
            return new StudentProfileSummaryResponse(false, 0, 0, 0);
        }
        List<RecommendResponse> segments = recommendationEngine.buildSegments(profile.getRank(), profile.getProvince());
        int rush = segments.get(0).getItems().size();
        int match = segments.get(1).getItems().size();
        int safe = segments.get(2).getItems().size();
        return new StudentProfileSummaryResponse(true, rush, match, safe);
    }
}
