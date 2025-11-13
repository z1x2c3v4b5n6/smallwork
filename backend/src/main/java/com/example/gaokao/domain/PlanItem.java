package com.example.gaokao.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("plan_item")
public class PlanItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("plan_id")
    private Long planId;
    @TableField("university_id")
    private Long universityId;
    @TableField("major_id")
    private Long majorId;
    private String batch;
    @TableField("order_no")
    private Integer orderNo;
}
