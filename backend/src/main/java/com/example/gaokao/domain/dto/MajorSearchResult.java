package com.example.gaokao.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MajorSearchResult {
    private Long universityId;
    private String universityName;
    private String province;
    private Long majorId;
    private String majorName;
    private String category;
    private String subjectReq;
    private String level;
    private String batch;
    private Integer latestMinScore;
    private Integer latestMinRank;
}
