package com.example.gaokao.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentProfileSummaryResponse {
    private boolean ready;
    private int rushCount;
    private int matchCount;
    private int safeCount;

    public int getTotal() {
        return rushCount + matchCount + safeCount;
    }
}
