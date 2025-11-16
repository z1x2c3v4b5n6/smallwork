package com.example.gaokao.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MajorSearchResult {
    private Long universityId;
    private String universityName;
    private String province;
    private String city;
    private String universityLevel;
    private String universityType;
    private Boolean doubleFirstClass;
    private String universityRemark;
    private Long majorId;
    private String majorName;
    private String category;
    private String subjectReq;
    private String level;
    private String discipline;
    private String majorRemark;
    private String batch;
    private Integer duration;
    private Integer tuition;
    private Integer latestMinScore;
    private Integer latestMinRank;
}
