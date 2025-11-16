package com.example.gaokao.controller.student;

import com.example.gaokao.domain.StudentProfile;
import com.example.gaokao.domain.dto.RecommendRequest;
import com.example.gaokao.domain.dto.RecommendResponse;
import com.example.gaokao.service.RecommendationEngine;
import com.example.gaokao.service.StudentProfileService;
import com.example.gaokao.service.UserService;
import com.example.gaokao.util.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/student/recommend")
public class RecommendController {

    private final RecommendationEngine recommendationEngine;
    private final StudentProfileService studentProfileService;
    private final UserService userService;

    public RecommendController(RecommendationEngine recommendationEngine, StudentProfileService studentProfileService,
                               UserService userService) {
        this.recommendationEngine = recommendationEngine;
        this.studentProfileService = studentProfileService;
        this.userService = userService;
    }

    @PostMapping
    public List<RecommendResponse> recommend(@RequestBody RecommendRequest request, Authentication authentication) {
        Long userId = SecurityUtils.getUserId(authentication, userService);
        StudentProfile profile = userId != null
                ? studentProfileService.lambdaQuery().eq(StudentProfile::getUserId, userId).one()
                : null;
        Integer rank = request.getCurrentRank();
        if (rank == null && profile != null) {
            rank = profile.getRank();
        }
        String province = request.getProvince();
        if ((province == null || province.isEmpty()) && profile != null) {
            province = profile.getProvince();
        }
        if (rank == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "请先完善个人信息中的位次");
        }
        return recommendationEngine.buildSegments(rank, province);
    }
}
