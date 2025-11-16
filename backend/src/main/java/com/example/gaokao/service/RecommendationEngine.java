package com.example.gaokao.service;

import com.example.gaokao.domain.RecommendRule;
import com.example.gaokao.domain.dto.MajorSearchResult;
import com.example.gaokao.domain.dto.RecommendResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RecommendationEngine {

    private final StudentSearchControllerAdapter searchAdapter;
    private final RecommendRuleService recommendRuleService;

    public RecommendationEngine(StudentSearchControllerAdapter searchAdapter, RecommendRuleService recommendRuleService) {
        this.searchAdapter = searchAdapter;
        this.recommendRuleService = recommendRuleService;
    }

    public enum RiskLevel {
        RUSH("冲刺", "danger"),
        MATCH("稳妥", "warning"),
        SAFE("保底", "success"),
        UNKNOWN("待评估", "info");

        private final String label;
        private final String tagType;

        RiskLevel(String label, String tagType) {
            this.label = label;
            this.tagType = tagType;
        }

        public String getLabel() {
            return label;
        }

        public String getTagType() {
            return tagType;
        }
    }

    public List<RecommendResponse> buildSegments(int currentRank, String province) {
        List<MajorSearchResult> all = searchAdapter.searchForRecommendation(province);
        all = all.stream()
                .filter(item -> item.getLatestMinRank() != null)
                .sorted(Comparator.comparing(MajorSearchResult::getLatestMinRank))
                .collect(Collectors.toList());

        List<MajorSearchResult> rush = new ArrayList<>();
        List<MajorSearchResult> match = new ArrayList<>();
        List<MajorSearchResult> safe = new ArrayList<>();
        for (MajorSearchResult item : all) {
            RiskLevel level = evaluateRisk(currentRank, item.getLatestMinRank());
            switch (level) {
                case RUSH -> rush.add(item);
                case MATCH -> match.add(item);
                case SAFE -> safe.add(item);
                default -> {
                }
            }
        }
        return List.of(
                new RecommendResponse("冲刺", rush.stream().limit(10).collect(Collectors.toList())),
                new RecommendResponse("稳妥", match.stream().limit(10).collect(Collectors.toList())),
                new RecommendResponse("保底", safe.stream().limit(10).collect(Collectors.toList()))
        );
    }

    public RiskLevel evaluateRisk(Integer currentRank, Integer targetRank) {
        if (currentRank == null || targetRank == null) {
            return RiskLevel.UNKNOWN;
        }
        RecommendRule rule = recommendRuleService.getActiveRule();
        int rushOffset = rule.getRushOffset() != null ? rule.getRushOffset() : 200;
        int matchOffset = rule.getMatchOffset() != null ? rule.getMatchOffset() : 200;
        int safeOffset = rule.getSafeOffset() != null ? rule.getSafeOffset() : 200;

        int diff = currentRank - targetRank;
        if (diff >= rushOffset) {
            return RiskLevel.RUSH;
        }
        if (Math.abs(diff) <= matchOffset) {
            return RiskLevel.MATCH;
        }
        if (diff <= -safeOffset) {
            return RiskLevel.SAFE;
        }
        return diff >= 0 ? RiskLevel.RUSH : RiskLevel.SAFE;
    }
}
