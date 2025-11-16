package com.example.gaokao.domain.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class StudentOverview {
    private Long userId;
    private String username;
    private String realName;
    private String nickname;
    private String province;
    private Integer score;
    private Integer rank;
    private Integer firstMockScore;
    private Integer secondMockScore;
    private String subjects;
    private String targetMajorType;
    private Boolean enabled;
    private LocalDateTime lastLoginTime;
}
