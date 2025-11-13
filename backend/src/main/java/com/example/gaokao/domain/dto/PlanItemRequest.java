package com.example.gaokao.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PlanItemRequest {
    @NotNull
    private Long universityId;
    @NotNull
    private Long majorId;
    private String batch;
    private Integer orderNo;
}
