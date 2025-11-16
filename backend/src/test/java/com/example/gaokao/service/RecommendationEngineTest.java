package com.example.gaokao.service;

import com.example.gaokao.domain.RecommendRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RecommendationEngineTest {

    private RecommendRuleService recommendRuleService;
    private RecommendationEngine recommendationEngine;

    @BeforeEach
    void setUp() {
        recommendRuleService = Mockito.mock(RecommendRuleService.class);
        RecommendRule rule = new RecommendRule();
        rule.setRushOffset(200);
        rule.setMatchOffset(80);
        rule.setSafeOffset(150);
        Mockito.when(recommendRuleService.getActiveRule()).thenReturn(rule);
        recommendationEngine = new RecommendationEngine(null, recommendRuleService);
    }

    @Test
    void returnsUnknownWhenRankMissing() {
        assertEquals(RecommendationEngine.RiskLevel.UNKNOWN, recommendationEngine.evaluateRisk(null, 1000));
        assertEquals(RecommendationEngine.RiskLevel.UNKNOWN, recommendationEngine.evaluateRisk(1000, null));
    }

    @Test
    void classifiesRushWhenAheadBeyondOffset() {
        RecommendationEngine.RiskLevel level = recommendationEngine.evaluateRisk(2000, 1700);
        assertEquals(RecommendationEngine.RiskLevel.RUSH, level);
    }

    @Test
    void classifiesMatchWithinWindow() {
        RecommendationEngine.RiskLevel level = recommendationEngine.evaluateRisk(2100, 2050);
        assertEquals(RecommendationEngine.RiskLevel.MATCH, level);
    }

    @Test
    void classifiesSafeWhenBehindBeyondOffset() {
        RecommendationEngine.RiskLevel level = recommendationEngine.evaluateRisk(1800, 2100);
        assertEquals(RecommendationEngine.RiskLevel.SAFE, level);
    }

    @Test
    void fallsBackToRushWhenSlightlyAhead() {
        RecommendationEngine.RiskLevel level = recommendationEngine.evaluateRisk(2000, 1850);
        assertEquals(RecommendationEngine.RiskLevel.RUSH, level);
    }

    @Test
    void fallsBackToSafeWhenSlightlyBehind() {
        RecommendationEngine.RiskLevel level = recommendationEngine.evaluateRisk(1800, 1900);
        assertEquals(RecommendationEngine.RiskLevel.SAFE, level);
    }
}
