package com.example.gaokao.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class RecommendResponse {
    private String segment;
    private List<MajorSearchResult> items;
}
