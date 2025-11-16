package com.example.gaokao.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("recommend_rule")
public class RecommendRule {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Integer rushOffset;
    private Integer matchOffset;
    private Integer safeOffset;
    private LocalDateTime updateTime;
}
