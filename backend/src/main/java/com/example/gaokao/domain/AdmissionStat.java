package com.example.gaokao.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("admission_stat")
public class AdmissionStat {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("university_id")
    private Long universityId;
    @TableField("major_id")
    private Long majorId;
    private Integer year;
    private String batch;
    @TableField("min_score")
    private Integer minScore;
    @TableField("min_rank")
    private Integer minRank;
    @TableField("avg_score")
    private Integer avgScore;
    @TableField("avg_rank")
    private Integer avgRank;
    @TableField("admit_count")
    private Integer admitCount;
}
