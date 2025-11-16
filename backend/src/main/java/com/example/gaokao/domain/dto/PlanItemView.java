package com.example.gaokao.domain.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlanItemView {
    private Long id;
    private Integer orderNo;
    private String batch;
    private Long universityId;
    private String universityName;
    private Long majorId;
    private String majorName;
    private Integer latestMinRank;
    private Integer latestMinScore;
    private String riskLabel;
    private String riskType;
}
