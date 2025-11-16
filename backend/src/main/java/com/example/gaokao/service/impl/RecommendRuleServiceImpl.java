package com.example.gaokao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.gaokao.domain.RecommendRule;
import com.example.gaokao.mapper.RecommendRuleMapper;
import com.example.gaokao.service.RecommendRuleService;
import org.springframework.stereotype.Service;

@Service
public class RecommendRuleServiceImpl extends ServiceImpl<RecommendRuleMapper, RecommendRule> implements RecommendRuleService {

    private static final long DEFAULT_RULE_ID = 1L;

    @Override
    public RecommendRule getActiveRule() {
        RecommendRule rule = getById(DEFAULT_RULE_ID);
        if (rule == null) {
            rule = new RecommendRule();
            rule.setId(DEFAULT_RULE_ID);
            rule.setRushOffset(200);
            rule.setMatchOffset(200);
            rule.setSafeOffset(200);
            save(rule);
        }
        return rule;
    }
}
