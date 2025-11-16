package com.example.gaokao.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportResult {
    private boolean success;
    private int count;
    @Builder.Default
    private List<ImportError> errors = new ArrayList<>();
}
