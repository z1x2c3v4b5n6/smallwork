package com.example.gaokao.domain.dto;

import lombok.Data;

@Data
public class RecommendRuleRequest {
    private Integer rushOffset;
    private Integer matchOffset;
    private Integer safeOffset;
}
