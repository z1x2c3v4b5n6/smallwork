package com.example.gaokao.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("university_major")
public class UniversityMajor {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("university_id")
    private Long universityId;
    @TableField("major_id")
    private Long majorId;
    private String batch;
    private Integer duration;
    private Integer tuition;
    private String remark;
}
