package com.example.gaokao.domain.dto;

import lombok.Data;

@Data
public class RecommendRequest {
    private Integer currentRank;
    private String subjects;
    private String province;
}
