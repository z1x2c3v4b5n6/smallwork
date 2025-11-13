package com.example.gaokao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.gaokao.domain.PlanItem;
import com.example.gaokao.mapper.PlanItemMapper;
import com.example.gaokao.service.PlanItemService;
import org.springframework.stereotype.Service;

@Service
public class PlanItemServiceImpl extends ServiceImpl<PlanItemMapper, PlanItem> implements PlanItemService {
}
