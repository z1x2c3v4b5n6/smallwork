package com.example.gaokao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.gaokao.domain.AdmissionStat;
import com.example.gaokao.mapper.AdmissionStatMapper;
import com.example.gaokao.service.AdmissionStatService;
import org.springframework.stereotype.Service;

@Service
public class AdmissionStatServiceImpl extends ServiceImpl<AdmissionStatMapper, AdmissionStat> implements AdmissionStatService {
}
