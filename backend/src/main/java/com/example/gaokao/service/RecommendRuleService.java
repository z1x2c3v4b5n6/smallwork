package com.example.gaokao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.gaokao.domain.RecommendRule;

public interface RecommendRuleService extends IService<RecommendRule> {
    RecommendRule getActiveRule();
}
