package com.example.gaokao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.gaokao.domain.Plan;
import com.example.gaokao.mapper.PlanMapper;
import com.example.gaokao.service.PlanService;
import org.springframework.stereotype.Service;

@Service
public class PlanServiceImpl extends ServiceImpl<PlanMapper, Plan> implements PlanService {
}
