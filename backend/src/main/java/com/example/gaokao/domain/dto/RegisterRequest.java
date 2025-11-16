package com.example.gaokao.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank
    private String username;
    @NotBlank
    private String password;
    @NotBlank
    private String confirmPassword;
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
