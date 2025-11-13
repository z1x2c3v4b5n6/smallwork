package com.example.gaokao.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RecommendRequest {
    @NotNull
    private Integer currentRank;
    private String subjects;
    private String province;
}
