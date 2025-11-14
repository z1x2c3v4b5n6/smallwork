package com.example.gaokao.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("university")
public class University {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String province;
    private String city;
    private String level;
    private String type;
    private Boolean isDoubleTop;
    private String remark;
}
