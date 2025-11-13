package com.example.gaokao.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("major")
public class Major {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String category;
    private String discipline;
    private String subjectReq;
    private String level;
    private String remark;
}
