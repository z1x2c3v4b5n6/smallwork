package com.example.gaokao.domain.dto;

import lombok.Data;

@Data
public class StudentProfileRequest {
    private String realName;
    private String nickname;
    private Integer score;
    private Integer rank;
    private Integer firstMockScore;
    private Integer firstMockRank;
    private Integer secondMockScore;
    private Integer secondMockRank;
    private String subjects;
    private String province;
    private String targetMajorType;
}
