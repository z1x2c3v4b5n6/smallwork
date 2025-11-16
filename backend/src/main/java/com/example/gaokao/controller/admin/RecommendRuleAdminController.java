package com.example.gaokao.controller.admin;

import com.example.gaokao.domain.RecommendRule;
import com.example.gaokao.domain.dto.RecommendRuleRequest;
import com.example.gaokao.service.RecommendRuleService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/admin/recommend-rule")
public class RecommendRuleAdminController {

    private final RecommendRuleService recommendRuleService;

    public RecommendRuleAdminController(RecommendRuleService recommendRuleService) {
        this.recommendRuleService = recommendRuleService;
    }

    @GetMapping
    public RecommendRule getRule() {
        return recommendRuleService.getActiveRule();
    }

    @PutMapping
    public RecommendRule updateRule(@RequestBody RecommendRuleRequest request) {
        RecommendRule rule = recommendRuleService.getActiveRule();
        if (request.getRushOffset() != null) {
            validatePositive(request.getRushOffset(), "冲刺阈值");
            rule.setRushOffset(request.getRushOffset());
        }
        if (request.getMatchOffset() != null) {
            validatePositive(request.getMatchOffset(), "稳妥区间");
            rule.setMatchOffset(request.getMatchOffset());
        }
        if (request.getSafeOffset() != null) {
            validatePositive(request.getSafeOffset(), "保底阈值");
            rule.setSafeOffset(request.getSafeOffset());
        }
        recommendRuleService.updateById(rule);
        return rule;
    }

    private void validatePositive(int value, String label) {
        if (value <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, label + "需为正数");
        }
    }
}
