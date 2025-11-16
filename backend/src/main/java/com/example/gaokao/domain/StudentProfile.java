package com.example.gaokao.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("student_profile")
public class StudentProfile {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("user_id")
    private Long userId;
    @TableField("real_name")
    private String realName;
    private String nickname;
    private Integer score;
    private Integer rank;
    @TableField("first_mock_score")
    private Integer firstMockScore;
    @TableField("first_mock_rank")
    private Integer firstMockRank;
    @TableField("second_mock_score")
    private Integer secondMockScore;
    @TableField("second_mock_rank")
    private Integer secondMockRank;
    private String subjects;
    private String province;
    @TableField("target_major_type")
    private String targetMajorType;
}
